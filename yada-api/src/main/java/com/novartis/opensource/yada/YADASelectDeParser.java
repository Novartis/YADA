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
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.YADAMarkupParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
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
   * values expression for downstream expansion
   * @since 9.3.6
   */
  private ValuesList valuesList;
  /**
   * container of alias.column values for downstream reference
   * @since 9.3.6
   */
  private List<String> valuesColumns = new ArrayList<>();

	
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
	}
	
	@Override
  public void visit(ValuesList valuesList) {
    super.visit(valuesList);
    String alias = valuesList.getAlias().toString();
    List<String> columns = valuesList.getColumnNames();
    for(String column : columns)
    {
      this.getValuesColumns().add(String.format("%s.%s", alias, column));
    }
    this.setValuesList(valuesList);
    MultiExpressionList mel = valuesList.getMultiExpressionList();
    ExpressionList el = mel.getExprList().get(0);
    List<Expression> exprs = el.getExpressions();
    Table tab = new Table(alias);
    for(int i=0;i<exprs.size();i++)
    {
      if(exprs.get(i) instanceof YADAMarkupParameter)
      {
        Column columnFromValues = new Column();
        
        columnFromValues.setColumnName(columns.get(i));
        columnFromValues.setTable(tab);
        ((YADAExpressionDeParser)this.getExpressionVisitor()).getColumns().add(columnFromValues);
        ((YADAExpressionDeParser)this.getExpressionVisitor()).getJdbcColumns().add(columnFromValues);
      }
    }    
    // should only be called if select has JOIN clause
    // See line 349 of SelectDeParser.deparseJoins
    // access to valuesList :
    //  'alias'      : this is the "table" or CTE name of the values set (alias.name) or 'vals'
    //  'columnNames': array list of column names 'v' (i.e., vals.v)
    //  'multiExpressionList': the structure is 
    //      multiExpressionList.exprList[0] = (?v)
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
   * @return the valuesList
   * @since 9.3.6
   */
  public ValuesList getValuesList() {
    return valuesList;
  }

  /**
   * Standard mutator
   * @param valuesList the parsed out VALUES JOIN clause
   * @since 9.3.6
   */
  public void setValuesList(ValuesList valuesList) {
    this.valuesList = valuesList;
  }

  /**
   * Standard accessor
   * @return the {@link List} of {@link #valuesColumns}
   * @since 9.3.6
   */
  public List<String> getValuesColumns() {
    return valuesColumns;
  }

  /**
   * Standard mutator
   * @param valuesColumns the {@link List} returned by the parser
   * @since 9.3.6
   */
  public void setValuesColumns(List<String> valuesColumns) {
    this.valuesColumns = valuesColumns;
  }
}
