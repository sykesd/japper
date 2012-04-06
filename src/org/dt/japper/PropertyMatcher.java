package org.dt.japper;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
 * POSSIBILITY OF SUCH DAMAGE. 
 * 
 */

/**
 * Match the a column to a property in the given object graph
 * 
 * This is the workhorse for the automagic matching of columns to JavaBean properties. In order to 
 * discuss the rules for matching, some definitions will be required.
 * 
 * TT
 *    The Target Type we are mapping to.
 *    
 * P1..n
 *    A simple property on a JavaBean. Simple properties are the types that will be returned from
 *    the columns themselves, e.g. String, BigDecimal, Integer, Date, Timestamp, etc.
 *    
 * ST1..n
 *    A complex property, i.e. a Sub-Type of TTT, or one of its sub-types, recursively. A complex type
 *    is itself a JavaBean and can have further simple properties or even complex properties.
 *    
 * The following defintions all refer to column metadata. For the defintions we will assume
 * an example SQL statement of:
 *   select user.name as username from user
 *   
 * L
 *    The column label. In our example it is 'username'.
 *    
 * T
 *    The table name. In our example it is 'user'.
 *    
 * C
 *    The column name. In our example it is 'name'.
 *    
 * The column label/name is converted to lower camel case to match it to a property name, e.g. FIRST_NAME -> firstName
 * 
 * When looking for a property, the following rules are applied, in this order:
 * 
 * Direct Property Rule:
 * L = P?
 * The column label matches a simple property directly on the target type.
 * 
 * Direct Sub-Type Property Rule:
 * L = ST1...STn.P?
 * The column label references a simple property directly on a sub-type of the target type, by explicitly
 * referencing the path through the object graph to get to it.
 * References to the sub-types may uses abbreviations, e.g. PROD_NAME = product.name
 * References to the property name may not be abbreviated, e.g. PROD_NAME != productName
 * Ambiguous matches result in no match, e.g. if we have both productName, and product.name as potential 
 * properties, then PROD_NAME will NOT be matched.
 * 
 * Sub-Type Property Rule:
 * L = P? of ST?
 * The column label references a simple property directly on a sub-type, but does not specify ANY part of the
 * path through the object graph to get there, e.g. NAME = product.name
 * Ambiguous matches result in no match, e.g. if we have both product.name and currency.name then NAME will NOT 
 * be matched.
 * 
 * Column Name Direct Property Rule:
 * Same as for Direct Property Rule, but uses C instead of L. Only applied if L != C.
 * 
 * Column Name Sub-Type Property Rule:
 * Same as for Sub-Type Property Rule, but uses C instead of L. Only applied if L != C.
 * Same ambiguity clause applies.
 * 
 * Table.Label Sub-Type Direct Property Rule:
 * Like the Direct Sub-Type Property Rule, but uses T_L instead of L alone.
 * Abbreviations for sub-types are allowed. Same ambiguity clause applies.
 * This rule searches "bottom up", so to speak, e.g. part.currency.code = CURRENCY.CODE
 * 
 * Table.Column Sub-Type Direct Property Rule:
 * Like the Direct Sub-Type Property Rule, but uses T_C instead of L.
 * Abbreviations for sub-types are allowed. Same ambiguity clause applies.
 * This rule searches "bottom up", so to speak, e.g. part.currency.code = CURRENCY.CODE
 * 
 * NOTE: Ambiguous cases across rules are not checked. e.g. if we had a case that matched with both 
 * Sub-Type Property Rule and the Table.Column Direct Property Rule, we would use the match found
 * by the Sub-Type Property Rule, as this rule has precedence over the Table.Column Direct Property Rule
 * 
 * Usage:
 *   PropertyMatcher matcher = new PropertyMatcher(TargetType.class);
 *   for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
 *     PropertyDescriptor[] path = matcher.match(resultSetMetaData.getColumnLabel(i), resultSetMetaData.getTableName(i), resultSetMetaData.getColumnName(i));
 *     ...
 *   }
 *    
 * @author David Sykes
 *
 */
public class PropertyMatcher {

  private static final Log log = LogFactory.getLog(PropertyMatcher.class);
  
  private List<Property> properties;
  
  public PropertyMatcher(Class<?> type) {
    buildPropertyList(type);
    dumpPropertyList();
  }
  
  public PropertyDescriptor[] match(String columnLabel, String tableName, String columnName) {
    PropertyDescriptor[] path = null;
    
    path = matchDirectPropertyRule(columnLabel);
    if (path != null) {
      return path;
    }

    path = matchDirectSubTypePropertyRule(columnLabel);
    if (path != null) {
      return path;
    }
    
    path = matchSubTypePropertyRule(columnLabel);
    if (path != null) {
      return path;
    }
    
    if (columnName != null && !columnName.equalsIgnoreCase(columnLabel)) {
      // Column Name Direct Property Rule
      path = matchDirectPropertyRule(columnName);
      if (path != null) {
        return path;
      }

      // Column Name Sub-Type Property Rule
      path = matchSubTypePropertyRule(columnName);
      if (path != null) {
        return path;
      }
    }
    
    if (tableName != null) {
      // Table.Label Sub-Type Direct Property Rule
      path = matchTableLabelSubTypeDirectPropertyRule(tableName+"_"+columnLabel);
      if (path != null) {
        return path;
      }
      
      if (columnName != null && !columnName.equalsIgnoreCase(columnLabel)) {
        // Table.Column Sub-Type Direct Property Rule
        path = matchTableLabelSubTypeDirectPropertyRule(tableName+"_"+columnName);
        if (path != null) {
          return path;
        }
      }
    }
    
    return null;
  }
  
