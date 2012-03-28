package org.dt.japper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/*
 * Copyright (c) 2012, David Sykes and Tomasz Orzechowski 
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
 * POSSIBILITY OF SUCH DAMAGE. * @author Administrator
 * 
 * 
 */

/**
 * A helper class to create an in-memory HyperSQL database with some
 * known test data. This allows us to write some test cases with a 
 * known base set of data.
 * 
 * @author David Sykes
 *
 */
public class TestData {

  public Connection connect() throws SQLException {
    return DriverManager.getConnection("jdbc:hsqldb:mem:japperdb", "SA", "");
  }
  
  public void create() throws SQLException {
    Connection conn = connect();
    
    createTable(conn, SQL_PRICING_TABLE);
    insertData(conn, SQL_INSERT_PRICING, DATA_PRICING);
    
    createTable(conn, SQL_CURRENCY_TABLE);
    insertData(conn, SQL_INSERT_CURRENCY, DATA_CURRENCY);
    
    createTable(conn, SQL_PART_TABLE);
    insertData(conn, SQL_INSERT_PART, DATA_PART);
    
    createTable(conn, SQL_PART_PRICE_TABLE);
    insertData(conn, SQL_INSERT_PART_PRICE, DATA_PART_PRICE);
    
    conn.close();
  }
  
  
  private static String SQL_PRICING_TABLE =
        " CREATE TABLE pricing ("
      + "     pricing_id      NUMERIC(14) PRIMARY KEY"
      + "   , description     VARCHAR(60)"
      + " )"
      ;
  
  private static final String SQL_INSERT_PRICING =
      " INSERT INTO pricing(pricing_id, description) VALUES(?, ?)"
      ;
  private static final Object[][] DATA_PRICING = {
      { 1, "Default pricing" }
    , { 2, "Discounted pricing" }
  };

  
  private static String SQL_CURRENCY_TABLE =
        " CREATE TABLE currency ("
      + "     currency_code   VARCHAR(3) PRIMARY KEY"
      + "   , currency_symbol VARCHAR(6)"
      + "   , description     VARCHAR(60)"
      + " )"
      ;
  
  private static final String SQL_INSERT_CURRENCY =
      " INSERT INTO currency(currency_code, currency_symbol, description) VALUES(?, ?, ?)"
      ;
  
  private static final Object[][] DATA_CURRENCY = {
      { "AUD", "A$", "Australian Dollar" }
    , { "EUR", "€", "Euro" }
    , { "USD", "US$", "US Dollar" }
  };

  
  private static String SQL_PART_TABLE =
        " CREATE TABLE part ("
      + "     partno          VARCHAR(20) PRIMARY KEY"
      + "   , description     VARCHAR(60)"
      + "   , part_type       VARCHAR(3)"
      + " )"
      ;
  
  private static final String SQL_INSERT_PART =
      " INSERT INTO part(partno, description, part_type) VALUES(?, ?, ?)"
      ;
  
  private static final Object[][] DATA_PART = {
      { "123456", "Table, 50x50, Stainless steel", "FAB" }
    , { "123654", "Casters, Black, Plastic", "BUY" }
    , { "123789", "Door stop, Burgundy, Rubber", "BUY" }
  };

  
  private static String SQL_PART_PRICE_TABLE =
        " CREATE TABLE part_price ("
      + "     partno          VARCHAR(20)"
      + "   , pricing_id      NUMERIC(14)"
      + "   , currency_code   VARCHAR(3)"
      + "   , price           NUMERIC(12,2)"
      + "   , PRIMARY KEY (partno, pricing_id)"
      + " )"
      ;
  
  private static final String SQL_INSERT_PART_PRICE =
      " INSERT INTO part_price(partno, pricing_id, currency_code, price) VALUES(?, ?, ?, ?)"
      ;
  
  private static final Object[][] DATA_PART_PRICE = {
      { "123456", 1, "EUR", 100 }
    , { "123456", 2, "EUR", 93.25 }
    , { "123654", 1, "USD", 3.75 }
    , { "123654", 2, "USD", 3.51 }
    , { "123789", 1, "USD", 1.02 }
    , { "123789", 2, "USD", 0.98 }
  };

  

  private void createTable(Connection conn, String ddl) throws SQLException {
    Statement s = conn.createStatement();
    s.executeUpdate(ddl);
    s.close();
  }
  
  
  
  private void insertData(Connection conn, String sql, Object[][] data) throws SQLException {
    PreparedStatement ps = conn.prepareStatement(sql);
    try {
      for (Object[] record : data) {
        insertRecord(ps, record);
      }
    }
    finally {
      ps.close();
    }
  }
  
  private void insertRecord(PreparedStatement ps, Object[] record) throws SQLException {
    int index = 1;
    for (Object value : record) {
      if (value == null) {
        ps.setNull(index, Types.NULL);
      }
      else if (value instanceof String) {
        ps.setString(index, (String) value);
      }
      else if (value instanceof Integer) {
        ps.setInt(index, (Integer) value);
      }
      else if (value instanceof Double) {
        ps.setDouble(index, (Double) value);
      }
      
      index++;
    }
    
    ps.executeUpdate();
  }
}
