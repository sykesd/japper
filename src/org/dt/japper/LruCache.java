package org.dt.japper;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

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
 * Implement a very simple, but (mostly) thread-safe LRU-cache
 * Designed to be used by the DynamicFactory class to avoid a memory-leak
 * by never throwing away a created mapper
 * 
 * NOTE: this is only mostly thread-safe. A leakage of the implementation detail
 * is that the backing store is a Collections.synchronizedMap(). But the instance
 * itself is not the object over which you should synchronize to iterate through 
 * the collections returned by keySet(), etc.
 * 
 * For now such iteration is not require. If it ever does become required we will
 * need to add a method that provides access to the object that needs to be used to
 * synchronize access.
 * 
 * @author David Sykes
 *
 * @param <K>
 * @param <V>
 */
class LruCache<K, V> implements Map<K, V> {

  private final int maxEntries;
  private final Map<K,V> cache;

  public LruCache(int maxEntries) {
    this.maxEntries = maxEntries;
    
    cache = Collections.synchronizedMap(new LinkedHashMap<K, V>(maxEntries+1, 1.0f, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return super.size() > LruCache.this.maxEntries;
      }
    });
  }

  @Override
  public int size() { return cache.size(); }

  @Override
  public boolean isEmpty() { return cache.isEmpty(); }

  @Override
  public boolean containsKey(Object key) { return cache.containsKey(key); }

  @Override
  public boolean containsValue(Object value) { return cache.containsValue(value); }

  @Override
  public V get(Object key) { return cache.get(key); }

  @Override
  public V put(K key, V value) { return cache.put(key, value); }

  @Override
  public V remove(Object key) { return cache.remove(key); }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    cache.putAll(m);
  }

  @Override
  public void clear() {
    cache.clear();
  }

  @Override
  public Set<K> keySet() { return cache.keySet(); }

  @Override
  public Collection<V> values() { return cache.values(); }

  @Override
  public Set<java.util.Map.Entry<K, V>> entrySet() { return cache.entrySet(); }
  
}
