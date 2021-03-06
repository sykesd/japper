package org.dt.japper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.util.*;

/*
 * Copyright (c) 2012-2016, David Sykes and Tomasz Orzechowski
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * - Neither the name David Sykes nor Tomasz Orzechowski may be used to endorse
 * or promote products derived from this software without specific prior written
 * permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
 *
 * 
 */


/**
 * This is basically a rip-off of the .NET Dapper mini-ORM
 * http://code.google.com/p/dapper-dot-net/
 * <p>
 * The idea is that the conversion from SQL query to object is "magic"
 * No configuration, annotations, or anything. It should just work
 * <p>
 * We assume SQL is far better at doing the data stuff than anything we could
 * come up with, and we just worry about munging a result set into a nice object
 * for us to work with
 * <p>
 *   <strong>Configuration</strong>
 * </p>
 * <p>
 *   When reading BLOB field values, Japper reads the entire BLOB into memory. As such it places
 *   a limit on the maximum size that can be read. The default is 64MB. However, you can configure
 *   this limit via a Java system property: <code>japper.blob.limit</code>. The number given is
 *   the maximum size in megabytes.
 * </p>
 *
 * @author David Sykes
 *
 */
public class Japper {
  
  private static final Log log = LogFactory.getLog(Japper.class);

  public static final JapperConfig DEFAULT_CONFIG = new JapperConfig();


  /**
   * Constant to indicate that no parameters are included/needed
   */
  public static final Object[] NO_PARAMS = {};

  /**
   * Helper method to assist in calling {@link #executeBatch(JapperConfig, Connection, String, List)}.
   * <p>
   * Is shorter and more convenient than having to type {@code Arrays.&lt;Object[]&gt;asList(...)}
   * </p>
   *
   * @param paramLists the array of {@code Object[]} parameter lists to put together into a single {@link List}
   * @return the {@link List} of parameter lists ({@code Object[]})
   */
  public static List<Object[]> paramLists(Object[]...paramLists) {
    if (paramLists == null) {
      return Collections.emptyList();
    }

    return Arrays.asList(paramLists);
  }

  /**
   * Helper method to assist in calling {@link #executeBatch(JapperConfig, Connection, String, List)}.
   * <p>
   * Together with {@link #paramLists(Object[]...)} makes it possible to construct the parameter lists
   * in-line. For example:
   * </p>
   * <pre style="code">
   *     Japper.executeBatch(..., paramLists(
   *         params("P1", "value1", "P2", 2)
   *       , params("P2", 3, "P1", "value1")
   *     ));
   * </pre>
   * @param params the parameter list
   * @return the parameter list as an {@code Object[]}
   */
  public static Object[] params(Object...params) {
    return params != null ? params : NO_PARAMS;
  }


  /**
   * Execute the given SQL query mapping the results to instances of <code>resultType</code>, returning the results
   * in a form that they can be streamed.
   * <p>
   *
   * @param conn the connection to execute the query on
   * @param resultType the result to map the query results to
   * @param sql the SQL statement to execute
   * @param params the parameters to the query
   * @param <T>
   * @return a {@link JapperStreamingResult} which allows the caller to treat the results as an {@link Iterable} or as a {@link java.util.stream.Stream}.
   */
  public static <T> JapperStreamingResult<T> streamableOf(Connection conn, Class<T> resultType, String sql, Object...params) {
    return streamableOf(DEFAULT_CONFIG, conn, resultType, null, sql, params);
  }


  /**
   * Execute the given SQL query mapping the results to instances of <code>resultType</code>, returning the results
   * in a form that they can be streamed.
   * <p>
   *   {@link #DEFAULT_CONFIG} will used for the configuration.
   * </p>
   *
   * @param config the {@link JapperConfig} to use when executing this query
   * @param conn the connection to execute the query on
   * @param resultType the result to map the query results to
   * @param sql the SQL statement to execute
   * @param params the parameters to the query
   * @param <T>
   * @return a {@link JapperStreamingResult} which allows the caller to treat the results as an {@link Iterable} or as a {@link java.util.stream.Stream}.
   */
  public static <T> JapperStreamingResult<T> streamableOf(JapperConfig config, Connection conn, Class<T> resultType, String sql, Object...params) {
    return streamableOf(config, conn, resultType, null, sql, params);
  }

