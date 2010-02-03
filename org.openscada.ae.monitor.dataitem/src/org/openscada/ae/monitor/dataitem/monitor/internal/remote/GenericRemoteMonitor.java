package org.openscada.ae.monitor.dataitem.monitor.internal.remote;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import org.openscada.ae.ConditionStatus;
import org.openscada.ae.ConditionStatusInformation;
import org.openscada.ae.Event;
import org.openscada.ae.Event.EventBuilder;
import org.openscada.ae.Event.Fields;
import org.openscada.ae.event.EventProcessor;
import org.openscada.ae.monitor.ConditionListener;
import org.openscada.core.Variant;
import org.openscada.da.client.DataItemValue;
import org.openscada.da.client.DataItemValue.Builder;
import org.openscada.da.master.AbstractMasterHandlerImpl;
import org.openscada.da.master.MasterItem;
import org.openscada.utils.osgi.pool.ObjectPoolTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GenericRemoteMonitor extends AbstractMasterHandlerImpl
{
    private final static Logger logger = LoggerFactory.getLogger ( GenericRemoteMonitor.class );

    protected static Map<String, Variant> convertAttributes ( final Map<String, String> parameters )
    {
        final Map<String, Variant> attributes = new HashMap<String, Variant> ();

        for ( final Map.Entry<String, String> entry : parameters.entrySet () )
        {
            final String key = entry.getKey ();
            if ( key.startsWith ( "info." ) )
            {
                attributes.put ( key.substring ( "info.".length () ), new Variant ( entry.getValue () ) );
            }
        }

        return attributes;
    }

    protected final String id;

    protected ConditionStatus state;

    protected Date timestamp;

    private final Set<ConditionListener> listeners = new HashSet<ConditionListener> ();

    protected final EventProcessor eventProcessor;

    protected Map<String, Variant> attributes = new HashMap<String, Variant> ();

    protected final Executor executor;

    public GenericRemoteMonitor ( final Executor executor, final ObjectPoolTracker poolTracker, final int priority, final String id, final EventProcessor eventProcessor )
    {
        super ( poolTracker, priority );
        this.executor = executor;
        this.eventProcessor = eventProcessor;
        this.id = id;
    }

    public String getId ()
    {
        return this.id;
    }

    public void init ()
    {
        setState ( ConditionStatus.UNSAFE );
    }

    protected void setState ( final ConditionStatus state )
    {
        setState ( state, Calendar.getInstance () );
    }

    protected void setState ( final ConditionStatus state, final Calendar timestamp )
    {
        if ( this.state != state )
        {
            this.state = state;
            this.timestamp = timestamp.getTime ();
            logger.debug ( "State is: {}", state );

            final ConditionStatusInformation info = createStatus ();

            this.eventProcessor.publishEvent ( createEvent ( info, state.toString () ) );

            final ArrayList<ConditionListener> listnersClone = new ArrayList<ConditionListener> ( this.listeners );
            this.executor.execute ( new Runnable () {

                public void run ()
                {
                    for ( final ConditionListener listener : listnersClone )
                    {
                        listener.statusChanged ( info );
                    }
                }
            } );
        }
    }

    protected abstract DataItemValue handleUpdate ( final DataItemValue itemValue );

    @Override
    public synchronized DataItemValue dataUpdate ( final DataItemValue value )
    {
        logger.debug ( "Data update" );

        if ( value == null )
        {
            setState ( ConditionStatus.UNSAFE );
            return null;
        }

        return handleUpdate ( value );
    }

    @Override
    public synchronized void update ( final Map<String, String> parameters ) throws Exception
    {
        super.update ( parameters );
        this.attributes = convertAttributes ( parameters );
    }

    private ConditionStatusInformation createStatus ()
    {
        return new ConditionStatusInformation ( this.id, this.state, this.timestamp, null, null, null );
    }

    private Event createEvent ( final ConditionStatusInformation info, final String eventType )
    {
        final EventBuilder builder = Event.create ();
        builder.sourceTimestamp ( info.getStatusTimestamp () );
        builder.entryTimestamp ( new Date () );
        builder.attribute ( Fields.SOURCE, this.id );
        builder.attribute ( Fields.EVENT_TYPE, eventType );
        builder.attributes ( this.attributes );
        return builder.build ();
    }

    protected void publishAckRequestEvent ()
    {
        final EventBuilder builder = Event.create ();
        builder.sourceTimestamp ( new Date () );
        builder.entryTimestamp ( new Date () );
        builder.attribute ( Fields.SOURCE, this.id );
        builder.attribute ( Fields.EVENT_TYPE, "ACK-REQ" );
        builder.attributes ( this.attributes );

        this.eventProcessor.publishEvent ( builder.build () );
    }

    protected Builder injectState ( final Builder builder )
    {
        builder.setAttribute ( this.id + ".state", new Variant ( this.state.toString () ) );
        return builder;
    }

    public synchronized void addStatusListener ( final ConditionListener listener )
    {
        if ( this.listeners.add ( listener ) )
        {
            final ConditionStatusInformation state = createStatus ();
            this.executor.execute ( new Runnable () {

                public void run ()
                {
                    listener.statusChanged ( state );
                }
            } );
        }
    }

    public synchronized void removeStatusListener ( final ConditionListener listener )
    {
        this.listeners.remove ( listener );
    }

    protected void reprocess ()
    {
        this.executor.execute ( new Runnable () {

            public void run ()
            {
                logger.debug ( "Reprocessing {} master items", getMasterItems ().size () );

                for ( final MasterItem item : getMasterItems () )
                {
                    item.reprocess ();
                }
            }
        } );
    }

}
