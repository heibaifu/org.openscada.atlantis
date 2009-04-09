package org.openscada.da.server.exec2.command;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openscada.core.Variant;
import org.openscada.da.server.browser.common.FolderCommon;
import org.openscada.da.server.common.AttributeMode;
import org.openscada.da.server.common.chain.DataItemInputChained;
import org.openscada.da.server.common.impl.HiveCommon;
import org.openscada.da.server.common.item.factory.FolderItemFactory;
import org.openscada.da.server.exec2.Hive;
import org.openscada.da.server.exec2.extractor.Extractor;
import org.openscada.da.server.exec2.util.CommandExecutor;
import org.openscada.da.server.exec2.util.DefaultItemFactory;

public class SingleCommandImpl implements SingleCommand
{

    private final String id;

    private HiveCommon hive;

    private final ProcessConfiguration processConfiguration;

    private final Collection<Extractor> extractors;

    private FolderItemFactory itemFactory;

    private FolderCommon folderCommon;

    private DataItemInputChained runningItem;

    private DataItemInputChained exitCodeItem;

    private DataItemInputChained execItem;

    public SingleCommandImpl ( final String id, final ProcessConfiguration processConfiguration, final Collection<Extractor> extractors )
    {
        this.id = id;
        this.processConfiguration = processConfiguration;
        this.extractors = extractors;
    }

    public void execute ()
    {
        this.runningItem.updateData ( new Variant ( true ), null, null );
        final ExecutionResult result = CommandExecutor.executeCommand ( this.processConfiguration );
        this.runningItem.updateData ( new Variant ( false ), null, null );
        this.execItem.updateData ( new Variant (), new HashMap<String, Variant> (), AttributeMode.SET );

        updateStatus ( result );

        for ( final Extractor extractor : this.extractors )
        {
            extractor.process ( result );
        }
    }

    private void updateStatus ( final ExecutionResult result )
    {
        this.exitCodeItem.updateData ( new Variant ( result.getExitValue () ), null, null );

        final Map<String, Variant> attributes = new HashMap<String, Variant> ();
        attributes.put ( "exec.runtime", new Variant ( result.getRuntime () ) );
        attributes.put ( "exec.exitCode", new Variant ( result.getExitValue () ) );
        attributes.put ( "exec.standardOutput", new Variant ( result.getOutput () ) );
        attributes.put ( "exec.errorOutput", new Variant ( result.getErrorOutput () ) );
        this.execItem.updateData ( new Variant ( result.toString () ), attributes, AttributeMode.SET );
    }

    public void register ( final Hive hive, final FolderCommon parentFolder )
    {
        this.hive = hive;
        this.folderCommon = parentFolder;

        this.itemFactory = new DefaultItemFactory ( this.hive, this.folderCommon, this.id, this.id );
        this.runningItem = this.itemFactory.createInput ( "running" );
        this.exitCodeItem = this.itemFactory.createInput ( "exitCode" );
        this.execItem = this.itemFactory.createInput ( "exec" );

        for ( final Extractor ext : this.extractors )
        {
            ext.register ( hive, this.itemFactory );
        }
    }

    public void unregister ()
    {
        this.itemFactory.dispose ();
        this.hive = null;
    }

}
