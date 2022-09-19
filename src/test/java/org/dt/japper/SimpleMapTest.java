package org.dt.japper;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dt.japper.testmodel.DelegatedPartModel;
import org.dt.japper.testmodel.PartModel;
import org.dt.japper.testmodel.PartPriceModel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Copyright (c) 2012-2015, David Sykes and Tomasz Orzechowski
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

public class SimpleMapTest {

  private static TestData testData;
  
  @BeforeAll
  public static void setupDB() throws Exception {
    testData = new TestData();
    testData.create();
  }

  @AfterAll
  public static void clearDB() throws Exception {
    if (testData != null) testData.destroy();
  }

  private static final String SQL_PARTS =
        "   SELECT *"
      + "     FROM part"
      + " ORDER BY partno"
      ;
  
  @Test
  public void mapParts() throws Exception {
    Connection conn = testData.connect();
    
    List<PartModel> parts = Japper.query(conn, PartModel.class, SQL_PARTS);
    assertEquals(3, parts.size());
    
    PartModel part = parts.get(0);
    assertEquals("123456", part.getPartno());
    assertEquals("FAB", part.getPartType());
    
    conn.close();
  }

  @Test
  public void mapOnePart() throws Exception {
    Connection conn = testData.connect();
    
    PartModel part = Japper.queryOne(conn, PartModel.class, SQL_PARTS);
    assertEquals("123456", part.getPartno());
    assertEquals("FAB", part.getPartType());
    
    conn.close();
  }

  private static final String SQL_PARTS_IN_LIST_WITH_EXTRA =
          "   SELECT *"
        + "     FROM part"
        + "    WHERE partno in(:PART_LIST)"
        + "      AND partno != :LANGUAGE"
        + "      AND description != :LANGUAGE"
        + " ORDER BY partno"
          ;

  @Test
  public void mapPartsInListMultipleReferences() throws Exception {
    Connection conn = testData.connect();

    List<String> partsList = Arrays.asList("123456");

    List<PartModel> parts = Japper.query(conn, PartModel.class, SQL_PARTS_IN_LIST_WITH_EXTRA, "LANGUAGE", "EN", "PART_LIST", partsList);
    assertEquals(1, parts.size());

    PartModel part = parts.get(0);
    assertEquals("123456", part.getPartno());
    assertEquals("FAB", part.getPartType());

    conn.close();
  }

  private static final String SQL_PARTS_IN_LIST =
          "   SELECT *"
        + "     FROM part"
        + "    WHERE partno in(:PART_LIST)"
        + " ORDER BY partno"
          ;

  @Test
  public void mapPartsInList() throws Exception {
    Connection conn = testData.connect();

    List<String> partsList = Arrays.asList("123456", "123789");

    List<PartModel> parts = Japper.query(conn, PartModel.class, SQL_PARTS_IN_LIST, "PART_LIST", partsList);
    assertEquals(2, parts.size());

    PartModel part = parts.get(0);
    assertEquals("123456", part.getPartno());
    assertEquals("FAB", part.getPartType());

    PartModel part2 = parts.get(1);
    assertEquals("123789", part2.getPartno());
    assertEquals("BUY", part2.getPartType());

    conn.close();
  }

  @Test
  public void mapPartsInArray() throws Exception {
    Connection conn = testData.connect();

    List<PartModel> parts = Japper.query(conn, PartModel.class, SQL_PARTS_IN_LIST, "PART_LIST", new String[]{"123456", "123789"});
    assertEquals(2, parts.size());

    PartModel part = parts.get(0);
    assertEquals("123456", part.getPartno());
    assertEquals("FAB", part.getPartType());

    PartModel part2 = parts.get(1);
    assertEquals("123789", part2.getPartno());
    assertEquals("BUY", part2.getPartType());

    conn.close();
  }


  @Test
  public void mapPartsInListSingleValue() throws Exception {
    Connection conn = testData.connect();

    List<PartModel> parts = Japper.query(conn, PartModel.class, SQL_PARTS_IN_LIST, "PART_LIST", "123456");
    assertEquals(1, parts.size());

    PartModel part = parts.get(0);
    assertEquals("123456", part.getPartno());
    assertEquals("FAB", part.getPartType());

    conn.close();
  }

  @Test
  public void mapPartsInSet() throws Exception {
    Connection conn = testData.connect();

    Set<String> partsSet = new HashSet<String>( Arrays.asList("123456", "123789", "123456") );
    assertEquals(2, partsSet.size());

    List<PartModel> parts = Japper.query(conn, PartModel.class, SQL_PARTS_IN_LIST, "PART_LIST", partsSet);
    assertEquals(2, parts.size());

    PartModel part = parts.get(0);
    assertEquals("123456", part.getPartno());
    assertEquals("FAB", part.getPartType());

    PartModel part2 = parts.get(1);
    assertEquals("123789", part2.getPartno());
    assertEquals("BUY", part2.getPartType());

    conn.close();
  }

  private static final String SQL_PART_PRICES =
        "   SELECT part.partno part_partno, part.description part_description, currency.currency_code"
      + "        , currency.currency_symbol, currency.description currency_description"
      + "        , pricing.pricing_id pricing_pricing_id, pricing.description pricing_description"
      + "        , part_price.price "
      + "     FROM part_price"
      + "     JOIN part on (part.partno = part_price.partno)"
      + "     JOIN currency on (currency.currency_code = part_price.currency_code)"
      + "     JOIN pricing on (pricing.pricing_id = part_price.pricing_id)"
      + " ORDER BY part.partno, pricing.pricing_id"
      ;
  
  @Test
  public void mapPricesUsingDefaultMapper() throws Exception {
    Connection conn = testData.connect();
    
    List<PartPriceModel> prices = Japper.query(conn, PartPriceModel.class, SQL_PART_PRICES+" /*-codeGen*/ ");
    assertEquals(6, prices.size());
    
    PartPriceModel price = prices.get(0);
    PartModel part = price.getPart();
    assertEquals("123456", part.getPartno());
    assertEquals("EUR", price.getCurrency().getCurrencyCode());
    assertEquals("€", price.getCurrency().getCurrencySymbol());
    assertEquals(0, BigDecimal.valueOf(100).compareTo(price.getPrice()));
    
    conn.close();
  }
  
  @Test
  public void mapPrices() throws Exception {
    Connection conn = testData.connect();
    
    List<PartPriceModel> prices = Japper.query(conn, PartPriceModel.class, SQL_PART_PRICES);
    assertEquals(6, prices.size());
    
    PartPriceModel price = prices.get(0);
    PartModel part = price.getPart();
    assertEquals("123456", part.getPartno());
    assertEquals("EUR", price.getCurrency().getCurrencyCode());
    assertEquals("€", price.getCurrency().getCurrencySymbol());
    assertEquals(0, BigDecimal.valueOf(100).compareTo(price.getPrice()));
    
    conn.close();
  }

  private static final String SQL_DELEGATED_PARTS =
          "   SELECT part.*, part.partno delegated_id "
        + "     FROM part"
        + " ORDER BY partno"
          ;

  /**
   * Test case to reproduce the bug outlined here: https://github.com/sykesd/japper/issues/31
   * We keep this test to guard against regressions.
   *
   * @throws Exception
   */
  @Test
  public void mapDelegatedParts() throws Exception {
    Connection conn = testData.connect();

    List<DelegatedPartModel> parts = Japper.query(conn, DelegatedPartModel.class, SQL_DELEGATED_PARTS);
    assertEquals(3, parts.size());

    DelegatedPartModel part = parts.get(0);
    assertEquals("123456", part.getPartno());

    conn.close();
  }

}
