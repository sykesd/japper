package org.dt.japper;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

/*
 * Copyright (c) 2012-2025, David Sykes and Tomasz Orzechowski
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
 * Utility methods to help with the work of mapping query result fields to
 * Java POJO fields.
 */
public class MapperUtils {
  
  private static final Class<?>[] SIMPLE_TYPES = {
      int.class
    , Integer.class
    , long.class
    , Long.class
    , float.class
    , Float.class
    , double.class
    , Double.class
    , BigDecimal.class
    , String.class
    , Date.class
    , Timestamp.class
    , byte[].class
  };

  /**
   * Is the given Java {@link Class} considered "simple" by Japper, for the
   * purposes of mapping?
   * <p>
   * Simple types are those that are not themselves POJOs which require further
   * mapping.
   *
   * @param type the {@link Class} to check
   * @return {@code true} if it is a simple type
   */
  public static boolean isSimpleType(Class<?> type) {
    for (Class<?> simpleType : SIMPLE_TYPES) {
      if (simpleType.equals(type)) return true;
    }
    return false;
  }
  
  
  
  /**
   * Utility to create a class of the given type.
   * <p>
   * It is assumed the class has a public no-args constructor.
   * If it doesn't an {@link IllegalArgumentException} will be thrown.
   *
   * <p>
   * This doesn't really do any more than call {@code targetType.newInstance()}, except
   * that it softens the exceptions to make calling code clearer.
   * </p>
   *
   * @param targetType the type we want a new instance of
   * @return a new instance of {@code targetType}
   * @param <T> the type to create
   */
  public static <T> T create(Class<T> targetType) {
    try {
      return targetType.getDeclaredConstructor().newInstance();
    }
    catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException iEx) {
      throw new IllegalArgumentException("Type " + targetType.getName() + " does not have a default constructor!", iEx);
    }
  }

  /**
   * Like String.trim(), but only removes trailing whitespace
   * 
   * @param s the string to trim
   * @return the trimmed string
   */
  public static String trimRight(String s) {
    if (s == null || s.isEmpty()) return s;
    
    int lastNonSpace = -1;
    for (int index = s.length()-1; index >= 0; index--) {
      char ch = s.charAt(index);
      if (!Character.isWhitespace(ch)) {
        lastNonSpace = index;
        break;
      }
    }
    
    if (lastNonSpace == -1) {
      /*
       * s contains only whitespace
       */
      return "";
    }
    
    return s.substring(0, lastNonSpace+1);
  }
}
