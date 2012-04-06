package org.dt.japper;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

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
  };
  
  public static boolean isSimpleType(Class<?> type) {
    for (Class<?> simpleType : SIMPLE_TYPES) {
      if (simpleType.equals(type)) return true;
    }
    return false;
  }
  
  
  
  /**
   * Utility to create a class of the given type
   * It is assumed the class has a public no-args constructor
   * If it doesn't an IllegalArgumentException will be thrown
   * 
   * This doesn't really do any more than call targetType.newInstance(), except
   * that it softens the exceptions to make calling code clearer
   * 
   * @param targetType the type we want a new instance of
   * @return a new instance of targetType
   */
  public static <T> T create(Class<T> targetType) {
    try {
      return targetType.newInstance();
    }
    catch (InstantiationException iEx) {
      throw new IllegalArgumentException("Type "+targetType.getName()+" does not have a default constructor!", iEx);
    }
    catch (IllegalAccessException iaEx) {
      throw new IllegalArgumentException("Type "+targetType.getName()+" does not have a default constructor!", iaEx);
    }
  }


  /**
   * Convert the given string to lowerCamelCase
   * Treat the _ as the word separator. Convert the first letter of all words
   * from the 2nd word on to upper case
   * 
   * There are some special cases:
   *    E_MAIL -> email, not eMail
   *  
   * @param s the string to convert to lowerCamelCase
   * @return the string converted to lowerCamelCase
   */
  public static String toLowerCamelCase(String s) {
    if (s == null) return "";
    s = s.toLowerCase();
    s = s.replace("e_mail", "email");   // special handling for E_MAIL, since eMail looks silly
    
    String[] parts = s.split("_");
    
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (String part : parts) {
      if (first) {
        sb.append(part);
        first = false;
      }
      else {
        sb.append(part.substring(0, 1).toUpperCase()).append(part.substring(1));
      }
    }
    
    return sb.toString();
  }

  /**
   * Find the first occurrence of _ in the given string
   * and split s at that point:
   * e.g. PART_DESCRIPTION -> { PART, DESCRIPTION }
   * PART -> { PART, null }
   * PART_CURRENCY_DESCRIPTION -> { PART, CURRENCY_DESCRIPTION }
   * _PART -> {"", PART}
   * 
   * @param s the string to split at the first occurrence of _
   * @return s split into 2 parts at the first occurrency of _, the _ is not returned in either part
   */
  public static String[] splitOnFirstWord(String s) {
    if (s == null) return new String[]{"", null};
    
    int underscorePos = s.indexOf('_');
    if (underscorePos < 0) {
      return new String[]{s, null};
    }
    
    return new String[]{ s.substring(0, underscorePos), s.substring(underscorePos+1) };
  }
}