  /**
   * Execute the given SQL query mapping the results to instances of <code>resultType</code>, returning the results
   * in a form that they can be streamed.
   * <p>
   *
   * @param config the {@link JapperConfig} to use when executing this query
   * @param conn the connection to execute the query on
   * @param resultType the result to map the query results to
   * @param rowProcessor an (optional) {@link org.dt.japper.RowProcessor} to perform additional per-row processing on the result
   * @param sql the SQL statement to execute
   * @param params the parameters to the query
   * @param <T>
   * @return a {@link JapperStreamingResult} which allows the caller to treat the results as an {@link Iterable} or as a {@link java.util.stream.Stream}.
   */
  public static <T> JapperStreamingResult<T> streamableOf(JapperConfig config, Connection conn, Class<T> resultType, RowProcessor<T> rowProcessor, String sql, Object...params) {
    Profile profile = new Profile(resultType, sql);

    boolean needsCleanup = true;

    PreparedStatement ps = null;
    try {
      ps = prepareSql(profile, config, conn, sql, params);

      profile.startQuery();
      ResultSetMetaData metaData = ps.getMetaData();
      if (rowProcessor != null) {
        rowProcessor.prepare(metaData);
      }
      logMetaData(metaData);

      ResultSet rs = ps.executeQuery();
      profile.stopQuery();

      profile.startMap();
      Mapper<T> mapper = getMapper(resultType, sql, metaData);
      profile.stopMapperCreation();

      needsCleanup = false;
      return new JapperStreamingResult<T>(ps, rs, mapper, rowProcessor, profile);
    }
    catch (SQLException sqlEx) {
      try {
        profile.end();
        profile.log();
      }
      catch (Throwable ignoredExceptionDuringProfileLog) { }

      throw new JapperException(sqlEx);
    }
    finally {
      if (needsCleanup) {
        Throwable exceptionDuringDispose = null;
        if (rowProcessor != null) {
          try {
            rowProcessor.dispose();
          }
          catch (Throwable t) {
            exceptionDuringDispose = t;
          }
        }

        try { if (ps != null) ps.close(); } catch (SQLException ignored) {}

        if (exceptionDuringDispose != null) {
          if (exceptionDuringDispose instanceof JapperException) {
            throw (JapperException) exceptionDuringDispose;
          }
          throw new JapperException(exceptionDuringDispose);
        }
      }
    }
  }



  /**
   * Execute the given SQL query on the given connection, mapping the result to the given
   * resultType.
   * <p>
   *   {@link #DEFAULT_CONFIG} will used for the configuration.
   * </p>
   *
   * @param conn the connection to execute the query on
   * @param resultType the result to map the query results to
   * @param rowProcessor an (optional) {@link org.dt.japper.RowProcessor} to perform additional per-row processing on the result
   * @param sql the SQL statement to execute
   * @param params the parameters to the query
   * @return the list of resultType instances containing the results of the query, or an empty list of the query returns no results
   */
  public static <T> List<T> query(Connection conn, Class<T> resultType, RowProcessor<T> rowProcessor, String sql, Object...params) {
    return query(DEFAULT_CONFIG, conn, resultType, rowProcessor, sql, params);
  }

  /**
   * Execute the given SQL query on the given connection, mapping the result to the given
   * resultType
   *
   * @param config the {@link JapperConfig} to use when executing this query
   * @param conn the connection to execute the query on
   * @param resultType the result to map the query results to
   * @param rowProcessor an (optional) {@link org.dt.japper.RowProcessor} to perform additional per-row processing on the result
   * @param sql the SQL statement to execute
   * @param params the parameters to the query
   * @return the list of resultType instances containing the results of the query, or an empty list of the query returns no results
   */
  public static <T> List<T> query(JapperConfig config, Connection conn, Class<T> resultType, RowProcessor<T> rowProcessor, String sql, Object...params) {
    Profile profile = new Profile(resultType, sql);

    List<T> result = new ArrayList<T>();

    PreparedStatement ps = null;
    try {
      ps = prepareSql(profile, config, conn, sql, params);

      profile.startQuery();
      ResultSetMetaData metaData = ps.getMetaData();
      if (rowProcessor != null) {
        rowProcessor.prepare(metaData);
      }
      logMetaData(metaData);

      ResultSet rs = ps.executeQuery();
      profile.stopQuery();

      profile.startMap();
      Mapper<T> mapper = getMapper(resultType, sql, metaData);
      profile.stopMapperCreation();

      while (rs.next()) {
        profile.startMapRow();
        result.add( mapper.map(rs, rowProcessor) );
        profile.stopMapRow();
      }
      profile.stopMap();

      profile.end();
      profile.log();

      return result;
    }
    catch (SQLException sqlEx) {
      try {
        profile.end();
        profile.log();
      }
      catch (Throwable ignoredExceptionDuringProfileLog) { }

      throw new JapperException(sqlEx);
    }
    finally {
      Throwable exceptionDuringDispose = null;
      if (rowProcessor != null) {
        try {
          rowProcessor.dispose();
        }
        catch (Throwable t) {
          exceptionDuringDispose = t;
        }
      }

      try { if (ps != null) ps.close(); } catch (SQLException ignored) {}

      if (exceptionDuringDispose != null) {
        if (exceptionDuringDispose instanceof JapperException) {
          throw (JapperException) exceptionDuringDispose;
        }
        throw new JapperException(exceptionDuringDispose);
      }
    }
  }


