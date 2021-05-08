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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.YADAMarkupParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.SetStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.deparser.DeleteDeParser;

import org.apache.log4j.Logger;

/**
 * An implementation of {@link StatementVisitor} which currently handles
 * {@code SELECT}, {@code INSERT}, {@code UPDATE}, and {@code DELETE} statements
 * (but not {@code REPLACE}, {@code TRUNCATE}, or {@code DROP}, yet). The class
 * also indexes columns parsed out of statements into 3 categories:
 * <ul>
 * <li>any column referenced</li>
 * <li>any column mapped ot a JDBC parameter</li>
 * <li>any column referenced by an {@code IN} clause</li>
 * </ul>
 *
 * @author David Varon
 *
 */
public class Parser implements StatementVisitor {
  /**
   * Local logger handle
   */
  private static Logger l = Logger.getLogger(Parser.class);
  /**
   * Constant equal to: {@value}
   */
  public static final String COLUMNS = "columns";
  /**
   * Constant equal to: {@value}
   */
  public static final String JDBC_COLUMNS = "jdbcColumns";
  /**
   * Constant equal to: {@value}
   */
  public static final String IN_COLUMNS = "inColumns";
  /**
   * Constant equal to: {@value}
   */
  public static final String STATEMENT = "statement";
  /**
   * Constant equal to: {@value}
   */
  @SuppressWarnings("unused")
  private static final String JDBC_PARAMETER = "?";
  /**
   * Constant equal to: {@value}
   */
  public static final String TYPE = "type";

  /**
   * Constant value equal to: {@value}
   *
   * @since 4.0.0
   */
  public static final String SELECT = "SELECT";
  /**
   * Constant value equal to: {@value}
   *
   * @since 4.0.0
   */
  public static final String UPDATE = "UPDATE";
  /**
   * Constant value equal to: {@value}
   *
   * @since 4.0.0
   */
  public static final String INSERT = "INSERT";
  /**
   * Constant value equal to: {@value}
   *
   * @since 4.0.0
   */
  public static final String DELETE = "DELETE";
  /**
   * Constant value equal to: {@value}
   *
   * @since 4.0.0
   */
  public static final String CALL = "CALL";
  /**
   * Constant value equal to: {@value}
   *
   * @since 4.0.0
   */
  public static final String JDBC = "JDBC";
  /**
   * Constant value equal to: {@value}
   *
   * @since 4.0.0
   */
  public static final String REST = "REST";
  /**
   * Constant value equal to: {@value}
   *
   * @since 4.0.0
   */
  public static final String SOAP = "SOAP";
  /**
   * Constant value equal to: {@value}
   *
   * @since 4.0.0
   */
  public static final String FILE = "FILE";

  /**
   * Required jsqlparser class to execute parsing
   */
  private CCJSqlParserManager parserManager = new CCJSqlParserManager();
  /**
   * Instance var to hold Statement objects
   */
  private Statement statement = null;
  /**
   * Instance var to hold query type value
   */
  private String type = "";
  /**
   * Instance var to hold query type value
   */
  private String statementType = "";
  /**
   * Index of all SQL columns created by deparser
   */
  private ArrayList<Column> cols = new ArrayList<>();
  /**
   * Index of all SQL columns associated to JDBC parameters and created by the
   * deparser
   */
  private ArrayList<Column> jdbcCols = new ArrayList<>();
  /**
   * Index of all SQL columns found in IN clauses and created by the deparser
   */
  private ArrayList<Column> inCols = new ArrayList<>();
  /**
   * Index of all SQL in clauses in the query
   * @since 7.1.0
   */
  private Map<Column,InExpression> inExpressionMap = new HashMap<>();
  /**
   * Buffer used by jsqlparser to store SQL fragments
   */
  private StringBuilder buf = new StringBuilder();// new StringBuffer();
  /**
   * Local expression deparser
   */
  private YADAExpressionDeParser yedp = new YADAExpressionDeParser();

  /**
   * @return the type of query, in an array
   */
  public String[] getType() {
    return new String[] { this.type };
  }

