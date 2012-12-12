package org.dt.japper;

import java.math.BigDecimal;
import java.sql.Connection;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.dt.japper.Japper.out;

public class SimpleStoredProcedureTest {
  private static TestData testData;
  
  @BeforeClass
  public static void setupDB() throws Exception {
    testData = new TestData();
    testData.create();
  }
  
  private static final String SQL_CALL = "{ call do_something(:NAME, :MANGLED, :NAME_RANK) }";
  
  @Test
  public void callTest() throws Exception {
    Connection conn = testData.connect();
    
    CallResult callResult = Japper.call(conn, SQL_CALL, "NAME", "something", "MANGLED", out(String.class), "NAME_RANK", out(BigDecimal.class));
    assertEquals(new String("something"), callResult.get("MANGLED", String.class));
    assertEquals(new BigDecimal(5), callResult.get("NAME_RANK", BigDecimal.class));
    
    conn.close();
  }
}
