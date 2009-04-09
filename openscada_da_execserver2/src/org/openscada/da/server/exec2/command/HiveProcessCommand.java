package org.openscada.da.server.exec2.command;

import java.util.HashMap;
import java.util.Map;

import org.openscada.core.Variant;
import org.openscada.core.VariantEditor;
import org.openscada.da.server.browser.common.FolderCommon;
import org.openscada.da.server.common.AttributeMode;
import org.openscada.da.server.common.chain.DataItemInputChained;
import org.openscada.da.server.common.item.factory.FolderItemFactory;
import org.openscada.da.server.exec2.Hive;
import org.openscada.da.server.exec2.splitter.SplitSplitter;

public class HiveProcessCommand extends AbstractContinuousCommand
{

    private final VariantEditor variantEditor = new VariantEditor ();

    private FolderItemFactory processItemFactory;

    private final Map<String, DataItemInputChained> items = new HashMap<String, DataItemInputChained> ();

    public HiveProcessCommand ( final String id, final ProcessConfiguration processConfiguration, final int restartDelay, final int maxInputBuffer )
    {
        super ( id, processConfiguration, restartDelay, maxInputBuffer, new SplitSplitter ( System.getProperty ( "line.separator" ) ) );
    }

    @Override
    public void start ( final Hive hive, final FolderCommon parentFolder )
    {
        super.start ( hive, parentFolder );
        this.processItemFactory = this.itemFactory.createSubFolderFactory ( "values" );
    }

    @Override
    public void stop ()
    {
        disposeItems ();
        super.stop ();
    }

    @Override
    protected void handleStdLine ( final String line )
    {
        final String[] toks = split ( line );
        if ( toks.length < 1 )
        {
            return;
        }

        if ( "DATA-CHANGED".equals ( toks[0] ) )
        {
            final String itemId = toks[1];
            final String value = toks[2];
            final DataItemInputChained item = this.items.get ( itemId );
            if ( item == null )
            {
                return;
            }

            Variant variant = null;
            this.variantEditor.setAsText ( value );
            variant = (Variant)this.variantEditor.getValue ();

            // get the attributes
            final Map<String, Variant> attributes = new HashMap<String, Variant> ();
            for ( int i = 3; i < toks.length; i++ )
            {
                final String attribute = toks[i];
                final String[] attr = attribute.split ( "=", 2 );
                if ( attr.length > 1 )
                {
                    this.variantEditor.setAsText ( attr[1] );
                    attributes.put ( attr[0], (Variant)this.variantEditor.getValue () );
                }
                else
                {
                    attributes.put ( attr[0], null );
                }

            }
            item.updateData ( variant, attributes, AttributeMode.SET );
        }
        else if ( "REGISTER".equals ( toks[0] ) )
        {
            final String itemId = toks[1];
            if ( !this.items.containsKey ( itemId ) )
            {
                final DataItemInputChained item = this.processItemFactory.createInput ( itemId );
                this.items.put ( itemId, item );
            }
        }
        else if ( "UNREGISTER".equals ( toks[0] ) )
        {
            final String itemId = toks[1];
            final DataItemInputChained item = this.items.get ( itemId );
            if ( item != null )
            {
                this.processItemFactory.disposeItem ( item );
            }
        }
    }

    private String[] split ( final String line )
    {
        return line.split ( "/" );
    }

    @Override
    protected void processFailed ( final Throwable e )
    {
        this.processItemFactory.disposeAllItems ();
        this.items.clear ();
        super.processFailed ( e );
    }

    private void disposeItems ()
    {
        this.processItemFactory.dispose ();
        this.items.clear ();
    }

}
