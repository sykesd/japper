package org.dt.japper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dt.japper.testmodel.PartModel;
import org.dt.japper.testmodel.PartPriceModel;
import org.junit.BeforeClass;
import org.junit.Test;

/*
 * Copyright (c) 2015, David Sykes and Tomasz Orzechowski
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

public class RowProcessorMapTest {

  private static class PartRowProcessor implements RowProcessor<PartModel> {

    private int ord;

    @Override
    public void prepare(ResultSetMetaData metaData) throws SQLException {
      for (int i = 1; i <= metaData.getColumnCount(); i++) {
        String name = metaData.getColumnName(i);
        if ("dyn_field".equalsIgnoreCase(name)) {
          ord = i;
          return;
        }
      }
      ord = -1;
    }

    @Override
    public void process(PartModel model, ResultSet rs) throws SQLException {
      Map<String, BigDecimal> flexFields = new HashMap<String, BigDecimal>();

      String dynField = rs.getString(ord);
      if (dynField != null && !dynField.trim().isEmpty()) {
        String[] parts = dynField.split(",");
        for (String part : parts) {
          String[] terms = part.split(":");
          flexFields.put(terms[0], new BigDecimal(terms[1]));
        }
      }

      model.setFlexFields(flexFields);
    }

    @Override
    public void dispose() {
    }
  }

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

    List<PartModel> parts = Japper.query(conn, PartModel.class, new PartRowProcessor(), SQL_PARTS);
    assertEquals(3, parts.size());

    PartModel part = parts.get(0);
    assertEquals("123456", part.getPartno());
    assertEquals("FAB", part.getPartType());
    assertNotNull(part.getFlexFields());
    assertEquals(10, part.getFlexFields().get("A").intValue());
    assertNull(part.getFlexFields().get("B"));

    part = parts.get(1);
    assertNotNull(part.getFlexFields());
    assertNull(part.getFlexFields().get("A"));
    assertEquals(20, part.getFlexFields().get("B").intValue());

    part = parts.get(2);
    assertNotNull(part.getFlexFields());
    assertEquals(20, part.getFlexFields().get("A").intValue());
    assertEquals(30, part.getFlexFields().get("B").intValue());

    conn.close();
  }

  @Test
  public void mapOnePart() throws Exception {
    Connection conn = testData.connect();

    PartModel part = Japper.queryOne(conn, PartModel.class, new PartRowProcessor(), SQL_PARTS);
    assertEquals("123456", part.getPartno());
    assertEquals("FAB", part.getPartType());
    assertNotNull(part.getFlexFields());
    assertEquals(10, part.getFlexFields().get("A").intValue());
    assertNull(part.getFlexFields().get("B"));

    conn.close();
  }

}
