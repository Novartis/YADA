/**
 * 
 */
package com.novartis.opensource.yada.plugin;

import java.lang.annotation.Annotation;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.RowSetProvider;

import org.apache.log4j.Logger;

import com.novartis.opensource.yada.Service;
import com.novartis.opensource.yada.YADAQuery;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADASecurityException;

/**
 * Determines if a default query parameter is a {@code plugin} and if so, 
 * if it refers to a {@link Preprocess} plugin with a {@link SecurityPreprocessor} 
 * annotation. 
 * @author varontron
 * @since 8.7.6
 *
 */
public class SecurityPluginDetector extends AbstractPostprocessor {
	/**
	 * Local logger handle
	 */
	private static Logger l = Logger.getLogger(Service.class);
	/**
	 * The {@code NAME} column
	 */
	private final static String NAME   = "NAME";
	/**
	 * The {@code VALUE} column
	 */
	private final static String VALUE  = "VALUE";
	/**
	 * The {@code SPP} or SecurityPreprocessor flag column
	 */
	private final static String SPP    = "SPP";
	/**
	 * The {@code pl} parameter
	 */
	private final static String PLUGIN = "pl";
	
	/**
	 * Creates a {@link javax.sql.rowset.CachedRowSet} from the {@link java.sql.ResultSet}
	 * stored in the {@link YADAQuery}, containing the results of the query for stored
	 * query parameters.  It then adds a column to the 
	 * {@link javax.sql.rowset.CachedRowSet} to store a boolean value as a string indicating
	 * if a) the parameter refers to a {@code plugin}, and b) if the referenced plugin has
	 * a {@link SecurityPreprocessor} annotation. 
	 */
	@Override
	public void engage(YADAQuery yq) throws YADAPluginException, YADASecurityException
	{
		super.engage(yq);

		try 
		{
			ResultSet    rs  = (ResultSet) yq.getResult().getResult(0);
			CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
			crs.populate(rs);
			RowSetMetaDataImpl crsmd = (RowSetMetaDataImpl) crs.getMetaData();
			crsmd.setColumnCount(6);
			crsmd.setColumnName(5, SPP);
			
			while(crs.next())
			{
				if(crs.getString(NAME).equals(PLUGIN))
				{
					String plugin = crs.getString(VALUE);
	  			Class<?> pluginClass;
	  			try 
	  			{
	  				pluginClass = plugin.indexOf(YADARequest.PLUGIN_PKG) > -1 
	  						? Class.forName(plugin) 
	  						: Class.forName(YADARequest.PLUGIN_PKG + "." + plugin);
	  				Annotation secPlugin = pluginClass.getAnnotation(SecurityPreprocessor.class);
	  				if(secPlugin != null && secPlugin instanceof SecurityPreprocessor)
	  				{
	  					crs.updateString(SPP, "true");
	  				}
	  				else
	  				{
	  					crs.updateString(SPP, "false");
	  				}
	  			} 
	  			catch (ClassNotFoundException e) 
	  			{
	  				String msg = "Could not find any plugin with the classname ["+plugin+"]"; 
						l.error(msg,e);
	  			}		
			  }
			}
			yq.getResult().setResult(0, crs);
		} 
		catch (SQLException e) 
		{
			String msg = "Unable to interrogate ResultSet";
			throw new YADAPluginException(msg,e);
		}
	}
}
