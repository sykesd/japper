package org.dt.japper.lob;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

/*
 * Copyright (c) 2013, David Sykes and Tomasz Orzechowski 
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

  public static final long MAX_BLOB_LENGTH = 32 * 1024 * 1024L;   // 32MB - max BLOB size we support loading at once
  
  /**
   * Read the value of columnIndex column from the current row of result set rs as a BLOB value
   * If the value is null or of zero length, return null
   * Otherwise return the value as a byte array
   * 
   * @param rs the result set to load the column value from
   * @param columnIndex the column to load the value from
   * @return the BLOB as a byte[]
   * @throws SQLException
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
  
}
