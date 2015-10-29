package com.novartis.opensource.yada.adaptor;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.novartis.opensource.yada.YADARequest;

/**
 * Early implementation on JDBCAdaptor extension for MSSQLServer.  There were performance issues, likely driver-related. 
 * It may work as is with a better driver. 
 * @author David Varon
 * @since PROVISIONAL
 */
public class SQLServerAdaptor extends JDBCAdaptor {
	/**
	 * Local logger handle
	 */
	private static Logger l = Logger.getLogger(SQLServerAdaptor.class);
	/**
	 * Default constructor
	 */
	public SQLServerAdaptor() {
		super();
		l.debug("Initializing");
	}
	
	/**
	 * Preferred "YADARequest" constructor
	 * @param yadaReq YADA request configuration
	 */
	public SQLServerAdaptor(YADARequest yadaReq) {
		super(yadaReq);
		l.debug("Initializing");
	}
	
	/**
	 * Uses SQLServer {@code ROW_NUMBER() OVER} syntax for pagination
	 * @see com.novartis.opensource.yada.adaptor.JDBCAdaptor#buildSelect(java.lang.String, java.lang.String, java.lang.String, int, int, org.json.JSONObject)
	 */
	@Override
	public StringBuffer buildSelect(String core, String sortKey, String sortOrder, int firstRow, int pageSize, JSONObject filters) throws YADAAdaptorException 
	{
		StringBuffer sql = new StringBuffer(SQL_SELECT_ALL);
		sql.append(SQL_FROM);
		sql.append("(");
		sql.append(NEWLINE+"  ");
		sql.append(SQL_SELECT);
		sql.append(SQL_CORE_ALIAS+"."+SQL_ALL);
		sql.append(", ");
		sql.append("ROW_NUMBER() OVER(ORDER BY (SELECT 1)) AS rnum"); /* THIS IS A SQLSERVER SPECIFIC LINE */
		sql.append(NEWLINE+"  ");
		sql.append(SQL_FROM);
		sql.append(" ("+NEWLINE);
		sql.append(core);
		if (null != sortKey && !sortKey.equals(""))
		{
			if (core.toUpperCase().indexOf(SQL_ORDER_BY) == -1)
			{
				sql.append(NEWLINE);
				sql.append(SQL_ORDER_BY);
			}
			else
			{
				sql.append(", ");
			}
			sql.append(sortKey);
			if (null != sortOrder && !sortOrder.equals(""))
			{
				 sql.append(" " + sortOrder);
			}
		}
		sql.append(NEWLINE+"       ) ");
		sql.append(SQL_CORE_ALIAS);
		sql.append(NEWLINE+"  ");
		if (filters != null)
		{
			sql.append(SQL_WHERE);
			sql.append(NEWLINE+"    ("+NEWLINE);
			sql.append(getQueryFilters(false));
			sql.append(NEWLINE+")"+NEWLINE);
		}
		sql.append(") "+SQL_WRAPPER_ALIAS);
		sql.append(NEWLINE);
		sql.append(SQL_WHERE);
		sql.append("rnum >= " + firstRow + " ");  					/* THIS IS A SQLSERVER SPECIFIC LINE */
		sql.append(SQL_AND);										/* THIS IS A SQLSERVER SPECIFIC LINE */
		sql.append(" rnum < " + String.valueOf(firstRow+pageSize)); /* THIS IS A SQLSERVER SPECIFIC LINE */
		return sql;
	}
	
	/**
	 * Uses SQLServer {@code ROW_NUMBER() OVER} syntax for {@code viewLimit}
	 * @see com.novartis.opensource.yada.adaptor.JDBCAdaptor#buildSelectCount(java.lang.String, org.json.JSONObject)
	 */
	@Override
	public StringBuffer buildSelectCount(String core, JSONObject filters) throws YADAAdaptorException
	{
		StringBuffer sql = new StringBuffer(SQL_SELECT+SQL_COUNT_ALL+SQL_COUNT);
		sql.append(NEWLINE);
		sql.append(SQL_FROM);
		sql.append(" (");
		sql.append(NEWLINE+"  ");
		sql.append(SQL_SELECT);
		sql.append(" ROW_NUMBER() OVER(ORDER BY (SELECT 1)) AS rnum");
		sql.append(NEWLINE+" ");
		sql.append(SQL_FROM);
		sql.append("  ("+NEWLINE);
		sql.append(core);
		sql.append(NEWLINE+"       ) ");
		sql.append(SQL_CORE_ALIAS);
		if (filters != null)
		{
			sql.append(NEWLINE+"  ");
			sql.append(SQL_WHERE);
			sql.append(getQueryFilters(false));
		}
		sql.append(") "+SQL_WRAPPER_ALIAS);
		sql.append(NEWLINE);
		if (this.yadaReq.getViewLimit() > -1)
		{
			sql.append(SQL_WHERE); 
			sql.append("rnum <=" + this.yadaReq.getViewLimit()); /*  THIS IS AN SQLSERVER SPECIFIC LINE */
		}
		return sql;
	}
}
