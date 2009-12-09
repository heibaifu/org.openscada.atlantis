package org.openscada.ae.server.common.event.internal;

import java.util.Dictionary;
import java.util.Hashtable;

import org.openscada.ca.ConfigurationAdministrator;
import org.openscada.ca.ConfigurationFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

public class Activator implements BundleActivator
{

    private EventPoolConfigurationFactory factory;

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start ( final BundleContext context ) throws Exception
    {
        this.factory = new EventPoolConfigurationFactory ( context );

        final Dictionary<String, String> properties = new Hashtable<String, String> ();
        properties.put ( ConfigurationAdministrator.FACTORY_ID, context.getBundle ().getSymbolicName () + ".pool" );
        properties.put ( Constants.SERVICE_VENDOR, "inavare GmbH" );
        properties.put ( Constants.SERVICE_DESCRIPTION, "Event pool service factory" );
        context.registerService ( ConfigurationFactory.class.getName (), this.factory, properties );
    }

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop ( final BundleContext context ) throws Exception
    {
        this.factory.dispose ();
    }

}
