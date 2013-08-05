/*
 * This file is part of the OpenSCADA project
 * 
 * Copyright (C) 2013 Jens Reimann (ctron@dentrassi.de)
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

package org.openscada.da.server.jdbc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openscada.core.Variant;
import org.openscada.da.server.common.chain.DataItemInputChained;
import org.openscada.da.server.common.chain.WriteHandler;
import org.openscada.da.server.common.item.factory.FolderItemFactory;
import org.openscada.da.server.common.item.factory.ItemFactory;

public class TabularExporter
{
    private final FolderItemFactory itemFactory;

    public static class Entry
    {
        private final String id;

        private final Map<String, Variant> values = new HashMap<> ();

        public Entry ( final String id )
        {
            this.id = id;
        }

        public void put ( final String key, final Variant value )
        {
            this.values.put ( key, value );
        }

        public void putAll ( final Map<String, Variant> values )
        {
            this.values.putAll ( values );
        }

        public String getId ()
        {
            return this.id;
        }

        public Map<String, Variant> getValues ()
        {
            return this.values;
        }
    }

    public static interface WriteHandlerFactory
    {
        public WriteHandler createWriteHandler ( String columnName );
    }

    private static class Row
    {
        private final ItemFactory itemFactory;

        private final WriteHandlerFactory writeHandlerFactory;

        public Row ( final FolderItemFactory itemFactory, final String id, final WriteHandlerFactory writeHandlerFactory )
        {
            this.itemFactory = itemFactory.createSubFolderFactory ( id );
            this.writeHandlerFactory = writeHandlerFactory;
        }

        public void dispose ()
        {
            this.itemFactory.dispose ();
            this.items.clear ();
        }

        private final Map<String, DataItemInputChained> items = new HashMap<> ();

        public void update ( final Map<String, Variant> values )
        {
            final Set<String> currentItems = this.items.keySet ();

            for ( final Map.Entry<String, Variant> entry : values.entrySet () )
            {
                DataItemInputChained item = this.items.get ( entry.getKey () );
                if ( item == null )
                {
                    item = createItem ( entry.getKey () );
                }
                else
                {
                    currentItems.remove ( entry.getKey () );
                }
                item.updateData ( entry.getValue (), null, null );
            }

            for ( final String itemId : currentItems )
            {
                final DataItemInputChained item = this.items.remove ( itemId );
                if ( item != null )
                {
                    this.itemFactory.disposeItem ( item );
                }
            }
        }

        public DataItemInputChained createItem ( final String columnName )
        {
            final WriteHandler writeHandler = this.writeHandlerFactory.createWriteHandler ( columnName );
            if ( writeHandler != null )
            {
                return this.itemFactory.createInputOutput ( columnName, null, writeHandler );
            }
            else
            {
                return this.itemFactory.createInput ( columnName, null );
            }
        }
    }

    public Map<String, Row> rows = new HashMap<> ();

    private final WriteHandlerFactory writeHandlerFactory;

    public TabularExporter ( final FolderItemFactory itemFactory, final WriteHandlerFactory writeHandlerFactory )
    {
        this.itemFactory = itemFactory;
        this.writeHandlerFactory = writeHandlerFactory;
    }

    public void update ( final List<Entry> entries )
    {
        final Set<String> keys = new HashSet<> ( this.rows.keySet () );

        for ( final Entry entry : entries )
        {
            Row row = this.rows.get ( entry.getId () );
            if ( row == null )
            {
                row = new Row ( this.itemFactory, entry.getId (), this.writeHandlerFactory );
                this.rows.put ( entry.getId (), row );
            }
            else
            {
                keys.remove ( entry.getId () );
            }

            row.update ( entry.getValues () );
        }

        // remove values that disappeared

        for ( final String key : keys )
        {
            final Row row = this.rows.remove ( key );
            if ( row != null )
            {
                row.dispose ();
            }
        }
    }

    public void dispose ()
    {
        for ( final Row row : this.rows.values () )
        {
            row.dispose ();
        }
        this.rows.clear ();
    }

}
