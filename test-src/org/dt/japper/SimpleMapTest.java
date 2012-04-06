package org.dt.japper;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

import org.dt.japper.testmodel.PartModel;
import org.dt.japper.testmodel.PartPriceModel;
import org.junit.BeforeClass;
import org.junit.Test;

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

public class SimpleMapTest {

  private static TestData testData;
  
  @BeforeClass
  public static void setupDB() throws Exception {
    testData = new TestData();
    testData.create();
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
}
