import java.sql.Connection;
import java.util.NoSuchElementException;

import org.dt.japper.Japper;
import org.dt.japper.JapperStreamingResult;
import org.dt.japper.TestData;
import org.dt.japper.testmodel.PartModel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


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
 * POSSIBILITY OF SUCH DAMAGE. * @author Administrator
 *
 *
 */

public class SimpleStreamingTest {

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

    try (JapperStreamingResult<PartModel> parts = Japper.streamableOf(conn, PartModel.class, SQL_PARTS)) {
      assertTrue(parts.hasNext());

      PartModel part = parts.next();
      assertEquals("123456", part.getPartno());
      assertEquals("FAB", part.getPartType());
    }

    conn.close();
  }

  @Test
  public void testMultipleHasNext() throws Exception {
    Connection conn = testData.connect();

    try (JapperStreamingResult<PartModel> parts = Japper.streamableOf(conn, PartModel.class, SQL_PARTS)) {
      assertTrue(parts.hasNext());
      assertTrue(parts.hasNext());
      assertTrue(parts.hasNext());

      PartModel part = parts.next();
      assertEquals("123456", part.getPartno());
      assertEquals("FAB", part.getPartType());
    }

    conn.close();
  }

  @Test
  public void testNoSuchElementException() throws Exception {
    Connection conn = testData.connect();

    try (JapperStreamingResult<PartModel> parts = Japper.streamableOf(conn, PartModel.class, SQL_PARTS)) {
      parts.next();
      parts.next();
      parts.next();

      try {
        parts.next();
        fail("We should have thrown a NoSuchElementException");
      }
      catch (NoSuchElementException nseEx) {
        // this is success!
      }
    }

    conn.close();
  }

  @Test
  public void testAsIterable() throws Exception {
    Connection conn = testData.connect();

    try (JapperStreamingResult<PartModel> parts = Japper.streamableOf(conn, PartModel.class, SQL_PARTS)) {
      for (PartModel part : parts) {
        assertNotNull(part);
      }
    }

    conn.close();
  }

  @Test
  public void testAsStream() throws Exception {
    Connection conn = testData.connect();

    try (JapperStreamingResult<PartModel> parts = Japper.streamableOf(conn, PartModel.class, SQL_PARTS)) {
      long count = parts.stream()
              .map(part -> part != null)
              .count()
              ;
      assertEquals(3, count);
    }

    conn.close();
  }


  @Test
  public void testRestartException() throws Exception {
    Connection conn = testData.connect();

    try (JapperStreamingResult<PartModel> parts = Japper.streamableOf(conn, PartModel.class, SQL_PARTS)) {
      parts.next();

      try {
        parts.forEach(part -> assertNotNull(part));
        fail("We were able to 'restart'!");
      }
      catch (IllegalStateException isEx) {
        // this is expected
      }
    }

    conn.close();
  }

}