  /**
   * Execute the given SQL query on the given connection, mapping the result to the given
   * resultType
   * <p>
   *   {@link #DEFAULT_CONFIG} will used for the configuration.
   * </p>
   *
   * @param conn the connection to execute the query on
   * @param resultType the result to map the query results to
   * @param sql the SQL statement to execute
   * @param params the parameters to the query
   * @return the list of resultType instances containing the results of the query, or an empty list of the query returns no results
   */
  public static <T> List<T> query(Connection conn, Class<T> resultType, String sql, Object...params) {
    return query(DEFAULT_CONFIG, conn, resultType, null, sql, params);
  }

  /**
   * Execute the given SQL query on the given connection, mapping the result to the given
   * resultType
   *
   * @param config the {@link JapperConfig} to use when executing this query
   * @param conn the connection to execute the query on
   * @param resultType the result to map the query results to
   * @param sql the SQL statement to execute
   * @param params the parameters to the query
   * @return the list of resultType instances containing the results of the query, or an empty list of the query returns no results
   */
  public static <T> List<T> query(JapperConfig config, Connection conn, Class<T> resultType, String sql, Object...params) {
    return query(config, conn, resultType, null, sql, params);
  }


  /**
   * Execute the given SQL query on the given connection, mapping the result to the given
   * resultType. Return only the first result returned.
   * <p>
   * NOTE: at present this implementation of this is very naive. It simply calls query()
   * and then returns the first element of the returned list. It is assumed the caller
   * is not issuing a query that returns thousands of rows and then only wants the first one
   * <p>
   *   {@link #DEFAULT_CONFIG} will used for the configuration.
   * </p>
   *
   * @param conn the connection to execute the query on
   * @param resultType the result to map the query results to
   * @param rowProcessor an (optional) {@link org.dt.japper.RowProcessor} to perform additional per-row processing on the result
   * @param sql the SQL statement to execute
   * @param params the parameters to the query
   * @return the first result of the query mapped to a resultType instances, or null if the query returns no results
   */
  public static <T> T queryOne(Connection conn, Class<T> resultType, RowProcessor<T> rowProcessor, String sql, Object...params) {
    return queryOne(DEFAULT_CONFIG, conn, resultType, rowProcessor, sql, params);
  }

  /**
   * Execute the given SQL query on the given connection, mapping the result to the given
   * resultType. Return only the first result returned.
   * <p>
   * NOTE: at present this implementation of this is very naive. It simply calls query()
   * and then returns the first element of the returned list. It is assumed the caller
   * is not issuing a query that returns thousands of rows and then only wants the first one
   * <p>
   * @param config the {@link JapperConfig} to use when executing this query
   * @param conn the connection to execute the query on
   * @param resultType the result to map the query results to
   * @param rowProcessor an (optional) {@link org.dt.japper.RowProcessor} to perform additional per-row processing on the result
   * @param sql the SQL statement to execute
   * @param params the parameters to the query
   * @return the first result of the query mapped to a resultType instances, or null if the query returns no results
   */
  public static <T> T queryOne(JapperConfig config, Connection conn, Class<T> resultType, RowProcessor<T> rowProcessor, String sql, Object...params) {
    List<T> results = query(conn, resultType, rowProcessor, sql, params);
    if (results.size() > 0) {
      return results.get(0);
    }
    return null;
  }

  /**
   * Execute the given SQL query on the given connection, mapping the result to the given
   * resultType. Return only the first result returned.
   * <p>
   * NOTE: at present this implementation of this is very naive. It simply calls query()
   * and then returns the first element of the returned list. It is assumed the caller
   * is not issuing a query that returns thousands of rows and then only wants the first one
   * <p>
   *   {@link #DEFAULT_CONFIG} will used for the configuration.
   * </p>
   *
   * @param conn the connection to execute the query on
   * @param resultType the result to map the query results to
   * @param sql the SQL statement to execute
   * @param params the parameters to the query
   * @return the first result of the query mapped to a resultType instances, or null if the query returns no results
   */
  public static <T> T queryOne(Connection conn, Class<T> resultType, String sql, Object...params) {
    return queryOne(DEFAULT_CONFIG, conn, resultType, (RowProcessor<T>)null, sql, params);
  }



