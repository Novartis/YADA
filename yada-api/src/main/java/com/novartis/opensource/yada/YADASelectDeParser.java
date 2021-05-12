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
package com.novartis.opensource.yada;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.ValuesList;
import net.sf.jsqlparser.util.deparser.SelectDeParser;

/**
 * A subclass of {@link net.sf.jsqlparser.util.deparser.SelectDeParser} with methods to account for JDBC-related columns and 
 * query-parsing-state management (i.e., flag setting/resetting)
 * @author David Varon
 *
 */
public class YADASelectDeParser extends SelectDeParser {

	/**
	 * Flag to mark when expression is aliased
	 */
	private boolean expressionHasAlias = false;
	/**
	 * Flag to mark when expression contains a jdbc parameter symbol
	 */
	private boolean hasJdbcParameter   = false;
	/**
	 * Flag to mark when select contains JOIN clause
	 */
	private boolean statementHasJoins = false;
	/**
	 * Flag to marke when select contains VALUES clause
	 */
	private boolean hasValues = false;
	/**
	 * List of columns associated to VALUES clause 
	 */
	private List<String> valueColumns = new ArrayList<>();
	/**
	 * List of expressions (markup) in VALUES clause
	 */
	private Map<String,ExpressionList> valueExprs = new HashMap<>();
	
	/**
	 * Default no-arg constructor
	 */
	public YADASelectDeParser() {}
	
	/**
	 * Creates a new instance, as well as sets vars for arguments.
	 * @param yadaExpressionDeParser the object for processing SQL expressions
	 * @param builder the container for expression processing metadata
	 */
	public YADASelectDeParser(YADAExpressionDeParser yadaExpressionDeParser,
			StringBuilder builder) {
		super(yadaExpressionDeParser,builder);
	}

	/**
	 * Sets flags as needed, then calls handler.
	 * @see net.sf.jsqlparser.util.deparser.SelectDeParser#visit(net.sf.jsqlparser.statement.select.SelectExpressionItem)
	 */
	@Override
	public void visit(SelectExpressionItem selectExpressionItem) {
		super.visit(selectExpressionItem);
		this.expressionHasAlias = selectExpressionItem.getAlias() != null;		
		YADAExpressionDeParser expDeParser = (YADAExpressionDeParser)this.getExpressionVisitor(); 
		this.hasJdbcParameter   = expDeParser.hasJdbcParameter();						
		((YADAExpressionDeParser)this.getExpressionVisitor()).setInExpression(true);
		handleSelectExpressionItem(selectExpressionItem);
	}
	
	@Override
	public void deparseJoin(Join join) {
	  super.deparseJoin(join);
	  this.statementHasJoins = true;
	}
	
	@Override
  public void visit(ValuesList valuesList) {
    super.visit(valuesList);
    if(this.statementHasJoins)
    {
      // access to valuesList :
      //  'alias'      : this is the "table" or CTE name of the values set (alias.name) or 'vals'
      //  'columnNames': array list of column names 'v' (i.e., vals.v)
      //  'multiExpressionList': the structure is 
      //      multiExpressionList.exprList[0] = (?v)
      this.hasValues = true;
      this.valueColumns.addAll(valuesList.getColumnNames());
      MultiExpressionList mel = valuesList.getMultiExpressionList();
//      this.valueExprs.put(key, value);
    }    
  }

	
	/**
	 * If the column in the expression has an alias and an associated JDBC parameter, the column is added to the 
	 * appropriate index, flags are subsequently reset.
	 * @param selectExpressionItem the select expression object to process
	 */
	public void handleSelectExpressionItem(SelectExpressionItem selectExpressionItem)
	{
		if(this.expressionHasAlias && this.hasJdbcParameter)
		{
			Column columnFromAlias = new Column();
			columnFromAlias.setColumnName(selectExpressionItem.getAlias().getName());
			((YADAExpressionDeParser)this.getExpressionVisitor()).getJdbcColumns().add(columnFromAlias);
			resetInExpression();
			resetHasJdbcParameter();
			resetExpressionHasAlias();
		}
	}
	
	/**
	 * Resets flag to false after processing an item.
	 */
	public void resetInExpression()
	{
		((YADAExpressionDeParser)this.getExpressionVisitor()).setInExpression(false);	
	}
	
	/**
	 * Resets flag to false after processing an item.
	 */
	public void resetExpressionHasAlias() 
	{
		this.expressionHasAlias = false;
	}
	
	/**
	 * Resets flag to false after processing an item.
	 */
	public void resetHasJdbcParameter()
	{
		((YADAExpressionDeParser)this.getExpressionVisitor()).setHasJdbcParameter(false);
		this.hasJdbcParameter = false;
	}

	/**
	 * Standard accessor
	 * @return the valueExpressionMap
	 * @since 9.3.6
	 */
  public Map<String, ExpressionList> getValuesExpressionMap() {
    return this.valueExprs;
  }
}
