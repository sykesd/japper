package org.dt.japper.lob;

import junit.framework.Assert;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Unit test for BLOB limit test
 */
public class ConfigureBlobLimitTest {

  @Test
  public void parseLimitTest() {
    String rawLength = "17m";
    assertEquals(17*1024*1024L, BlobReader.parseMaxBlobLength(rawLength));

    rawLength = "bogus";
    assertEquals(64*1024*1024L, BlobReader.parseMaxBlobLength(rawLength));

    rawLength = null;
    assertEquals(64*1024*1024L, BlobReader.parseMaxBlobLength(rawLength));
  }

}
