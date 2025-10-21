package org.dt.japper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/*
 * Copyright (c) 2012, David Sykes and Tomasz Orzechowski 
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

/**
 * A wrapper around a {@link ResultSet} and its underlying
 * {@link PreparedStatement} that can perform proper cleanup when done.
 * <p>
 * Some versions of the methods of {@link Japper} which actually execute
 * queries return an instance of this, and the caller is expected to read
 * the query results themselves, and then call {@link #close()} to ensure
 * the underlying results get cleaned up.
 */
@API(status = Status.STABLE)
public class QueryResult implements AutoCloseable {

  private final PreparedStatement ps;
  private final ResultSet rs;

  QueryResult(PreparedStatement ps, ResultSet rs) {
    this.ps = ps;
    this.rs = rs;
  }

  /**
   * Get the {@link ResultSet} in order to read the query results.
   *
   * @return the {@link ResultSet}
   */
  @API(status = Status.STABLE)
  public ResultSet getResultSet() { return rs; }

  @API(status = Status.STABLE)
  @Override
  public void close() {
    try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
    try { if (ps != null) ps.close(); } catch (SQLException ignored) {}
  }

}
