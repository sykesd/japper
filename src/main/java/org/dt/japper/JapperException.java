package org.dt.japper;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.sql.Statement;

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
 * A {@link RuntimeException} subclass used when Japper operations catch
 * {@link SQLException}, or others, during their execution.
 */
public class JapperException extends RuntimeException {

  /**
   * Create a {@link JapperException} instance wrapping the given {@link BatchUpdateException}.
   * <p>
   * Try to figure out which set of parameters was the one that caused the error and
   * include that information in the exception message.
   *
   * @param buEx the {@link BatchUpdateException} thrown when executing the statement batch
   * @param batchSize the size of the batch that was executed
   * @return a {@link JapperException} wrapping the root cause, hopefully including which parameter set the error occurred on
   */
  public static JapperException fromBatchUpdate(BatchUpdateException buEx, int batchSize) {
    // try and figure out which statement was the one that caused the exception!
    int[] updateCounts = buEx.getUpdateCounts();
    if (updateCounts == null) {
      // we definitely can't determine the root cause here - fall back to default error report
      return new JapperException(buEx);
    }

    if (updateCounts.length < batchSize) {
      // this library/server-combo only returns the update counts for those statements that succeeded
      // this means that the updateCounts.length == index of bad parameter set
      int badIndex = updateCounts.length;
      return new JapperException("Occurred executing parameter set# " + badIndex, buEx);
    }

    // this library/combo hopefully reports the bad statement with the Statement.EXECUTE_FAILED value
    // look for it
    for (int index = 0; index < updateCounts.length; index++) {
      if (updateCounts[index] == Statement.EXECUTE_FAILED) {
        // Found it - report it in the error
        return new JapperException("Occurred executing parameter batch# " + index, buEx);
      }
    }

    // couldn't figure out the bad statement index - just fall back to the default exception
    return new JapperException(buEx);
  }

  /**
   * Create a {@link JapperException} instance with {@code cause} as the
   * underlying root cause.
   *
   * @param cause the cause of this {@link JapperException}
   */
  public JapperException(Throwable cause) {
    super("Error executing Japper query", cause);
  }

  /**
   * Create a {@link JapperException} instance with the given error message,
   * and {@code cause} as the underlying root cause.
   *
   * @param message the error message to include in the {@link JapperException}
   * @param cause the root cause to report
   */
  public JapperException(String message, Throwable cause) {
    super("Error executing Japper query: " + message, cause);
  }
}
