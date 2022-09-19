package org.dt.japper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */



/**
 * The contract for a class that can perform additional per-row processing
 * on a {@link ResultSet} in addition to {@link Japper}s usual property-mapping
 * magic.
 * <p>
 *   Providing a {@link org.dt.japper.RowProcessor} instance is optional, but when one is
 *   provided it will be called once per row of the <code>ResultSet</code> to allow for
 *   any further processing that cannot be performed as simple property mapping.
 * </p>
 * <p>
 *   The canonical use-case for this to allow for flex-field like function. We have a complex
 *   entity inside some system, let's call it <code>Project</code>. This <code>Project</code> has
 *   many static properties, which can all be mapped directly by Japper in a very performant way.
 * </p>
 * <p>
 *   However, our system allows the user to define a bunch of dynamic fields that may or may not exist
 *   on a given <code>Project</code> and are not even known at compile time.
 * </p>
 * <p>
 *   We can dynamically enhance our SQL statement (perhaps using {@link org.dt.japper.SqlBuilder}) at runtime
 *   to load some set of dynamic fields. We will map these fields to some sort of {@link java.util.Map} like
 *   data structure inside of our <code>Project</code>. We write a <code>RowProcessor</code> implementation
 *   that performs this mapping and pass an instance of it to <code>Japper</code>.
 * </p>
 * <p>
 *   <code>Japper</code> will call our <code>RowProcessor</code> instance after it has mapped all the static
 *   properties of each <code>Project</code> and we can then populate the <code>java.util.Map</code> with the
 *   dynamic properties.
 * </p>
 *
 * @author David Sykes
 *
 */
public interface RowProcessor<T> {
  /**
   * Prepare this instance of this row processor for the actual processing of all the
   * records returned in the {@link java.sql.ResultSet}
   * <p>
   *   This method is guaranteed to be called once and only once before {@link #process(Object, java.sql.ResultSet)} is called
   *   on the first row of the <code>ResultSet</code>.
   * </p>
   * <p>
   *   If the <code>ResultSet</code> is empty this method will still be called.
   * </p>
   * @param metaData the {@link java.sql.ResultSetMetaData} for the {@link ResultSet}
   */
  void prepare(ResultSetMetaData metaData) throws SQLException;

  /**
   * Process the current row in the given {@link ResultSet}.
   * <p>
   *   This method is called inside the main processing loop for the <code>ResultSet</code>, once for each
   *   row of the result. All directly mappable properties of the <code>model</code> object will already
   *   have been populated.
   * </p>
   *
   * @param model the model instance we are processing
   * @param rs the {@link ResultSet} containing the current row to process
   */
  void process(T model, ResultSet rs) throws SQLException;

  /**
   * Called once after the {@link java.sql.ResultSet} has been processed to allow for
   * any implementations to clean up any resources they may have allocated during processing.
   */
  void dispose();
}