  /**
   * Execute the given SQL query on the given connection, returning the result as a
   * {@link QueryResult}.
   * <p>
   *   {@link #DEFAULT_CONFIG} will used for the configuration.
   * </p>
   *
   * @param conn the connection to execute the query on
   * @param sql the SQL statement to execute
   * @param params the parameters to the query
   * @return the result set as a {@link QueryResult}
   */
  public static QueryResult query(Connection conn, String sql, Object...params) {
    return query(DEFAULT_CONFIG, conn, sql, params);
  }

  /**
   * Execute the given SQL query on the given connection, returning the result as a
   * {@link QueryResult}.
   *
   * @param config the {@link JapperConfig} to use when executing this query
   * @param conn the connection to execute the query on
   * @param sql the SQL statement to execute
   * @param params the parameters to the query
   * @return the result set as a {@link QueryResult}
   */
  public static QueryResult query(JapperConfig config, Connection conn, String sql, Object...params) {
    Profile profile = new Profile(ResultSet.class, sql);
    
    PreparedStatement ps = null;
    try {
      ps = prepareSql(profile, config, conn, sql, params);
      
      profile.startQuery();
      ResultSetMetaData metaData = ps.getMetaData();
      logMetaData(metaData);
      
      ResultSet rs = ps.executeQuery();
      profile.stopQuery();
      

      profile.end();
      profile.log();
      
      return new QueryResult(ps, rs);
    }
    catch (SQLException sqlEx) {
      try {
        profile.end();
        profile.log();
      }
      catch (Throwable ignoredExceptionDuringProfileLog) { }

      try { if (ps != null) ps.close(); } catch (SQLException ignored) {}
      throw new JapperException(sqlEx);
    }
  }


  /**
   * Execute the given SQL statement on the given {@link Connection}, returning the
   * number of rows affected.
   * <p>
   * This method is designed for issuing UPDATE/DELETE or other non-query SQL statements
   * on the database, but taking advantage of all of the parameter parsing, setting and
   * conversions offered by Japper.
   * <p>
   *   {@link #DEFAULT_CONFIG} will used for the configuration.
   * </p>
   *
   * @param conn the connection to execute the query on
   * @param sql the SQL statement to execute
   * @param params the parameters to the query
   * @return the number of rows affected by the given statement
   */
  public static int execute(Connection conn, String sql, Object...params) {
    return execute(DEFAULT_CONFIG, conn, sql, params);
  }

  /**
   * Execute the given SQL statement on the given {@link Connection}, returning the
   * number of rows affected.
   * <p>
   * This method is designed for issuing UPDATE/DELETE or other non-query SQL statements
   * on the database, but taking advantage of all of the parameter parsing, setting and
   * conversions offered by Japper.
   *
   * @param config the {@link JapperConfig} to use when executing this query
   * @param conn the connection to execute the query on
   * @param sql the SQL statement to execute
   * @param params the parameters to the query
   * @return the number of rows affected by the given statement
   */
  public static int execute(JapperConfig config, Connection conn, String sql, Object...params) {
    Profile profile = new Profile("statement", int.class, sql);
    
    PreparedStatement ps = null;
    try {
      ps = prepareSql(profile, config, conn, sql, params);

      profile.startQuery();
      int rowsAffected = ps.executeUpdate();
      profile.stopQuery();
      
      profile.end();
      profile.log();
      
      return rowsAffected;
    }
    catch (SQLException sqlEx) {
      try {
        profile.end();
        profile.log();
      }
      catch (Throwable ignoredExceptionDuringProfileLog) { }

      throw new JapperException(sqlEx);
    }
    finally {
      try { if (ps != null) ps.close(); } catch (SQLException ignored) {}
    }
  }


  /**
   * Execute the given statement several times on the given {@link Connection} with a list of
   * parameters as a JDBC batch.
   * <p>
   * The main use-case for this method is for inserting a batch of records in the minimum number
   * of round-trips to the database.
   * <p>
   * This method is only useful for the case where you have exactly the same SQL statement to be
   * executed a number of times with a different set of parameters each time. That is, a single
   * {@link PreparedStatement} instance is created and {@link PreparedStatement#addBatch()} is
   * called for each set a parameters.
   * <p>
   *   {@link #DEFAULT_CONFIG} will used for the configuration.
   * </p>
   *
   * @param conn the connection to execute the query on
   * @param sql the SQL statement to execute
   * @param paramsList a {@link List} of parameter sets
   * @return the total number of rows affected by the batch
   */
  public static int executeBatch(Connection conn, String sql, List<Object[]> paramsList) {
    return executeBatch(DEFAULT_CONFIG, conn, sql, paramsList);
  }

