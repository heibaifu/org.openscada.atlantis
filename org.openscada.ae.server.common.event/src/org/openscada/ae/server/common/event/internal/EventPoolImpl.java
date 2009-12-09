package org.openscada.ae.server.common.event.internal;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;

import org.openscada.ae.Event;
import org.openscada.ae.event.EventListener;
import org.openscada.ae.event.EventManager;
import org.openscada.ae.server.common.event.EventMatcher;
import org.openscada.ae.server.common.event.EventQuery;
import org.openscada.ae.server.storage.Query;
import org.openscada.ae.server.storage.Storage;
import org.openscada.utils.collection.BoundedPriorityQueueSet;
import org.openscada.utils.collection.BoundedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventPoolImpl implements EventListener, EventQuery
{

    private final static Logger logger = LoggerFactory.getLogger ( EventPoolImpl.class );

    private final BoundedQueue<Event> events;

    private final Set<EventListener> listeners = new HashSet<EventListener> ();

    private final Storage storage;

    private final EventManager eventManager;

    private final String filter;

    private final EventMatcher matcher;

    private final Executor executor;

    public EventPoolImpl ( final Executor executor, final Storage storage, final EventManager eventManager, final String filter, final int poolSize )
    {
        this.storage = storage;
        this.eventManager = eventManager;
        this.filter = filter;
        this.matcher = new EventMatcherImpl ( filter );
        this.events = new BoundedPriorityQueueSet<Event> ( poolSize, new Comparator<Event> () {
            public int compare ( final Event o1, final Event o2 )
            {
                return Event.comparator.compare ( o2, o1 );
            }
        } );
        this.executor = executor;
    }

    public synchronized void start () throws Exception
    {
        // add to event manager
        this.eventManager.addEventListener ( this );

        // load initial set from storage
        final Query query = this.storage.query ( this.filter );
        try
        {
            final Collection<Event> result = query.getNext ( this.events.getCapacity () );
            logger.debug ( "Loaded {} entries from storage", result.size () );
            this.events.addAll ( result );
            notifyEvent ( result.toArray ( new Event[result.size ()] ) );
        }
        finally
        {
            query.dispose ();
        }
    }

    public synchronized void stop ()
    {
        this.eventManager.removeEventListener ( this );
    }

    public synchronized void handleEvent ( final Event[] events )
    {
        final Set<Event> toNotify = new HashSet<Event> ();
        for ( final Event event : events )
        {
            if ( this.events.add ( event ) )
            {
                toNotify.add ( event );
            }
        }
        notifyEvent ( toNotify.toArray ( new Event[0] ) );
    }

    private void notifyEvent ( final Event[] event )
    {
        final EventListener[] listeners = this.listeners.toArray ( new EventListener[this.listeners.size ()] );
        this.executor.execute ( new Runnable () {

            public void run ()
            {
                for ( final EventListener listener : listeners )
                {
                    listener.handleEvent ( event );
                }
            }
        } );
    }

    public synchronized void addListener ( final EventListener eventListener )
    {
        this.listeners.add ( eventListener );

        // deliver pool events
        this.executor.execute ( new Runnable () {

            public void run ()
            {
                eventListener.handleEvent ( EventPoolImpl.this.events.toArray ( new Event[EventPoolImpl.this.events.size ()] ) );
            }
        } );
    }

    public synchronized void removeListener ( final EventListener eventListener )
    {
        this.listeners.remove ( eventListener );
    }

}
