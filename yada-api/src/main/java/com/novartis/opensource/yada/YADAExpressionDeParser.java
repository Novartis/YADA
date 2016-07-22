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
/**
 * 
 */
package com.novartis.opensource.yada;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.YADAMarkupParameter;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SubSelect;

import org.apache.log4j.Logger;

/**
 * A subclass of net.sf.jsqlparser.util.deparser.ExpressionDeParser which is called during
 * com.novartis.opensource.yada.Adaptor processing of UPDATE statement WHERE clauses.
 * @author David Varon 
 */
public class YADAExpressionDeParser extends
		net.sf.jsqlparser.util.deparser.ExpressionDeParser {
	/**
	 * Local logger handle
	 */
	private static Logger l = Logger.getLogger(YADAExpressionDeParser.class);
	/**
	 * A java.util.ArrayList to store instances of {@link Column} found in the WHERE clause
	 */
	private ArrayList<Column>     columns  		    = new ArrayList<>();
	/**
	 * An index of columns referenced by SQL {@code IN} clauses
	 * @deprecated as of 7.1.0
	 */
	@Deprecated
  private ArrayList<Column>     inColumns		    = new ArrayList<>();
	/**
	 * An index of columns referenced by SQL {@code IN} clauses
	 */
	private List<Column>          inColumnList    = new ArrayList<>();
	/**
	 * An index of columns associated to JDBC parameter symbols
	 */
	private ArrayList<Column>     jdbcColumns       = new ArrayList<>();
	/**
	 * A list of SQL expressions
	 */
	private List<Expression>      expressions       = null;
	/**
   * A list of SQL "IN" expressions
   */
  private Map<Column,InExpression> inExpressionMap = new HashMap<>();
	/**
	 * A flag for managing query deparsing state
	 */
	public boolean 			          hasExpressionList = false;
	/**
	 * A flag for managing query deparsing state
	 */
	public boolean                hasSubSelect      = false;
	/**
	 * A flag for managing query deparsing state
	 * @deprecated as of 7.1.0
	 */
	@Deprecated
  private boolean               inExpression      = false;
	/**
   * A flag for managing query deparsing state
   */
  private boolean               insideExpression  = false;
	/**
	 * A flag for managing query deparsing state
	 */
	private boolean               inFunction          = false;
	/**
	 * A flag for managing query deparsing state
	 */
	private boolean               hasJdbcParameter  = false;
	/**
	 * A placeholder used when deparsing a binary expression
	 */
	private Column                pendingLeftColumn = null;
	
	/**
	 * A flag for managing query deparsing state, set to {@code true} when traversing an SQL expression. 
	 * @param insideExpression set to {@code true} if the deparser is handling an SQL expression.
	 */
	public void setInExpression(boolean insideExpression)
	{
		this.insideExpression = insideExpression;
	}
	
	/**
	 * A flag for managing query deparsing state, set to {@code true} when analyzing an expression that contains
	 * an instance of {@link net.sf.jsqlparser.expression.JdbcParameter}
	 * @param hasJdbcParameter set to {@code true} if the current expression includes a JDBC parameter symbol
	 */
	public void setHasJdbcParameter(boolean hasJdbcParameter)
	{
		this.hasJdbcParameter = hasJdbcParameter;
	}
	
	/**
	 * Returns the jdbc parameter status of the current expression.
	 * @return {@code true} if the current expression has a jdbc parameter
	 */
	public boolean hasJdbcParameter()
	{
		return this.hasJdbcParameter;
	}
	
	
	/**
	 *  Generic constructor, sets SelectDeParser and StringBuffer
	 */
	public YADAExpressionDeParser() 
	{
	  this.setBuffer(new StringBuilder());
		YADASelectDeParser selectVistitor = new YADASelectDeParser(this,this.getBuffer());
		this.setSelectVisitor(selectVistitor);
	}

	/**
	 * Inherited constructor, calls <code>super(SelectVisitor selectVisitor, StringBuffer buffer)</code>;
	 * @param selectVisitor the object for processing {@code SELECT} statements
	 * @param buffer the object in which to store deparsing metadata
	 */
	public YADAExpressionDeParser(SelectVisitor selectVisitor, StringBuilder buffer)
	{
		super(selectVisitor, buffer);
	}
	
	/**
	 * The key method of the visitor pattern.  When called in the deparsing process, adds each
	 * column it finds to the <code>columns</code> array list.
	 * 
	 * @param column the column encountered by the current handler
	 */
	@Override
	public void visit(Column column)
	{
		super.visit(column);
		if(this.insideExpression)
			this.pendingLeftColumn = column;
		this.columns.add(column);
	}
	
	/**
	 * @return List&lt;Expression&gt; of expressions deparsed from the statement
	 */
	public List<Expression> getExpressions()
	{
		return this.expressions;
	}
	
	/**
	 * Sets {@link #hasExpressionList} parameter to {@code true}, and indexes 
	 * expressions.
	 */
  @Override
	public void visit(ExpressionList expressionList)
	{
		super.visit(expressionList);
		l.debug("processing expression list");
		this.hasExpressionList = true;
		this.expressions = expressionList.getExpressions();
	}
	
	/**
	 * Sets {@link #hasSubSelect} flag to {@code true}.
	 */
	@Override
	public void visit(SubSelect subSelect)
	{
		super.visit(subSelect);
		this.hasSubSelect = true;
	}
	
	/**
	 * Sets {@link #insideExpression} flag to {@code true} and calls handler.
	 */
	@Override
	public void visit(InExpression in)
	{
		this.insideExpression = true;
		super.visit(in);
		handleInExpression(in);
	}
	
	/**
	 * Sets {@link #inFunction} flag to {@code true} and calls handler.
	 */
	@Override
	public void visit(Function f)
	{
		this.inFunction = true;
		super.visit(f);
		handleFunction(f);
	}
	
	/**
	 * Handler for extracting column names from functions if they map to JDBC parameters.
	 * @param f the current SQL function to evaluate
	 */
	public void handleFunction(Function f)
	{
		if(this.inFunction && this.hasJdbcParameter)
		{
			l.debug("Function contains jdbc parameter");	
		}
		else if(this.insideExpression && this.hasJdbcParameter)
		{
			l.debug("Function contains jdbc parameter");
			this.jdbcColumns.add(this.pendingLeftColumn);
		}
	}
	
	/**
	 * Handler for extracting the column on the left side of an SQL {@code in} clause
	 * @param in the SQL {@code in} clause to evaluate
	 */
	public void handleInExpression(InExpression in)
	{
		if (this.insideExpression && this.hasJdbcParameter)
		{
			this.jdbcColumns.add(this.pendingLeftColumn);
			this.inColumns.add(this.pendingLeftColumn);
			while(this.inColumnList.size() < this.jdbcColumns.size() - 1)
			{
			  this.inColumnList.add(null);
			}
			this.inColumnList.add(this.pendingLeftColumn);
			this.getInExpressionMap().put(this.pendingLeftColumn,in);
		}
		this.insideExpression = false;
		this.hasJdbcParameter = false;
		this.pendingLeftColumn = null;
	}
	
	/**
	 * 
	 */
	@Override
	public void visit(EqualsTo expr)
	{
		this.insideExpression = true;
		super.visit(expr);
		handleBinaryExpression(expr);
	}
	
	/**
	 * 
	 */
	@Override
	public void visit(NotEqualsTo expr)
	{
		this.insideExpression = true;
		super.visit(expr);
		handleBinaryExpression(expr);
	}
	
	/**
	 * 
	 */
	@Override
	public void visit(MinorThan expr)
	{
		this.insideExpression = true;
		super.visit(expr);
		handleBinaryExpression(expr);
	}
	
	/**
	 * 
	 */
	@Override
	public void visit(MinorThanEquals expr)
	{
		this.insideExpression = true;
		super.visit(expr);
		handleBinaryExpression(expr);
	}
	
	/**
	 * 
	 */
	@Override
	public void visit(GreaterThan expr)
	{
		this.insideExpression = true;
		super.visit(expr);
		handleBinaryExpression(expr);
	}
	
	/**
	 * 
	 */
	@Override
	public void visit(GreaterThanEquals expr)
	{
		this.insideExpression = true;
		super.visit(expr);
		handleBinaryExpression(expr);
	}
	
	/**
	 * 
	 */
	@Override
	public void visit(LikeExpression expr)
	{
		this.insideExpression = true;
		super.visit(expr);	
		handleBinaryExpression(expr);
	}
	
	/**
	 * @since 7.0.0
	 */
	@Override
	public void visit(YADAMarkupParameter expr)
	{
	  super.visit(expr);
	  this.hasJdbcParameter = true;
	}
	
	/**
	 * Handler to extract column names where appropriate (inside an expression which contains a jdbc parameter) and to set deparser flags.
	 * @param be the binary expression currently under evaluation
	 */
	public void handleBinaryExpression(BinaryExpression be)
	{
		if (this.insideExpression && this.hasJdbcParameter)
		{
			this.jdbcColumns.add(this.pendingLeftColumn);
		}
		this.insideExpression = false;
		this.hasJdbcParameter = false;
		this.pendingLeftColumn = null;
	}
	
	/**
	 * 
	 * @return java.util.ArrayList the <code>columns</code> java.util.ArrayList 
	 */
	public ArrayList<Column> getColumns()
	{
		return this.columns;
	}
	
	/**
	 * 
	 * @return java.util.ArrayList the <code>ins</code> java.util.ArrayList 
	 */
	public ArrayList<Column> getInColumns()
	{
		return this.inColumns;
	}
	
	/**
   * 
   * @return java.util.ArrayList the <code>ins</code> java.util.ArrayList 
   */
  public List<Column> getInColumnList()
  {
    return this.inColumnList;
  }
	
	/**
	 * @return java.util.ArrayList the <code>jdbcColumns</code> java.util.ArrayList 
	 */
	public ArrayList<Column> getJdbcColumns()
	{
		return this.jdbcColumns;
	}

  /**
   * @return the inExpressionMap
   */
  public Map<Column,InExpression> getInExpressionMap() {
    return this.inExpressionMap;
  }

  /**
   * @param inExpressionMap the inExpressionMap to set
   */
  public void setInExpressionMap(Map<Column,InExpression> inExpressionMap) {
    this.inExpressionMap = inExpressionMap;
  }
	
	

}