  /**
   * @return the type of query
   */
  public String getStatementType() {
    return this.statementType;
  }

  /**
   * Standard mutator of variable
   *
   * @param type the type of query
   * @since 7.1.0
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Standard mutator of variable
   * @param statementType the type of query
   * @since 7.1.0
   */
  public void setStatementType(String statementType) {
    this.statementType = statementType;
  }

  /**
   * Evaluates {@code SELECT} statement, setting {@link #type} to
   * {@link #SELECT}, drilling into the select body, and indexing any columns
   * encountered.
   */
  @Override
  public void visit(Select select) {
    YADASelectDeParser selectVisitor = (YADASelectDeParser) getExpressionDeParser()
        .getSelectVisitor();
    select.getSelectBody().accept(selectVisitor);
    addColumnNamesToLists();
    setStatementType(SELECT);
  }

  /**
   * Evalates {@code DELETE} statement, setting {@link #type} to {@link #DELETE}
   * , deparsing the statement, and indexing any columns encountered.
   */
  @Override
  public void visit(Delete delete) {
    DeleteDeParser dp = new DeleteDeParser(getExpressionDeParser(),
        getStringBuffer());
    dp.deParse(delete);
    addColumnNamesToLists();
    setStatementType(DELETE);
  }

  /**
   * Evalates {@code UPDATE} statement, setting {@link #type} to {@link #UPDATE}
   * , deparsing the statement, and indexing any columns encountered. This
   * method also more actively handles expressions and functions encountered in
   * the statement body.
   */
  @Override
  public void visit(Update update) {
    List<Column> columns = update.getColumns();
    getColumnList().addAll(columns);
    for (int i = 0; i < columns.size(); i++) {
      Column column = columns.get(i);
      Expression expression = update.getExpressions().get(i);
      if (expression instanceof YADAMarkupParameter) {
        getJdbcColumnList().add(column);
      } else if (expression instanceof Function) {
        Function function = (Function) expression;
        ExpressionList paramList = function.getParameters();
        for (int j = 0; j < paramList.getExpressions().size(); j++) {
          if (paramList.getExpressions().get(j) instanceof YADAMarkupParameter) {
            getJdbcColumnList().add(column);
          }
        }
      }
    }
    if (update.getWhere() != null) {
      update.getWhere().accept(getExpressionDeParser());
    }
    addColumnNamesToLists();
    setStatementType(UPDATE);
  }

  /**
   * Evalates {@code INSERT} statement, setting {@link #type} to {@link #INSERT}
   * , deparsing the statement, and indexing any columns encountered. This
   * method also more actively handles expressions and functions encountered in
   * the statement body.
   */
  @Override
  public void visit(Insert insert) {
    if (insert.getItemsList() != null) {
      insert.getItemsList().accept(getExpressionDeParser());
      if (getExpressionDeParser().hasExpressionList) {
        l.debug("insert has expression list");
        getExpressionDeParser().hasExpressionList = false;
        // this insert has a 'VALUES' statement and an expressionList (?,?,?)
        // so add all the columns from the INSERT column list
        List<Expression> expressions = getExpressionDeParser().getExpressions();
        List<Column> columns = insert.getColumns();

        // if(null == columns)
        // {
        //   //TODO support blind inserts (no column list)
        // }
        // else
        // {
          getColumnList().addAll(columns);
          for (int i = 0; i < columns.size(); i++) {
            if (expressions.get(i) instanceof YADAMarkupParameter) {
              l.debug("Adding JDBC column [" + columns.get(i) + "] to list");
              getJdbcColumnList().add(columns.get(i));
            } else if (expressions.get(i) instanceof Function) {
              Function function = (Function) expressions.get(i);
              ExpressionList paramList = function.getParameters();
              for (int j = 0; j < paramList.getExpressions().size(); j++) {
                if (paramList.getExpressions().get(j) instanceof YADAMarkupParameter) {
                  getJdbcColumnList().add(columns.get(i));
                }
              }
            }
          }
        // }
      }
    } else {
      l.debug("insert has subselect");
      // this insert has subselect, i.e., INSERT into table [(..)] SELECT...
      // so all jdbc action will be handled by the ExpressionDeParser
      insert.getSelect().getSelectBody()
          .accept(getExpressionDeParser().getSelectVisitor());
      addColumnNamesToLists();
    }
    setStatementType(INSERT);
  }