  /**
   * Execute the given statement several times on the given {@link Connection} with a list of
   * parameters as a JDBC batch.
   * <p>
   * The main use-case for this method is for inserting a batch of records in the minimum number
   * of round-trips to the database.
   * <p>
   * This method is only useful for the case where you have exactly the same SQL statement to be
   * executed a number of times with a different set of parameters each time. That is, a single
   * {@link PreparedStatement} instance is created and {@link PreparedStatement#addBatch()} is
   * called for each set a parameters.
   *
   * @param config the {@link JapperConfig} to use when executing this query
   * @param conn the connection to execute the query on
   * @param sql the SQL statement to execute
   * @param paramsList a {@link List} of parameter sets
   * @return the total number of rows affected by the batch
   */
  public static int executeBatch(JapperConfig config, Connection conn, String sql, List<Object[]> paramsList) {
    Profile profile = new Profile("statement", int.class, sql);

    PreparedStatement ps = null;
    try {
      PreparedBatch batch = prepareSqlForBatch(profile, config, conn, sql);

      for (Object[] params : paramsList) {
        addToBatch(profile, batch, params);
      }

      profile.stopPrep();

      profile.startQuery();
      ps = batch.getStatement();
      int[] updateCounts = ps.executeBatch();
      profile.stopQuery();

      profile.end();
      profile.log();

      int rowsAffected = 0;
      for (int updateCount : updateCounts) {
        if (updateCount >= 0) {
          rowsAffected += updateCount;
        }
      }

      return rowsAffected;
    }
    catch (BatchUpdateException buEx) {
      try {
        profile.end();
        profile.log();
      }
      catch (Throwable ignoredExceptionDuringProfileLog) { }

      throw JapperException.fromBatchUpdate(buEx, paramsList.size());
    }
    catch (SQLException sqlEx) {
      try {
        profile.end();
        profile.log();
      }
      catch (Throwable ignoredExceptionDuringProfileLog) { }

      throw new JapperException(sqlEx);
    }
    finally {
      try { if (ps != null) ps.close(); } catch (SQLException ignored) {}
    }
  }

  /**
   * Execute a SQL statement on the given {@link Connection}, and map
   * the result of the call to the given target type.
   * <p>
   * If any of the parameter values are of type {@link OutParameter} then they
   * will be registered as IN OUT parameters and their values will be mapped
   * into the target type.
   * 
   * @param conn the connection to execute the query on
   * @param targetType the result to map the query results to
   * @param sql the SQL statement to execute
   * @param params the parameters to the query
   * @return an instance of target type with any OUT parameters mapped to its properties
   */
  public static <T> T call(Connection conn, Class<T> targetType, String sql, Object...params) {
    Profile profile = new Profile("call", int.class, sql);
    
    CallableStatement cs = null;
    try {
      CallResult callResult = new CallResult();
      cs = prepareCallSql(profile, conn, callResult, sql, params);

      profile.startQuery();
      cs.execute();
      profile.stopQuery();
      
      profile.startMap();
      T result = callResult.mapResults(cs, targetType);
      profile.stopMap();
      
      profile.end();
      profile.log();
      
      return result;
    }
    catch (SQLException sqlEx) {
      try {
        profile.end();
        profile.log();
      }
      catch (Throwable ignoredExceptionDuringProfileLog) { }

      throw new JapperException(sqlEx);
    }
    finally {
      try { if (cs != null) cs.close(); } catch (SQLException ignored) {}
    }
  }
  
  /**
   * Execute a SQL query or statement on the given {@link Connection}, and map
   * the result of the call to a {@link CallResult}.
   * <p>
   * If any of the parameter values are of type {@link OutParameter} then they
   * will be registered as IN OUT parameters and their return values will be 
   * available in the returned {@link CallResult}
   * 
   * @param conn the connection to execute the query on
   * @param sql the SQL statement to execute
   * @param params the parameters to the query
   * @return a {@link CallResult} with any OUT parameter values
   */
  public static CallResult call(Connection conn, String sql, Object...params) {
    Profile profile = new Profile("call", int.class, sql);
    
    CallableStatement cs = null;
    try {
      CallResult callResult = new CallResult();
      cs = prepareCallSql(profile, conn, callResult, sql, params);

      profile.startQuery();
      cs.execute();
      profile.stopQuery();
      
      profile.startMap();
      callResult.readResults(cs);
      profile.stopMap();
      
      profile.end();
      profile.log();
      
      return callResult;
    }
    catch (SQLException sqlEx) {
      try {
        profile.end();
        profile.log();
      }
      catch (Throwable ignoredExceptionDuringProfileLog) { }

      throw new JapperException(sqlEx);
    }
    finally {
      try { if (cs != null) cs.close(); } catch (SQLException ignored) {}
    }
  }
  
