/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2006-2012 TH4 SYSTEMS GmbH (http://th4-systems.com)
 *
 * OpenSCADA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3
 * only, as published by the Free Software Foundation.
 *
 * OpenSCADA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License version 3 for more details
 * (a copy is included in the LICENSE file that accompanied this code).
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with OpenSCADA. If not, see
 * <http://opensource.org/licenses/lgpl-3.0.html> for a copy of the LGPLv3 License.
 */

package org.openscada.hd.server.ngp;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

import org.apache.mina.filter.ssl.SslContextFactory;
import org.openscada.core.ConnectionInformation;
import org.openscada.hd.server.Service;
import org.openscada.protocol.ngp.common.ProtocolConfiguration;
import org.openscada.protocol.ngp.common.SslHelper;
import org.openscada.utils.lifecycle.LifecycleAware;

public class Exporter implements LifecycleAware
{
    private Server server;

    private final ProtocolConfiguration protocolConfiguration;

    private Collection<InetSocketAddress> addresses = new LinkedList<InetSocketAddress> ();

    private final Service service;

    public Exporter ( final Service service, final ProtocolConfiguration protocolConfiguration, final Collection<InetSocketAddress> addresses )
    {
        this.service = service;
        this.protocolConfiguration = protocolConfiguration;
        this.addresses = addresses;
    }

    public Exporter ( final Service service, final ConnectionInformation connectionInformation ) throws Exception
    {
        this ( service, makeProtocolConfiguration ( connectionInformation ), Collections.singletonList ( new InetSocketAddress ( connectionInformation.getTarget (), connectionInformation.getSecondaryTarget () ) ) );
    }

    public Class<? extends Service> getServiceClass ()
    {
        return this.service.getClass ();
    }

    private static ProtocolConfiguration makeProtocolConfiguration ( final ConnectionInformation connectionInformation ) throws Exception
    {
        final ProtocolConfiguration protocolConfiguration = new ProtocolConfiguration ( Exporter.class.getClassLoader () );
        protocolConfiguration.setSslContextFactory ( makeSslContextFactory ( connectionInformation.getProperties () ) );
        return protocolConfiguration;
    }

    private static SslContextFactory makeSslContextFactory ( final Map<String, String> properties ) throws Exception
    {
        return SslHelper.createDefaultSslFactory ( properties, false );
    }

    private void createServer () throws Exception
    {
        this.server = new Server ( this.addresses, this.protocolConfiguration, this.service );
        this.server.start ();
    }

    @Override
    public void start () throws Exception
    {
        createServer ();
    }

    @Override
    public void stop () throws Exception
    {
        destroyServer ();
    }

    private void destroyServer ()
    {
        if ( this.server != null )
        {
            this.server.dispose ();
            this.server = null;
        }
    }
}