  /**
   * {@code REPLACE} statements are not yet supported by the framework.
   */
  @Override
  public void visit(Replace replace) {
    try {
      throw new YADASQLException("REPLACE statements are not yet supported.");
    } catch (YADASQLException e) {
      l.error(e.getMessage(), e);
    }
  }

  /**
   * {@code DROP} statements are not yet supported by the framework.
   */
  @Override
  public void visit(Drop drop) {
    try {
      throw new YADASQLException("DROP statements are not yet supported.");
    } catch (YADASQLException e) {
      l.error(e.getMessage(), e);
    }
  }

  /**
   * {@code TRUNCATE} statements are not yet supported by the framework.
   */
  @Override
  public void visit(Truncate truncate) {
    try {
      throw new YADASQLException("TRUNCATE statements are not yet supported.");
    } catch (YADASQLException e) {
      l.error(e.getMessage(), e);
    }
  }

  /**
   * {@code CREATE} statements are not yet supported by the framework.
   */
  @Override
  public void visit(CreateTable createTable) {
    try {
      throw new YADASQLException("CREATE statements are not yet supported.");
    } catch (YADASQLException e) {
      l.error(e.getMessage(), e);
    }
  }

  /**
   * Submits the YADA marked-up code from the {@link YADAQuery} to the
   * {@link CCJSqlParserManager}
   *
   * @param code
   *          the YADA markup
   * @throws YADAParserException when the query fails to parse
   * @since 7.1.0
   */
  public void parseDeparse(String code) throws YADAParserException {
    try {
      this.setStatement(this.parserManager.parse(new StringReader(code)));
      this.deparse(this.getStatement());
    } catch (JSQLParserException e) {
      l.error(e.getCause());
      String msg = "The query was not parsable.  Other than the query being invalid, this could be because it is non-compliant, unsupported, or not SQL";
      l.error(msg + "\n" + code);
      throw new YADAParserException(msg, e);
    }
  }

  /**
   * The YADA parser implementation, calling
   * {@link net.sf.jsqlparser.parser.CCJSqlParserManager#parse(java.io.Reader)}
   * on the current query. It also builds the data structures used for column
   * indexing
   *
   * @param sql
   *          the statement to parse
   * @return a {@link java.util.Hashtable} containing 3 separate arrays of
   *         colums, plus the query type
   * @throws YADAParserException when an exception is encountered during the parsing operation
   * @deprecated as of 7.1.0
   */
  @Deprecated
  public Hashtable<String, String[]> parse(String sql)
      throws YADAParserException {
    Hashtable<String, String[]> colLists = new Hashtable<>();
    try {
      this.setStatement(this.parserManager.parse(new StringReader(sql)));

      processStatement(this.getStatement());

      String[] jdbcColumns = new String[getJdbcColumnList().size()];
      String[] columns = new String[getColumnList().size()];
      String[] inColumns = new String[getInColumnList().size()];
      for (int i = 0; i < this.cols.size(); i++) {
        columns[i] = this.cols.get(i).getColumnName().toUpperCase();
      }
      for (int i = 0; i < this.jdbcCols.size(); i++) {
        jdbcColumns[i] = this.jdbcCols.get(i).getColumnName().toUpperCase();
      }
      for (int i = 0; i < this.inCols.size(); i++) {
        inColumns[i] = this.inCols.get(i).getColumnName().toUpperCase();
      }
      colLists.put(TYPE, getType());
      colLists.put(COLUMNS, columns);
      colLists.put(JDBC_COLUMNS, jdbcColumns);
      colLists.put(IN_COLUMNS, inColumns);
    } catch (JSQLParserException e) {
      String msg = "The query was not parsable.  Other than the query being invalid, this could be because it is non-compliant, unsupported, or not SQL";
      l.error(msg + "\n" + sql);
      throw new YADAParserException(msg, e);
    } finally {
      if (l.isDebugEnabled()) {
        for (String list : colLists.keySet()) {
          String[] columns = colLists.get(list);
          for (String col : columns) {
            l.debug("[" + list + "] contains [" + col + "]");
          }
        }
      }
    }
    return colLists;
  }

