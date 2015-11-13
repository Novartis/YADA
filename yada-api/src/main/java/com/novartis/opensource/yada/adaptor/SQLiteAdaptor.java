/**
 * Copyright 2015 Novartis Institutes for BioMedical Research Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.novartis.opensource.yada.adaptor;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.novartis.opensource.yada.YADARequest;

/**
 * For connecting to MySQL databases via JDBC.
 * 
 * @author David Varon
 * @since 0.3.0.0
 */
public class SQLiteAdaptor extends JDBCAdaptor {
	
	/**
   * Local logger handle
   */
	private static Logger l = Logger.getLogger(SQLiteAdaptor.class);
	
	/**
	 * Default subclass constructor (calls {@code super()}
	 */
	public SQLiteAdaptor() {
		super();
		l.debug("Initializing");
	}
	
	/**
	 * Subclass constructor, calls {@code super(yadaReq)}
	 * @param yadaReq YADA request configuration
	 */
	public SQLiteAdaptor(YADARequest yadaReq)
	{
		super(yadaReq);
	}
	
	@Override
	protected void setDateParameter(PreparedStatement pstmt, int index, char type, String val) throws SQLException 
	{
	  if (EMPTY.equals(val) || val == null)
    {
      pstmt.setNull(index, java.sql.Types.INTEGER);
    }
	  else
	  {
	    SimpleDateFormat sdf     = new SimpleDateFormat(STANDARD_DATE_FMT);
      ParsePosition    pp      = new ParsePosition(0);
      Date             dateVal = sdf.parse(val,pp);
      int              secs    = (int) (dateVal.getTime()/1000L);
      pstmt.setInt(index,secs);
	  }
	}
}
