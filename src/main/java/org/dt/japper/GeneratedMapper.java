package org.dt.japper;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

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

public class GeneratedMapper {

  private static final int MAX_CACHED_MAPPERS = 100;
  
  /*
   * This is the cache of the generated mappers, which means we only have to generate them
   * once for each query. We only ever keep up to MAX_CACHED_MAPPERS in the cache. 
   * TODO Think about a way to allow configuration of this max. entry
   * 
   */
  private static final Map<Key, Mapper<?>> mapperCache = new LruCache<>(MAX_CACHED_MAPPERS);

  /**
   * Get a Mapper<T> implementation that can map the result of the given SQL (meta data in metaData)
   * to objects of the resultType, generating the code on the fly if necessary.
   * 
   * @param resultType the type we are mapping the results to
   * @param sql the SQL that generates the result
   * @param metaData the metaData for the given SQL
   * @return a Mapper<T> implementation to map results of the given query to resultType
   * @throws SQLException on an error during mapper code generation
   */
  public static <T> Mapper<T> get(Class<T> resultType, String sql, ResultSetMetaData metaData) throws SQLException {
    Key key = new Key(sql, resultType);
    
    @SuppressWarnings("unchecked")
    Mapper<T> mapper = (Mapper<T>) mapperCache.get(key);
    if (mapper == null) {
      mapper = MapperCodeGenerator.create(resultType, metaData);
      mapperCache.put(key, mapper);
    }
    
    return mapper;
  }
  
  
  
  
  private static class Key {
    private final String sql;
    private final Class<?> resultType;
    
    public Key(String sql, Class<?> resultType) {
      this.sql = sql;
      this.resultType = resultType;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((resultType == null) ? 0 : resultType.hashCode());
      result = prime * result + ((sql == null) ? 0 : sql.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      Key other = (Key) obj;
      if (resultType == null) {
        if (other.resultType != null) return false;
      }
      else if (!resultType.equals(other.resultType)) return false;
      if (sql == null) {
        if (other.sql != null) return false;
      }
      else if (!sql.equals(other.sql)) return false;
      return true;
    }
  }
  
}
