/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2006-2009 inavare GmbH (http://inavare.com)
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

package org.openscada.da.server.common.impl;

import java.util.Map;
import java.util.concurrent.Callable;

import org.openscada.core.Variant;
import org.openscada.da.core.WriteAttributeResults;
import org.openscada.da.server.common.DataItem;

public class WriteAttributesCallable implements Callable<WriteAttributeResults>
{
    private final DataItem item;

    private final Map<String, Variant> attributes;

    public WriteAttributesCallable ( final DataItem item, final Map<String, Variant> attributes )
    {
        this.item = item;
        this.attributes = attributes;
    }

    public WriteAttributeResults call () throws Exception
    {
        return this.item.setAttributes ( this.attributes );
    }

}