  /**
   * Recurses through the object hierarchy to enable statement processing by
   * setting flags, populating lists, etc.
   * @param statement The {@link Statement} object to deparse
   * @since 7.1.0
   */
  private void deparse(Statement statement) {
    // set the deparser and the buffer
    getExpressionDeParser().setBuffer(getStringBuffer());
    // proceed to process the statement
    this.getStatement().accept(this);
  }

  /**
   * Kick off the recursive accept/visit deparsing action
   *
   * @param statementToProcess
   *          the deparsed query
   * @deprecated as of 7.1.0
   */
  @Deprecated
  private void processStatement(Statement statementToProcess) {
    // set the ivar
    this.setStatement(statementToProcess);
    // set the deparser and the buffer
    getExpressionDeParser().setBuffer(getStringBuffer());
    // proceed to process the statement
    this.getStatement().accept(this);
  }

  /**
   * Dump column metadata collected by deparsing into indices
   */
  private void addColumnNamesToLists() {
    YADAExpressionDeParser yedp = getExpressionDeParser();
    getColumnList().addAll(yedp.getColumns());
    getJdbcColumnList().addAll(yedp.getJdbcColumns());
    getInColumnList().addAll(yedp.getInColumnList());
    setInExpressionMap(yedp.getInExpressionMap());
  }

  /**
   * Standard accessor for variable
   *
   * @return the expression deparser
   */
  private YADAExpressionDeParser getExpressionDeParser() {
    return this.yedp;
  }

  /**
   * Standard accessor for variable
   *
   * @return the internal string buffer
   */
  private StringBuilder getStringBuffer() {
    return this.buf;
  }

  /**
   * Standard accessor for variable
   *
   * @return the column index
   */
  public ArrayList<Column> getColumnList() {
    return this.cols;
  }

  /**
   * Standard accessor for variable
   *
   * @return the jdbc column index
   */
  public ArrayList<Column> getJdbcColumnList() {
    return this.jdbcCols;
  }

  /**
   * Standard accessor for variable
   *
   * @return the "in" column index
   */
  public ArrayList<Column> getInColumnList() {
    return this.inCols;
  }

  /**
   * @return the statement
   * @since 7.0.0
   */
  public Statement getStatement() {
    return this.statement;
  }

  /**
   * @param statement
   *          the statement to set
   * @since 7.0.0
   */
  public void setStatement(Statement statement) {
    this.statement = statement;
  }

  /**
   * Standard accessor
   * @return the inExpressionMap
   * @since 7.1.0
   */
  public Map<Column,InExpression> getInExpressionMap() {
    return this.inExpressionMap;
  }

  /**
   * Standard mutator
   * @param inExpressionMap the inExpressionMap to set
   * @since 7.1.0
   */
  public void setInExpressionMap(Map<Column,InExpression> inExpressionMap) {
    this.inExpressionMap = inExpressionMap;
  }

  @Override
  public void visit(CreateIndex createIndex) {
    // nothing to do
  }

  @Override
  public void visit(CreateView createView) {
    // nothing to do
  }

  @Override
  public void visit(AlterView alterView) {
    // nothing to do
  }

  @Override
  public void visit(Alter alter) {
    // nothing to do
  }

  @Override
  public void visit(Statements stmts) {
    // nothing to do
  }

  @Override
  public void visit(Execute execute) {
    // nothing to do
  }

  @Override
  public void visit(SetStatement set) {
    // nothing to do
  }

  @Override
  public void visit(Merge merge) {
    // nothing to do
  }
}
