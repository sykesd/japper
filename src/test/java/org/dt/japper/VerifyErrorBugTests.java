package org.dt.japper;

import java.sql.Connection;
import java.util.List;

import org.dt.japper.testmodel.IntPricingModel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VerifyErrorBugTests {

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

  private static final String SQL_PRICING =
          "   SELECT *"
          + "     FROM pricing"
          + " ORDER BY pricing_id"
          ;

  @Test
  public void load_model_that_triggers_bug() throws Exception {
    Connection conn = testData.connect();

    List<IntPricingModel> pricings = Japper.query(conn, IntPricingModel.class, SQL_PRICING);
    assertEquals(2, pricings.size());

    IntPricingModel pricing = pricings.get(0);
    assertEquals(1, pricing.getPricingId());

    conn.close();
  }
}
