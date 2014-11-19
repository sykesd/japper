package org.dt.japper;

import junit.framework.Assert;
import org.dt.japper.testmodel.Attachment;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.charset.Charset;
import java.sql.Connection;

import static junit.framework.Assert.*;

/**
 * Unit test for the BLOB writing.
 *
 * Incidentally tests the reading of BLOB fields in order to actually assert that
 * the writing succeeded.
 *
 * @author David Sykes
 *
 */
public class WriteBlobTest {

  private static final String SQL_INSERT_ATTACHMENT =
          " INSERT INTO attachment(id, mime_type, attachment) VALUES(:ID, :MIME_TYPE, :ATTACHMENT)"
          ;

  /*
   * We disabled the code generator in the following SQL to avoid a java.lang.VerifyError
   * we were getting.
   */
  private static final String SQL_ATTACHMENT =
            " select /*-codeGen*/ id, mime_type, attachment "
          + "   from attachment "
          + "  where id = :ID"
          ;

  private static TestData testData;

  @BeforeClass
  public static void setupDB() throws Exception {
    testData = new TestData();
    testData.create();
  }

  @Test
  public void writeBlobTest() throws Exception {
    Connection conn = testData.connect();

    byte[] blob = "MAGIC".getBytes(Charset.forName("UTF-8"));

    int rowsAffected = Japper.execute(conn, SQL_INSERT_ATTACHMENT
            , "ID", Integer.valueOf(13)
            , "MIME_TYPE", "text/plain"
            , "ATTACHMENT", blob
            );
    assertEquals(1, rowsAffected);

    Attachment a = Japper.queryOne(conn, Attachment.class, SQL_ATTACHMENT, "ID", 13);
    assertNotNull(a);

    String s = new String(a.getAttachment(), Charset.forName("UTF-8"));
    assertEquals("MAGIC", s);
  }
}
