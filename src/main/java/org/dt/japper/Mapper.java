package org.dt.japper;

import java.sql.ResultSet;
import java.sql.SQLException;

/*
 * Copyright (c) 2012-2015, David Sykes and Tomasz Orzechowski
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
 * The interface of something that can map rows from a {@link ResultSet} to
 * Java POJOs of type {@code T}.
 *
 * @param <T> the type map {@link ResultSet} rows to
 */
public interface Mapper<T> {
  /**
   * Map the current row in {@link ResultSet} to a new instance of type
   * {@code T}.
   *
   * @param config the {@link JapperConfig} that was given to control query
   *               execution. The {@link Mapper} may use this to control
   *               aspects of the mapping for this specific query, e.g.
   *               limiting the maximum BLOB length that can be read.
   * @param rs the {@link ResultSet} to obtain the current row from
   * @param rowProcessor (Optional) {@link RowProcessor} that, when given,
   *                     will be allowed to process the resulting instance of
   *                     {@code T} before it is added to the query result set
   * @return the instance of {@code T} that the row was mapped to
   * @throws SQLException if any calls to {@link ResultSet} throw
   */
  T map(JapperConfig config, ResultSet rs, RowProcessor<T> rowProcessor) throws SQLException;
}