  /**
   * Convenience method for creating an {@link OutParameter} inline in a call to {@link Japper#call(Connection, Class, String, Object...) call}.
   * 
   * @param type the data type we expect this return value to have
   * @return the OutParameter instance to pass in the parameters to {@link #call(Connection, Class, String, Object...) call}
   */
  public static OutParameter out(Class<?> type) { return new OutParameter(type); }
  
  
  private static String version = null;
  
  public static String getVersion() {
    if (version != null) return version;
    
    try {
      Properties p = new Properties();
      InputStream in = Japper.class.getResourceAsStream("version.properties");
      if (in != null) {
        try {
          p.load(in);
          version = p.getProperty("version");
        }
        finally {
          try { in.close(); } catch (IOException ignored) {}
        }
      }
    }
    catch (Exception ignored) {}
    
    if (version == null) {
      version = "$development$";
    }
    
    return version;
  }
  
  
  
  
  private static CallableStatement prepareCallSql(Profile profile, Connection conn, CallResult callResult, String sql, Object...params) throws SQLException {
    profile.startPrep();
    ParameterParser parser = new ParameterParser(sql, params).parse();
    
    profile.setSql(parser.getSql());
    CallableStatement cs = conn.prepareCall(parser.getSql());

    if (params != null && params.length > 0) {
      profile.nextParamSet();
      for (ParameterParser.ParameterValue paramValue : parser.getParameterValues()) {
        profile.setParam(paramValue.getName(), paramValue.getValue(), paramValue.getFirstIndex());

        if (paramValue.getValue() instanceof OutParameter) {
          if (paramValue.getStartIndexes().size() != 1) throw new IllegalArgumentException("OUT parameter "+paramValue.getName()+" referenced multiple times!");
          OutParameter outP = (OutParameter) paramValue.getValue();
          callResult.register(cs, paramValue.getName(), outP.getType(), paramValue.getStartIndexes().get(0));
        }
        else {
          setParameter(cs, paramValue);
        }
      }
    }

    profile.stopPrep();
    return cs;
  }

  private static class PreparedBatch {
    private final PreparedStatement ps;
    private final ParameterParser parser;

    private PreparedBatch(PreparedStatement ps, ParameterParser parser) {
      this.ps = ps;
      this.parser = parser;
    }

    public PreparedStatement getStatement() {
      return ps;
    }

    public ParameterParser getParser() {
      return parser;
    }
  }

  private static PreparedBatch prepareSqlForBatch(Profile profile, JapperConfig config, Connection conn, String sql) throws SQLException {
    profile.startPrep();
    ParameterParser parser = new ParameterParser(sql, NO_PARAMS).parse(true);

    profile.setSql(parser.getSql());
    PreparedStatement ps = conn.prepareStatement(parser.getSql());

    ps.setFetchSize(config.getFetchSize());

    return new PreparedBatch(ps, parser);
  }

  private static void addToBatch(Profile profile, PreparedBatch batch, Object...params) throws SQLException {
    PreparedStatement ps = batch.getStatement();
    ps.clearParameters();

    ParameterParser parser = batch.getParser();
    parser.setNextParameterValueSet(params);

    if (params != null && params.length > 0) {
      profile.nextParamSet();
      for (ParameterParser.ParameterValue paramValue : parser.getParameterValues()) {
        profile.setParam( paramValue.getName(), paramValue.getValue(), paramValue.getFirstIndex() );
        setParameter(ps, paramValue);
      }
    }

    ps.addBatch();
  }

  private static PreparedStatement prepareSql(Profile profile, JapperConfig config, Connection conn, String sql, Object...params) throws SQLException {
    profile.startPrep();
    ParameterParser parser = new ParameterParser(sql, params).parse();
    
    profile.setSql(parser.getSql());
    PreparedStatement ps = conn.prepareStatement(parser.getSql());
    
    ps.setFetchSize(config.getFetchSize());
    
    if (params != null && params.length > 0) {
      profile.nextParamSet();
      for (ParameterParser.ParameterValue paramValue : parser.getParameterValues()) {
        profile.setParam( paramValue.getName(), paramValue.getValue(), paramValue.getFirstIndex() );
        setParameter(ps, paramValue);
      }
    }
    
    profile.stopPrep();
    return ps;
  }

