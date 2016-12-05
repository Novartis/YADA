/**
 * Copyright 2016 Novartis Institutes for BioMedical Research Inc.
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

import org.json.JSONObject;

import com.novartis.opensource.yada.YADARequest;

/**
 * @author David Varon
 * @since 8.4.0
 *
 */
public class HSQLdbAdaptor extends JDBCAdaptor {

  /**
   * 
   */
  public HSQLdbAdaptor() {
  }

  /**
   * @param yadaReq
   */
  public HSQLdbAdaptor(YADARequest yadaReq) {
    super(yadaReq);
  }

  /**
   * HyperSQL® requires the <code>COUNT</code> keyword to be quoted: <code>"COUNT"</code>
   * 
   * @param core the SQL to wrap in an outer count(*) query
   * @param filters a JSON object containing the WHERE criteria
   * @return StringBuffer of wrapped core sql including filters
   * @throws YADAAdaptorException when the query filters can't be converted into a WHERE clause
   */
  @Override
  public StringBuffer buildSelectCount(String core, JSONObject filters) throws YADAAdaptorException
  {
    StringBuffer sql = new StringBuffer(SQL_SELECT);
    sql.append(SQL_COUNT_ALL);
    sql.append(QUOTE + SQL_COUNT + QUOTE);
    sql.append(NEWLINE);
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
    if (this.yadaReq.getViewLimit() > -1)
    {
      sql.append(NEWLINE);
      sql.append(LIMIT + this.yadaReq.getViewLimit()); /* THIS IS A MYSQL SPECIFIC LINE */
    }
    return sql;
  }
}
