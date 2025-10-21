package org.dt.japper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


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


/**
 * A class wrapping the {@link ResultSet} of a {@link Japper} query allowing the caller
 * to stream over the results, rather than first build a list of all of the results.
 * <p>
 *   This object can be iterated over directly using the {@link #hasNext()}/{@link #next()} methods.
 * </p>
 * <p>
 *   Optionally, the caller may wish to iterate using the <code>for (T result : japperStreamingResult)</code> pattern.
 * </p>
 * <p>
 *   Finally, this object is also capable of returning a {@link Stream}.
 * </p>
 * <p>
 *   This {@link #close()} method must be called on this to ensure that the results are cleaned up correctly. This
 *   class implements {@link AutoCloseable} to enable use of the try-with-resources pattern to make this easier
 *   to ensure.
 * </p>
 * <pre style="code">
 *   try (JapperStreamingResult&lt;MyType&gt; result = Japper.streamableOf(conn, MyType.class, SQL_GET_MY_TYPES, "PARAM1", param1)) {
 *     result.stream()
 *        .filter(type -&gt; type.isThisTheDroneWeAreLookingFor())
 *        .forEach(type -&gt; System.out.println("We found drone " + type.getName()))
 *        ;
 *   }
 * </pre>
 * @param <T> the model type the results will be mapped to
 */
public class JapperStreamingResult<T> implements AutoCloseable, Iterator<T>, Iterable<T> {
  private final JapperConfig config;

  /**
   * The SQL {@link PreparedStatement} from which we originally got our result set
   */
  private final PreparedStatement ps;

  /**
   * The {@link ResultSet} from which we are streaming the results
   */
  private final ResultSet resultSet;

  /**
   * The {@link Mapper} to be used for mapping each record into
   * the wanted result type.
   */
  private final Mapper<T> mapper;

  /**
   * A {@link RowProcessor} to call for each row of the result
   */
  private final RowProcessor<T> rowProcessor;

  /**
   * Profile data about this execution
   */
  private final Japper.Profile profile;

  /**
   * Have we actually started streaming the result?
   */
  private boolean streamStarted;

  private boolean nextKnown;
  private T nextResult;

  JapperStreamingResult(
          JapperConfig config,
          PreparedStatement ps,
          ResultSet resultSet,
          Mapper<T> mapper,
          RowProcessor<T> rowProcessor,
          Japper.Profile profile
  ) {
    this.config = config;
    this.ps = ps;
    this.resultSet = resultSet;
    this.mapper = mapper;
    this.rowProcessor = rowProcessor;
    this.profile = profile;
  }

  /**
   * Obtain the {@link Stream} of {@code T} of the mapped query result rows.
   *
   * @return a {@link Stream} of query result rows mapped to instances
   *         of {@code T}
   */
  public Stream<T> stream() {
    return StreamSupport.stream(spliterator(), false);
  }

  @Override
  public Spliterator<T> spliterator() {
    return Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED | Spliterator.NONNULL);
  }

  @Override
  public Iterator<T> iterator() {
    if (streamStarted) {
      throw new IllegalStateException("Streaming of these results has already started. It cannot be re-started!");
    }

    return this;
  }


  @Override
  public boolean hasNext() {
    if (!nextKnown) {
      try {
        knowNext();
      }
      catch (SQLException sqlEx) {
        throw new JapperException(sqlEx);
      }
    }

    return nextResult != null;
  }

  @Override
  public T next() {
    if (!nextKnown) {
      try {
        knowNext();
      }
      catch (SQLException sqlEx) {
        throw new JapperException(sqlEx);
      }
    }

    if (nextResult == null) {
      throw new NoSuchElementException();
    }

    nextKnown = false;
    return nextResult;
  }

  private void knowNext() throws SQLException {
    streamStarted = true;

    nextResult = null;

    if (resultSet.next()) {
      profile.startMapRow();
      nextResult = mapper.map(config, resultSet, rowProcessor);
      profile.stopMapRow();
    }
    else {
      // we are at the end of the results
      profile.stopMap();
    }

    nextKnown = true;
  }

  @Override
  public void close() {
    try {
      profile.end();
      profile.log();
    }
    catch (Throwable ignoredExceptionDuringProfileLog) { }

    Throwable exceptionDuringDispose = null;
    if (rowProcessor != null) {
      try {
        rowProcessor.dispose();
      }
      catch (Throwable t) {
        exceptionDuringDispose = t;
      }
    }

    try { resultSet.close(); } catch (SQLException ignored) {}
    try { ps.close(); } catch (SQLException ignored) {}

    if (exceptionDuringDispose != null) {
      if (exceptionDuringDispose instanceof JapperException) {
        throw (JapperException) exceptionDuringDispose;
      }
      throw new JapperException(exceptionDuringDispose);
    }
  }

}
