package org.dt.japper;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;

import org.dt.japper.testmodel.Attachment;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReadBlobTest {
  private static final String SQL_INSERT_ATTACHMENT =
          " INSERT INTO attachment(id, mime_type, attachment) VALUES(:ID, :MIME_TYPE, :ATTACHMENT)"
          ;

  private static final String SQL_ATTACHMENT =
          " select id, mime_type, attachment "
          + "   from attachment "
          + "  where id = :ID"
          ;

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

  @Test
  void check_blob_limit_is_respected() throws Exception {
    Connection conn = testData.connect();

    insertTestAttachment(conn);

    Attachment attachment = Japper.queryOne(conn, Attachment.class, SQL_ATTACHMENT
            , "ID", 13
    );
    assertNotNull(attachment);

    // Lower the limit below the length of our actual blob
    // via the JapperConfig object
    try {
      Japper.queryOne(JapperConfig.maxBlob(25)
              , conn, Attachment.class, SQL_ATTACHMENT
              , "ID", 13
      );
      fail("Loaded BLOB larger than limit!");
    }
    catch (IllegalArgumentException iaEx) {
      // If we get here, all is well!
    }
  }

  private void insertTestAttachment(Connection conn) {
    byte[] blob = "MAGIC MAGIC MAGIC 30+ bytes long".getBytes(StandardCharsets.UTF_8);

    int rowsAffected = Japper.execute(conn, SQL_INSERT_ATTACHMENT
            , "ID", 13
            , "MIME_TYPE", "text/plain"
            , "ATTACHMENT", blob
    );
    assertEquals(1, rowsAffected);

  }
}