  private PropertyDescriptor[] matchDirectPropertyRule(String columnLabel) {
    for (Property property : properties) {
      if (property.depth > 1) continue;
      
      if (matchExact(property.name, 0, columnLabel, 0)) {
        return property.path;
      }
    }
    return null;
  }

  private PropertyDescriptor[] matchDirectSubTypePropertyRule(String columnLabel) {
    PropertyDescriptor[] path = null;
    
    for (Property property : properties) {
      if (property.depth == 1) continue;
      
      int lastDotPos = property.name.lastIndexOf('.');
      String prefix = property.name.substring(0, lastDotPos);
      String propertyName = property.name.substring(lastDotPos+1);
      
      int columnNameStartIndex = matchAbbreviated(prefix, columnLabel);
      if (columnNameStartIndex == -1) {
        // prefix doesn't match - check the next one
        continue;
      }
      
      if (matchExact(propertyName, 0, columnLabel, columnNameStartIndex)) {
        if (path != null) {
          // ambiguous match! - return no match
          // TODO add debug output showing the ambiguous matches
          return null;
        }
        
        path = property.path;
      }
    }
    
    return path;
  }

  private PropertyDescriptor[] matchSubTypePropertyRule(String columnLabel) {
    PropertyDescriptor[] path = null;
    
    for (Property property : properties) {
      if (property.depth == 1) continue;

      int lastDotPos = property.name.lastIndexOf('.');
      String propertyName = property.name.substring(lastDotPos+1);
      
      if (matchExact(propertyName, 0, columnLabel, 0)) {
        if (path != null) {
          // ambiguous match! - return no match
          // TODO add debug output showing the ambiguous matches
          return null;
        }
        
        path = property.path;
      }
    }
    
    return path;
  }

  private PropertyDescriptor[] matchTableLabelSubTypeDirectPropertyRule(String columnLabel) {
    PropertyDescriptor[] path = null;
    
    for (Property property : properties) {
      if (property.depth == 1) continue;

      int lastDotPos = property.name.lastIndexOf('.');
      String prefix = property.name.substring(0, lastDotPos);
      String propertyName = property.name.substring(lastDotPos+1);
      
      int columnNameStartIndex = matchAbbreviatedSuffix(prefix, columnLabel);
      if (columnNameStartIndex == -1) {
        // prefix doesn't match - check the next one
        continue;
      }
      
      if (matchExact(propertyName, 0, columnLabel, columnNameStartIndex)) {
        if (path != null) {
          // ambiguous match! - return no match
          // TODO add debug output showing the ambiguous matches
          return null;
        }
        
        path = property.path;
      }
    }
    
    return path;
  }
  
  private boolean matchExact(String propertyName, int pnStartIndex, String columnName, int cnStartIndex) {
    int pnIndex = pnStartIndex;
    boolean upperCase = false;
    for (int cnIndex = cnStartIndex; cnIndex < columnName.length(); cnIndex++) {
      char cch = columnName.charAt(cnIndex);
      if (cch == '_') {
        upperCase = true;
        continue;
      }
      
      if (pnIndex >= propertyName.length()) {
        // we have gone past the end of the property name - this doesn't match
        return false;
      }
      
      cch = upperCase ? Character.toUpperCase(cch) : Character.toLowerCase(cch);
      char pch = propertyName.charAt(pnIndex++);
      if (pch != cch) {
        return false;
      }
      
      upperCase = false;
    }
    
    return pnIndex >= propertyName.length();
  }
  
