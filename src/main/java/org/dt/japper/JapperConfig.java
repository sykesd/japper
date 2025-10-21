package org.dt.japper;

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
 * POSSIBILITY OF SUCH DAMAGE.
 *
 *
 */

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.dt.japper.lob.BlobReader;

/**
 * Configuration object for Japper queries.
 * <p>
 *   Holds configuration information that is used by {@link Japper} when
 *   executing queries.
 * </p>
 */
@API(status = Status.STABLE)
public class JapperConfig {
  /**
   * The default JDBC fetch size to use if no other value is specified via
   * a {@link JapperConfig} instance.
   */
  public static final int DEFAULT_FETCH_SIZE = 500;

  /**
   * Convenience factory method which creates a {@link JapperConfig} instance
   * with the given value for the JDBC fetch size (see {@link #fetchSize},
   * and the default value for {@link #maxBlobLength}.
   *
   * @param aSize the JDBC fetch size to use for this query
   * @return the new {@link JapperConfig} instance configured with the given
   *         fetch size
   */
  @API(status = Status.STABLE)
  public static JapperConfig fetchSize(int aSize) {
    return new JapperConfig(aSize, 0L);
  }

  /**
   * Convenience factory method which creates a {@link JapperConfig} instance
   * with the given value for the {@link #maxBlobLength}, and the default
   * value for {@link #fetchSize}.
   *
   * @param aLength the maximum length of BLOB to allow to be loaded by this
   *                query
   * @return the new {@link JapperConfig} instance configured with the given
   *         max BLOB length
   */
  public static JapperConfig maxBlob(long aLength) {
    return new JapperConfig(DEFAULT_FETCH_SIZE, aLength);
  }

  /**
   * The JDBC fetch size to use for this query.
   * <p>
   * Defaults to {@value DEFAULT_FETCH_SIZE}.
   */
  private int fetchSize = DEFAULT_FETCH_SIZE;

  /**
   * The maximum length of a BLOB field that we will read.
   * <p>
   * When reading BLOB fields through {@link Mapper}s, included generated ones,
   * the BLOB data is read fully into memory into a {@code byte[]}. To avoid
   * memory exhaustion, or at least reduce the risk of it, there is a limit to
   * the maximum size that will be read.
   * <p>
   * This defaults to {@code 0}, in which case the Java system property
   * {@code japper.blob.limit} is parsed. If that cannot be read, or is not
   * set, the default {@value BlobReader#DEFAULT_MAX_BLOB_LENGTH} is used. If
   * this property is set to anything {@code >0}, then this is the maximum to
   * be read, regardless of any value set via {@code japper.blob.limit}.
   * <p>
   * The value set via the Java system property {@code japper.blob.limit} can
   * only be set in increments of 1 megabyte (1024 * 1024). The value of the
   * property must be a positive integer followed by {@code m} or {@code M}.
   * For example, {@code -Djapper.blob.limit=32m} or
   * {@code -Djapper.blob.limit=27M}.
   */
  private long maxBlobLength;

  /**
   * Create a new {@link JapperConfig} instance, setting the JDBC fetch size
   * to {@code fetchSize}.
   *
   * @param fetchSize the JDBC fetch to set
   * @deprecated Use the convenience factory method {@link #fetchSize(int)}
   *             instead
   */
  @Deprecated
  @API(status = Status.DEPRECATED)
  public JapperConfig(int fetchSize) {
    setFetchSize(fetchSize);
  }

  /**
   * Create a new {@link JapperConfig} instance, setting the JDBC fetch size
   * to {@code fetchSize}, and BLOB field length limit to
   * {@code maxBlobLength}.
   *
   * @param fetchSize the JDBC fetch size to set
   * @param maxBlobLength the BLOB field length limit to set
   */
  public JapperConfig(int fetchSize, long maxBlobLength) {
    setFetchSize(fetchSize);
    setMaxBlobLength(maxBlobLength);
  }

  /**
   * Create a new {@link JapperConfig} instance with all default values.
   */
  public JapperConfig() {
    // use default fetch size
  }

  /**
   * Set the JDBC fetch size to use.
   *
   * @param fetchSize the JDBC fetch size to use
   */
  public void setFetchSize(int fetchSize) {
    this.fetchSize = fetchSize;
  }

  /**
   * Get the JDBC fetch size
   *
   * @return the JDBC fetch size
   */
  public int getFetchSize() {
    return fetchSize;
  }

  /**
   * Get the BLOB field length limit. Fields with values over this length
   * will throw an {@link IllegalArgumentException} during mapping, i.e. at
   * runtime.
   *
   * @return the BLOB field length limit
   */
  public long getMaxBlobLength() {
    return maxBlobLength;
  }

  /**
   * Set the BLOB field length limit, in bytes.
   *
   * @param maxBlobLength the BLOB field length limit, in bytes
   */
  public void setMaxBlobLength(long maxBlobLength) {
    this.maxBlobLength = maxBlobLength;
  }
}
