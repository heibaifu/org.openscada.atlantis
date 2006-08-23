package org.openscada.da.core.common.chained;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openscada.da.core.DataItemInformation;
import org.openscada.da.core.WriteAttributesOperationListener.Result;
import org.openscada.da.core.WriteAttributesOperationListener.Results;
import org.openscada.da.core.common.AttributeManager;
import org.openscada.da.core.common.DataItemBase;
import org.openscada.da.core.common.WriteAttributesHelper;
import org.openscada.da.core.data.AttributesHelper;
import org.openscada.da.core.data.Variant;

public abstract class DataItemBaseChained extends DataItemBase
{

    protected Map<String,Variant> _primaryAttributes = null;
    protected AttributeManager _secondaryAttributes = null;

    public DataItemBaseChained ( DataItemInformation di )
    {
        super ( di );
    }

    public synchronized void updateAttributes ( Map<String, Variant> attributes )
    {
        Map<String, Variant> diff = new HashMap <String, Variant> ();
        AttributesHelper.mergeAttributes ( _primaryAttributes, attributes, diff );
        
        if ( diff.size () > 0 )
            process ();
    }

    public Map<String, Variant> getAttributes ()
    {
        return _secondaryAttributes.get ();
    }
  
    public synchronized Results setAttributes ( Map<String, Variant> attributes )
    {
        Results results = new Results ();
        
        for ( BaseChainItem item : getChainItems () )
        {
            Results partialResult = item.setAttributes ( attributes );
            if ( partialResult != null )
            {
                for ( Map.Entry<String, Result> entry : partialResult.entrySet () )
                {
                    if ( entry.getValue ().isError () )
                    {
                        attributes.remove ( entry.getKey () );
                    }
                    results.put ( entry.getKey (), entry.getValue () );
                }
            }
        }
        
        process ();
        
        return WriteAttributesHelper.errorUnhandled ( results, attributes );
    }
    
    
    protected abstract void process ();
    
    /**
     * return a copy of the chain items
     * @return a copy of all chain items
     */
    protected abstract Collection<BaseChainItem> getChainItems ();


}