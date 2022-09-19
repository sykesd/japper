package org.dt.japper.lob;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * Copyright (c) 2013-2022, David Sykes and Tomasz Orzechowski
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

public class BlobReader {

  private static final Log log = LogFactory.getLog(BlobReader.class);

  public static final long MAX_BLOB_LENGTH = getMaxBlobLength();


  /**
   * Read the value of columnIndex column from the current row of result set rs as a BLOB value
   * If the value is null or of zero length, return null
   * Otherwise return the value as a byte array
   * 
   * @param rs the result set to load the column value from
   * @param columnIndex the column to load the value from
   * @return the BLOB as a byte[]
   */
  public static byte[] read(ResultSet rs, int columnIndex) throws SQLException {
    Blob blob = rs.getBlob(columnIndex);
    if (blob == null) {
      return null;
    }
    
    long length = blob.length();
    if (length == 0L) {
      return null;
    }
    
    if (length > MAX_BLOB_LENGTH) {
      throw new IllegalArgumentException("Attempt to read a BLOB column whose value is > "+MAX_BLOB_LENGTH+" bytes. Japper cannot read such BLOB values");
    }
    
    return blob.getBytes(1L, (int) length);
  }

  private static final long DEFAULT_MAX_BLOB_LENGTH = 64 * 1024 * 1024L;   // 64MB - max BLOB size we support loading at once

  private static final Pattern REGEX_MAX_LENGTH = Pattern.compile("([0-9]+)([mM])?");

  private static long getMaxBlobLength() {
    try {
      String rawLength = System.getProperty("japper.blob.limit", "64M");
      return parseMaxBlobLength(rawLength);
    }
    catch (Exception ex) {
      log.error(String.format("Error reading BLOB size limit - using default of %d bytes", DEFAULT_MAX_BLOB_LENGTH));
      return DEFAULT_MAX_BLOB_LENGTH;
    }
  }

  static long parseMaxBlobLength(String rawLength) {
    if (rawLength == null) {
      return DEFAULT_MAX_BLOB_LENGTH;
    }

    Matcher matcher = REGEX_MAX_LENGTH.matcher(rawLength);
    if (matcher.matches()) {
      return Long.parseLong(matcher.group(1)) * 1024L * 1024L;
    }
    else if (log.isDebugEnabled()) {
      log.debug(String.format("Invalid length parameter: %s. Using default limit of %d bytes", rawLength, DEFAULT_MAX_BLOB_LENGTH));
    }

    return DEFAULT_MAX_BLOB_LENGTH;
  }

}