  private static void setParameter(PreparedStatement ps, ParameterParser.ParameterValue paramValue) throws SQLException {
    List<Integer> indexes = paramValue.getStartIndexes();

    if (!paramValue.isListTypeValue()) {
      setParameter(ps, paramValue.getValue(), indexes);
      return;
    }

    /*
     * The value is a Collection or an array. Iterate over the values and set them at their appropriate index
     */
    Object rawValue = paramValue.getValue();
    if (rawValue instanceof Collection) {
      Collection<?> bag = (Collection<?>) rawValue;
      for (int startIndex : indexes) {
        int subIndex = 0;
        for (Object v : bag) {
          setParameter(ps, v, startIndex+subIndex);
          subIndex++;
        }
      }
    }
    else if (rawValue.getClass().isArray()) {
      for (int startIndex : indexes) {
        for (int subIndex = 0; subIndex < paramValue.getReplaceCount(); subIndex++) {
          setParameter(ps, java.lang.reflect.Array.get(rawValue, subIndex), startIndex+subIndex);
        }
      }
    }
  }

  private static String formatList(List<Integer> indexes) {
    StringBuilder out = new StringBuilder("[");
    String prefix = "";

    for (int index : indexes) {
      out.append(prefix).append(index);
      prefix = ",";
    }

    out.append("]");
    return out.toString();
  }

  private static void setParameter(PreparedStatement ps, Object value, List<Integer> indexes) throws SQLException {
    for (int index : indexes) {
      setParameter(ps, value, index);
    }
  }

  private static void setParameter(PreparedStatement ps, Object value, int index) throws SQLException {
    if (value == null) {
      ps.setNull(index, Types.CHAR);
    }
    else if (value instanceof String) {
      ps.setString(index, (String) value);
    }
    else if (value instanceof BigDecimal) {
      ps.setBigDecimal(index, (BigDecimal) value);
    }
    else if (value instanceof Integer) {
      ps.setInt(index, (Integer) value);
    }
    else if (value instanceof Integer) {
      ps.setInt(index, (Integer) value);
    }
    else if (value instanceof Long) {
      ps.setLong(index, (Long) value);
    }
    else if (value instanceof Double) {
      ps.setDouble(index, (Double) value);
    }
    else if (value instanceof Float) {
      ps.setFloat(index, (Float) value);
    }
    else if (value instanceof Calendar) {
      Calendar cal = (Calendar) value;
      Timestamp timestamp = new Timestamp(cal.getTimeInMillis()); 
      ps.setTimestamp(index, timestamp, cal);
    }
    else if (value instanceof Timestamp) {
      ps.setTimestamp(index, (Timestamp) value);
    }
    else if (value instanceof Date) {
      ps.setDate(index, new java.sql.Date(((Date) value).getTime()));
    }
    else if (value instanceof byte[]) {
      ps.setBlob(index, new ByteArrayInputStream((byte[]) value));
    }
  }

  /**
   * Get a Mapper implementation that can be used to map the results of the given SQL query
   * with the given meta data to the given type
   * 
   * @param resultType the type to map to
   * @param sql the query whose results we are going to map
   * @param metaData the meta data of the results of the query
   * @return a Mapper implementation
   * @throws SQLException 
   */
  public static <T> Mapper<T> getMapper(Class<T> resultType, String sql, ResultSetMetaData metaData) throws SQLException {
    // TODO Once we have a better feel for what the performance overhead of the code generation
    // is, and whether it provides a benefit for large result sets and/or multiple invocations of 
    // the same query, we can add some decision code in here to decide when to use the default,
    // reflective Mapper implementation, and when to generate a specific one dynamically.

    // For now we leave it up to the caller. Code generation is on by default, and can be disabled
    // via a specific comment inside the SQL query
    
    if (isUseGeneratedMapper(sql)) {
      return GeneratedMapper.get(resultType, sql, metaData);
    }

    return new DefaultMapper<T>(resultType, metaData);
  }
  
  /**
   * Check to see if the caller put in the Japper hint to disable code gen
   * in the query
   * 
   * @param sql the sql check
   * @return true if a generated mapper can be used, false otherwise
   */
  private static boolean isUseGeneratedMapper(String sql) {
    return !sql.contains("/*-codeGen*/");
  }
  
  
  
  private static void logMetaData(ResultSetMetaData metaData) throws SQLException {
    if (!log.isDebugEnabled()) return;
    
    log.debug("ResultSet Meta Data:");
    for (int i = 1; i <= metaData.getColumnCount(); i++) {
      log.debug("Column "+i+": "+metaData.getTableName(i)+"."+metaData.getColumnName(i)+" as "+metaData.getColumnLabel(i)+", type "+metaData.getColumnTypeName(i)+" ("+metaData.getColumnType(i)+")");
    }
  }

  
  static class Profile {
    static class ParamSet {
      private List<String> names = new ArrayList<String>();
      private List<String> values = new ArrayList<String>();
      private List<Integer> indexes = new ArrayList<Integer>();
    }

