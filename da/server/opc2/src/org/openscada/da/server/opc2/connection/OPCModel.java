/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2006-2008 inavare GmbH (http://inavare.com)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.openscada.da.server.opc2.connection;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.jinterop.dcom.core.JISession;
import org.openscada.da.server.common.exporter.AbstractPropertyChange;
import org.openscada.opc.dcom.common.impl.OPCCommon;
import org.openscada.opc.dcom.da.OPCSERVERSTATUS;
import org.openscada.opc.dcom.da.impl.OPCGroupStateMgt;
import org.openscada.opc.dcom.da.impl.OPCItemMgt;
import org.openscada.opc.dcom.da.impl.OPCServer;
import org.openscada.opc.dcom.da.impl.OPCSyncIO;

public class OPCModel extends AbstractPropertyChange
{
    private boolean connectionRequested;

    private boolean connecting;

    private JISession session;

    private OPCServer server;

    private long lastConnect;

    private long reconnectDelay = 5000L;

    private OPCSERVERSTATUS serverState;

    private OPCGroupStateMgt group;

    private OPCItemMgt itemMgt;

    private OPCSyncIO syncIo;

    private OPCCommon common;

    private Throwable lastConnectionError;

    private ConnectionState connectionState = ConnectionState.DISCONNECTED;

    private final Set<Thread> disposersRunning = new CopyOnWriteArraySet<Thread> ();

    private ControllerState controllerState = ControllerState.IDLE;

    private long loopDelay = 250;

    private long defaultTimeout = Long.getLong ( "rpc.socketTimeout", 5000L );

    private Long globalTimeout = null;

    private Long connectJobTimeout = null;

    private Long statusJobTimeout = null;

    private Long writeJobTimeout = null;

    private Long readJobTimeout = null;

    private int updateRate = 250;

    /**
     * Flag that indicates if the driver should ignore timestamp only changes completly
     */
    private boolean ignoreTimestampOnlyChange = false;

    public boolean isIgnoreTimestampOnlyChange ()
    {
        return this.ignoreTimestampOnlyChange;
    }

    public void setIgnoreTimestampOnlyChange ( final boolean ignoreTimestampOnlyChange )
    {
        final boolean oldIgnoreTimestampOnlyChange = this.ignoreTimestampOnlyChange;
        this.ignoreTimestampOnlyChange = ignoreTimestampOnlyChange;
        this.listeners.firePropertyChange ( "ignoreTimestampOnlyChange", oldIgnoreTimestampOnlyChange, ignoreTimestampOnlyChange );
    }

    public void setLastConnectNow ()
    {
        setLastConnect ( System.currentTimeMillis () );
    }

    public void setLastConnect ( final long lastConnect )
    {
        final long oldLastConnect = this.lastConnect;
        this.lastConnect = lastConnect;
        this.listeners.firePropertyChange ( "lastConnect", oldLastConnect, lastConnect );
    }

    public long getLastConnect ()
    {
        return this.lastConnect;
    }

    public boolean mayConnect ()
    {
        return System.currentTimeMillis () - this.lastConnect > this.reconnectDelay;
    }

    public boolean isConnected ()
    {
        return this.session != null && this.server != null;
    }

    public JISession getSession ()
    {
        return this.session;
    }

    public void setSession ( final JISession session )
    {
        final JISession oldSession = this.session;
        final boolean oldConnected = isConnected ();

        this.session = session;

        this.listeners.firePropertyChange ( "session", oldSession, session );
        this.listeners.firePropertyChange ( "connected", oldConnected, isConnected () );
    }

    public OPCServer getServer ()
    {
        return this.server;
    }

    public void setServer ( final OPCServer server )
    {
        final OPCServer oldServer = this.server;
        final boolean oldConnected = isConnected ();

        this.server = server;

        this.listeners.firePropertyChange ( "session", oldServer, this.session );
        this.listeners.firePropertyChange ( "connected", oldConnected, isConnected () );
    }

    public boolean isConnectionRequested ()
    {
        return this.connectionRequested;
    }

    public void setConnectionRequested ( final boolean connectionRequested )
    {
        this.connectionRequested = connectionRequested;
    }

    public boolean isConnecting ()
    {
        return this.connecting;
    }

    public void setConnecting ( final boolean connecting )
    {
        final boolean oldConnecting = this.connecting;
        this.connecting = connecting;
        this.listeners.firePropertyChange ( "connecting", oldConnecting, connecting );
    }

    public long getReconnectDelay ()
    {
        return this.reconnectDelay;
    }

    public void setReconnectDelay ( final long reconnectDelay )
    {
        this.reconnectDelay = reconnectDelay;
    }

    public OPCSERVERSTATUS getServerState ()
    {
        return this.serverState;
    }

    public void setServerState ( final OPCSERVERSTATUS serverState )
    {
        final OPCSERVERSTATUS oldServerState = this.serverState;
        this.serverState = serverState;
        this.listeners.firePropertyChange ( "serverState", oldServerState, serverState );
    }

    public OPCGroupStateMgt getGroup ()
    {
        return this.group;
    }

    public void setGroup ( final OPCGroupStateMgt group )
    {
        this.group = group;
    }

    public OPCItemMgt getItemMgt ()
    {
        return this.itemMgt;
    }

    public void setItemMgt ( final OPCItemMgt itemMgt )
    {
        this.itemMgt = itemMgt;
    }

    public OPCSyncIO getSyncIo ()
    {
        return this.syncIo;
    }

    public void setSyncIo ( final OPCSyncIO syncIo )
    {
        this.syncIo = syncIo;
    }

    public OPCCommon getCommon ()
    {
        return this.common;
    }

    public void setCommon ( final OPCCommon common )
    {
        this.common = common;
    }

    public Throwable getLastConnectionError ()
    {
        return this.lastConnectionError;
    }

    public void setLastConnectionError ( final Throwable lastConnectionError )
    {
        final Throwable oldLastConnectionError = this.lastConnectionError;
        this.lastConnectionError = lastConnectionError;
        this.listeners.firePropertyChange ( "lastConnectionError", oldLastConnectionError, lastConnectionError );
    }

    public ConnectionState getConnectionState ()
    {
        return this.connectionState;
    }

    public void setConnectionState ( final ConnectionState connectionState )
    {
        final ConnectionState oldConnectionState = this.connectionState;
        this.connectionState = connectionState;
        this.listeners.firePropertyChange ( "connectionState", oldConnectionState, connectionState );
    }

    public long getNumDisposersRunning ()
    {
        return this.disposersRunning.size ();
    }

    public void addDisposerRunning ( final Thread disposer )
    {
        long oldDisposersRunning;
        long disposersRunning;

        synchronized ( this.disposersRunning )
        {
            oldDisposersRunning = this.disposersRunning.size ();
            this.disposersRunning.add ( disposer );
            disposersRunning = this.disposersRunning.size ();
        }
        this.listeners.firePropertyChange ( "numDisposersRunning", oldDisposersRunning, disposersRunning );
    }

    public void removeDisposerRunning ( final Thread disposer )
    {
        long oldDisposersRunning;
        long disposersRunning;

        synchronized ( this.disposersRunning )
        {
            oldDisposersRunning = this.disposersRunning.size ();
            this.disposersRunning.remove ( disposer );
            disposersRunning = this.disposersRunning.size ();
        }
        this.listeners.firePropertyChange ( "numDisposersRunning", oldDisposersRunning, disposersRunning );
    }

    public ControllerState getControllerState ()
    {
        return this.controllerState;
    }

    public void setControllerState ( final ControllerState controllerState )
    {
        final ControllerState oldControllerState = this.controllerState;
        this.controllerState = controllerState;
        this.listeners.firePropertyChange ( "controllerState", oldControllerState, controllerState );
    }

    public long getLoopDelay ()
    {
        return this.loopDelay;
    }

    public void setLoopDelay ( final long loopDelay )
    {
        final long oldLoopDelay = this.loopDelay;
        this.loopDelay = loopDelay;
        this.listeners.firePropertyChange ( "loopDelay", oldLoopDelay, loopDelay );
    }

    public long getDefaultTimeout ()
    {
        return this.defaultTimeout;
    }

    public void setDefaultTimeout ( final long defaultTimeout )
    {
        this.defaultTimeout = defaultTimeout;
    }

    public long getGlobalTimeout ()
    {
        final Long globalTimeout = this.globalTimeout;
        if ( globalTimeout == null )
        {
            return this.defaultTimeout;
        }
        return globalTimeout;
    }

    public void setGlobalTimeout ( final Long globalTimeout )
    {
        this.globalTimeout = globalTimeout;
    }

    public int getUpdateRate ()
    {
        return this.updateRate;
    }

    public void setUpdateRate ( final int updateRate )
    {
        final int oldUpdateRate = this.updateRate;
        this.updateRate = updateRate;
        this.listeners.firePropertyChange ( "updateRate", oldUpdateRate, updateRate );
    }

    public long getConnectJobTimeout ()
    {
        final Long connectJobTimeout = this.connectJobTimeout;
        if ( connectJobTimeout == null )
        {
            return this.defaultTimeout;
        }
        return connectJobTimeout;
    }

    public void setConnectJobTimeout ( final Long connectJobTimeout )
    {
        this.connectJobTimeout = connectJobTimeout;
    }

    public long getStatusJobTimeout ()
    {
        final Long statusJobTimeout = this.statusJobTimeout;
        if ( statusJobTimeout == null )
        {
            return this.defaultTimeout;
        }
        return statusJobTimeout;
    }

    public void setStatusJobTimeout ( final Long statusJobTimeout )
    {
        this.statusJobTimeout = statusJobTimeout;
    }

    public long getReadJobTimeout ()
    {
        final Long readJobTimeout = this.readJobTimeout;
        if ( readJobTimeout == null )
        {
            return this.defaultTimeout;
        }
        return readJobTimeout;
    }

    public void setReadJobTimeout ( final Long readJobTimeout )
    {
        this.readJobTimeout = readJobTimeout;
    }

    public long getWriteJobTimeout ()
    {
        final Long writeJobTimeout = this.writeJobTimeout;
        if ( writeJobTimeout == null )
        {
            return this.defaultTimeout;
        }
        return writeJobTimeout;
    }

    public void setWriteJobTimeout ( final Long writeJobTimeout )
    {
        this.writeJobTimeout = writeJobTimeout;
    }
}