  private int matchAbbreviated(String propertyName, String columnName) {
    int pnIndex = 0;
    int cnIndex = 0;
    boolean upperCase = false;
    
    for (; cnIndex < columnName.length(); ) {
      char cch = columnName.charAt(cnIndex);
      if (cch == '_') {
        if (pnIndex >= propertyName.length()) {
          // we have matched this completely
          return cnIndex+1;
        }
        
        char pch = propertyName.charAt(pnIndex);
        if (pch == '.') {
          // this is an example of matching A_B -> a.b - we are ok, so consume the . and continue
          pnIndex++;
          cnIndex++;
          upperCase = false;
          continue;
        }
        
        /*
         * An underscore can either be a word break, or an object graph reference
         * So we need to skip over any lower case letters in the property name up until
         * the next object graph reference ('.') or the next upper case letter (word break) 
         */
        while (pnIndex < propertyName.length() && pch != '.' && !Character.isUpperCase(pch)) {
          pnIndex++;
          
          if (pnIndex >= propertyName.length()) {
            /*
             * We skipped right to the end of the property name
             * This is considered a match
             * TODO Check for degenerate cases, e.g. matching column name '_'
             */
            return cnIndex+1;
          }
          
          pch = propertyName.charAt(pnIndex);
        }
        
        if (pch != '.') {
          /*
           * We will try and match the underscore as a word - break, so 
           * we need to consume the underscore and match the next character
           * in the property name as an upper case character
           */
          upperCase = true;
          cnIndex++;
        }
      }
      else {
        cch = upperCase ? Character.toUpperCase(cch) : Character.toLowerCase(cch);
        char pch = propertyName.charAt(pnIndex++);
        if (pch != cch) {
          return -1;
        }
        
        upperCase = false;
        cnIndex++;
      }
    }
    
    return -1;
  }
  
  
  private int matchAbbreviatedSuffix(String propertyName, String columnName) {
    int pnIndex = 0;
    int cnIndex = 0;
    boolean upperCase = false;
    
    for (; cnIndex < columnName.length(); ) {
      char cch = columnName.charAt(cnIndex);
      
      if (cch != '_') {
        if (pnIndex >= propertyName.length()) {
          // we still have characters to match in the column name
          // but we have reached the end of the property name
          // this doesn't match
          return -1;
        }
        
        cch = upperCase ? Character.toUpperCase(cch) : Character.toLowerCase(cch);
        char pch = propertyName.charAt(pnIndex++);
        if (pch != cch) {
          /*
           * We haven't matched up to here - skip ahead to next '.' and try again
           * e.g. overall we are trying to match part.currency.displaySymbol to CURRENCY_DISPLAY_SYMBOL
           * so this method would have been called with part.currency and CURRENCY_DISPLAY_SYMBOL
           */
          int dotPos = propertyName.indexOf('.', pnIndex);
          if (dotPos < 0) {
            // there are no more '.' in the property name - this one doesn't match
            return -1;
          }

          // reset column name index and keep trying
          pnIndex = dotPos+1;
          cnIndex = 0;
        }        
        else {
          cnIndex++;
        }
        
        upperCase = false;
        continue;
      }
      
      
      // we have an underscore character
      if (pnIndex >= propertyName.length()) {
        // we have matched this completely
        return cnIndex+1;
      }
      
      char pch = propertyName.charAt(pnIndex);
      if (pch == '.') {
        // this is an example of matching A_B -> a.b - we are ok, so consume the . and continue
        pnIndex++;
        cnIndex++;
        upperCase = false;
        continue;
      }
      
      /*
       * An underscore can either be a word break, or an object graph reference
       * So we need to skip over any lower case letters in the property name up until
       * the next object graph reference ('.') or the next upper case letter (word break) 
       */
      while (pnIndex < propertyName.length() && pch != '.' && !Character.isUpperCase(pch)) {
        pnIndex++;
        
        if (pnIndex >= propertyName.length()) {
          /*
           * We skipped right to the end of the property name
           * This is considered a match
           * TODO Check for degenerate cases, e.g. matching column name '_'
           */
          return cnIndex+1;
        }
        
        pch = propertyName.charAt(pnIndex);
      }
      
      if (pch != '.') {
        /*
         * We will try and match the underscore as a word-break, so 
         * we need to consume the underscore and match the next character
         * in the property name as an upper case character
         */
        upperCase = true;
        cnIndex++;
      }
    }
    
    return -1;
  }
  
  private void dumpPropertyList() {
    for (Property property : properties) {
      log.debug(property.name+" : "+property.depth);
    }
  }
  
  private static class Property {
    private final String name;
    private PropertyDescriptor[] path;
    private final int depth;
    
    public Property(String name, PropertyDescriptor[] path) {
      this.name = name;
      this.path = path;
      this.depth = path.length;
    }
  }
  

  private void buildPropertyList(Class<?> type) {
    properties = new ArrayList<Property>();
    buildPropertyList(type, new Stack<PropertyDescriptor>());
  }
  
  private void buildPropertyList(Class<?> type, Stack<PropertyDescriptor> path) {
    String prefix = pathToPrefix(path);
    
    for (PropertyDescriptor descriptor : PropertyUtils.getPropertyDescriptors(type)) {
      if ("class".equals(descriptor.getName())) continue;
      
      path.push(descriptor);
      
      if (MapperUtils.isSimpleType(descriptor.getPropertyType())) {
        properties.add(new Property(prefix+descriptor.getName(), path.toArray(new PropertyDescriptor[]{})));
      }
      else {
        buildPropertyList(descriptor.getPropertyType(), path);
      }
      
      path.pop();
    }
  }
  
  private static String pathToPrefix(List<PropertyDescriptor> path) {
    StringBuilder sb = new StringBuilder();
    for (PropertyDescriptor descriptor : path) {
      sb.append(descriptor.getName()).append('.');
    }
    return sb.toString();
  }

  
  
}
