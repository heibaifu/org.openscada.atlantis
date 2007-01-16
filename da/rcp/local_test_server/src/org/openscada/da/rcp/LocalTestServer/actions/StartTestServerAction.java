/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2006 inavare GmbH (http://inavare.com)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openscada.da.rcp.LocalTestServer.actions;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.eclipse.core.commands.operations.OperationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.openscada.da.server.common.configuration.ConfigurationError;
import org.openscada.da.rcp.LocalTestServer.Activator;
import org.openscada.da.rcp.LocalTestServer.AlreadyStartedException;

public class StartTestServerAction implements IWorkbenchWindowActionDelegate
{
    private static Logger _log = Logger.getLogger ( StartTestServerAction.class );
    
    private IWorkbenchWindow _window = null;
    
    public void dispose ()
    {
    }

    public void init ( IWorkbenchWindow window )
    {
        _window = window;
    }

    public void run ( IAction action )
    {
        _log.debug ( "Try to start local server" );
        
        IStatus status = null;
        try
        {
            Activator.getDefault ().startLocalServer ();
        }
        catch ( ClassNotFoundException e )
        {
            status = new OperationStatus ( OperationStatus.ERROR, Activator.PLUGIN_ID, 0, "Unable to find hive class", e );
        }
        catch ( InstantiationException e )
        {
            status = new OperationStatus ( OperationStatus.ERROR, Activator.PLUGIN_ID, 0, "Unable to instantiate hive class", e );
        }
        catch ( IllegalAccessException e )
        {
            status = new OperationStatus ( OperationStatus.ERROR, Activator.PLUGIN_ID, 0, "Access violation accessing hive class", e );
        }
        catch ( IOException e )
        {
            status = new OperationStatus ( OperationStatus.ERROR, Activator.PLUGIN_ID, 0, "IO Error", e );
        }
        catch ( AlreadyStartedException e )
        {
            status = new OperationStatus ( OperationStatus.WARNING, Activator.PLUGIN_ID, 0, "Local server was already started", e );
        }
        catch ( ConfigurationError e )
        {
            status = new OperationStatus ( OperationStatus.ERROR, Activator.PLUGIN_ID, 0, "Configuration error", e );
        }
        catch ( XmlException e )
        {
            status = new OperationStatus ( OperationStatus.ERROR, Activator.PLUGIN_ID, 0, "XML Error", e );
        }
        if ( status != null )
        {
            ErrorDialog.openError ( _window.getShell (), null, "Failed to start local server", status );
        }
    }

    public void selectionChanged ( IAction action, ISelection selection )
    {
        // we don't care about a selection
    }

}