    private String dmlType;
    
    private Class<?> type;
    
    private long globalStart;
    private long globalEnd;
    
    private long prepStart;
    private long prepEnd;
    
    private long queryStart;
    private long queryEnd;
    
    private boolean mapped;
    private long mapStart;
    private long mapEnd;
    
    private long mapperCreationStart;
    private long mapperCreationEnd;
    
    
    private int rowCount;
    private long rowStart;
    private long mapFirstRowStart;
    private long mapFirstRowEnd;
    private long mappingTotal;
    
    private String originalSql;
    private String sql;
    
    private List<ParamSet> paramSets = new ArrayList<>();

    public Profile(Class<?> type, String originalSql) {
      this("query", type, originalSql);
    }
    
    public Profile(String dmlType, Class<?> type, String originalSql) {
      this.dmlType = dmlType;
      this.type = type;
      this.originalSql = originalSql;
      globalStart = System.nanoTime();
    }
    
    public void setSql(String sql) { this.sql = sql; }

    public void nextParamSet() {
      paramSets.add(new ParamSet());
    }

    public void setParam(String name, Object value, Integer firstIndex) {
      currentParamSet().names.add(name);
      currentParamSet().values.add(value == null ? "(null)" : value.toString());
      currentParamSet().indexes.add(firstIndex);
    }
    
    public void startPrep() { prepStart = System.nanoTime(); }
    
    public void stopPrep() { prepEnd = System.nanoTime(); }
    
    public void startQuery() { queryStart = System.nanoTime(); }
    
    public void stopQuery() { queryEnd = System.nanoTime(); }
    
    public void startMap() {
      mapped = true;
      mapStart = mapperCreationStart = System.nanoTime(); 
    }
    
    public void stopMapperCreation() { mapperCreationEnd = System.nanoTime(); }
    
    public void startMapRow() { rowStart = System.nanoTime();  }
    
    public void stopMapRow() {
      long rowEnd = System.nanoTime();
      rowCount++;
      
      if (rowCount == 1) {
        mapFirstRowStart = rowStart;
        mapFirstRowEnd = rowEnd;
      }
      
      mappingTotal += (rowEnd - rowStart);
    }
    
    public void stopMap() { mapEnd = System.nanoTime(); }

    public void end() { globalEnd = System.nanoTime(); }
    
    public void log() {
      log.info(dmlType+"("+type.getName()+"):");
      log.info("  original: "+originalSql);
      log.info("  executed: "+sql);
      if (log.isDebugEnabled() && !paramSets.isEmpty()) {
        boolean batch = paramSets.size() > 1;

        for (int setIndex = 0; setIndex < paramSets.size(); setIndex++) {
          ParamSet paramSet = paramSets.get(setIndex);
          String indent = "";
          if (batch) {
            log.debug("  param set: " + setIndex);
            indent = "  ";
          }
          for (int i = 0; i < paramSet.names.size(); i++) {
            log.debug(indent + "  " + paramSet.names.get(i) + " = " + paramSet.values.get(i) + " @ " + paramSet.indexes.get(i));
          }
        }
      }
      log.info("  preparation: "+nicify(prepStart, prepEnd));
      log.info("        query: "+nicify(queryStart, queryEnd));
      if (mapped) {
        log.info("          map: "+nicify(mapStart, mapEnd));
      }
      if (mapperCreationEnd != 0L) {
        log.info("                     row count: "+rowCount);
        log.info("               mapper creation: "+nicify(mapperCreationStart, mapperCreationEnd));
        log.info("                     first row: "+nicify(mapFirstRowStart, mapFirstRowEnd));
        log.info("                      avg. row: "+nicify((long) avgPerRow()));
      }
      log.info("Total: "+nicify(globalStart, globalEnd));
    }

    private ParamSet currentParamSet() {
      return paramSets.get(paramSets.size()-1);
    }

    private static final long MILLI_THRESHOLD = 10000000L;
    private static final long MICRO_THRESHOLD = 10000L;
    
    private String nicify(long start, long end) { return nicify(end - start); }
    
    private String nicify(long duration) {
      if (duration < 0L) {
        return "ERROR!";
      }

      if (duration > MILLI_THRESHOLD) {
        return Long.toString(duration / 1000000L)+"ms";
      }
      
      if (duration > MICRO_THRESHOLD) {
        return Long.toString(duration / 1000L)+"us";
      }
      
      return Long.toString(duration) + "ns";
    }
    
    private double avgPerRow() {
      return (double) mappingTotal / (double) rowCount;
    }
  }
}
