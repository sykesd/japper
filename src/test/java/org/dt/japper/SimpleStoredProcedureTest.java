package org.dt.japper;

import java.math.BigDecimal;
import java.sql.Connection;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.dt.japper.Japper.*;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleStoredProcedureTest {
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

  private static final String SQL_CALL = "{ call do_something(:NAME, :MANGLED, :NAME_RANK) }";

  @Test
  public void callTest() throws Exception {
    Connection conn = testData.connect();

    CallResult callResult = Japper.call(conn, SQL_CALL, "NAME", "something", "MANGLED", out(String.class), "NAME_RANK", out(BigDecimal.class));
    assertEquals(new String("something"), callResult.get("MANGLED", String.class));
    assertEquals(new BigDecimal(5), callResult.get("NAME_RANK", BigDecimal.class));

    conn.close();
  }

  @Test
  public void callWithUnusedParameterTest() throws Exception {
    Connection conn = testData.connect();

    CallResult callResult = Japper.call(conn, SQL_CALL, "NAME", "something", "DUMMY", "value", "MANGLED", out(String.class), "NAME_RANK", out(BigDecimal.class));
    assertEquals(new String("something"), callResult.get("MANGLED", String.class));
    assertEquals(new BigDecimal(5), callResult.get("NAME_RANK", BigDecimal.class));

    conn.close();
  }

  @Test
  public void callWithTargetTypeTest() throws Exception {
    Connection conn = testData.connect();
    
    ProcResult result = Japper.call(conn, ProcResult.class, SQL_CALL, "NAME", "something", "MANGLED", out(String.class), "NAME_RANK", out(BigDecimal.class));
    assertEquals(new String("something"), result.getMangled());
    assertEquals(new BigDecimal(5), result.getNameRank());
    
    // Do it again so we can see the 2nd time performce
    result = Japper.call(conn, ProcResult.class, SQL_CALL, "NAME", "something", "MANGLED", out(String.class), "NAME_RANK", out(BigDecimal.class));
    assertEquals(new String("something"), result.getMangled());
    assertEquals(new BigDecimal(5), result.getNameRank());
    
    conn.close();
  }
}
