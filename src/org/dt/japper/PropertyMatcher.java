package org.dt.japper;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;

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
 * @author David Sykes
 * 
 */

public class PropertyMatcher {

  public static final PropertyDescriptor[] match(Class<?> type, String columnName) {
    if (columnName == null) return null;
    
    char ch = columnName.charAt(0);
    Path path = new Path(type, ch);
    if (path.hasFailed()) return null;
    
    boolean humpExpected = false;
    for (int index = 1; index < columnName.length(); index++) {
      ch = columnName.charAt(index);
      if (ch == '_') {
        humpExpected = true;
        continue;
      }
      
      if (!path.match(ch, humpExpected)) {
        return null;
      }
      
      humpExpected = false;
    }

    if (!path.matchOnEOS()) {
      return null;
    }
    
    List<PropertyDescriptor> thePath = path.buildPath();
    if (thePath == null || thePath.isEmpty()) {
      return null;
    }
    
    return thePath.toArray(new PropertyDescriptor[]{});
  }
  
  
  private static class Path {
    private PropertyDescriptor descriptor;
    
    private String name;
    private int index;
    
    private List<Path> path;
    
    public Path(Class<?> type, char ch) {
      buildPotentials(type, ch);
    }
    
    public Path(PropertyDescriptor descriptor) {
      this.descriptor = descriptor;
      this.name = descriptor.getName();
    }
    
    public boolean hasFailed() {
      return ( descriptor == null && (path == null || path.isEmpty()) ); 
    }
    
    public boolean match(char ch, boolean humpExpected) {
      if (path != null) {
        return filterPath(ch, humpExpected);
      }
      
      if (humpExpected) {
        while (!isNameMatched() && Character.isLowerCase(getChar())) {
          getAndConsumeChar();
        }
      }
      
      if (isNameMatched()) {
        if (canContinuePath()) {
          return continuePath(ch);
        }
        
        return false;
      }
      
      ch = humpExpected ? Character.toUpperCase(ch) : Character.toLowerCase(ch);
      return ch == getAndConsumeChar();
    }

    public boolean matchOnEOS() {
      if (path != null) {
        return filterPathOnEOS();
      }
      return isNameMatched();
    }
    
    public List<PropertyDescriptor> buildPath() {
      List<PropertyDescriptor> thePath = new ArrayList<PropertyDescriptor>();
      return buildPath(thePath);
    }

    
    private List<PropertyDescriptor> buildPath(List<PropertyDescriptor> thePath) {
      if (descriptor != null) {
        thePath.add(descriptor);
        if (path == null || path.isEmpty()) return thePath;    // reached the end of the path
      }
      
      if (path == null || path.size() != 1) {
        // we don't have an exact match!
        return null;
      }
      
      return path.get(0).buildPath(thePath);
    }
    
    
    private char getChar() {
      return name.charAt(index);
    }
    
    private char getAndConsumeChar() {
      return name.charAt(index++);
    }
    
    private boolean filterPath(char ch, boolean humpExpected) {
      for (Iterator<Path> iter = path.iterator(); iter.hasNext();) {
        Path aPath = iter.next();
        if (!aPath.match(ch, humpExpected)) {
          iter.remove();
        }
      }
      return !path.isEmpty();
    }
    
    private boolean filterPathOnEOS() {
      for (Iterator<Path> iter = path.iterator(); iter.hasNext();) {
        Path aPath = iter.next();
        if (!aPath.matchOnEOS()) {
          iter.remove();
        }
      }
      return !path.isEmpty();
    }
    
    private boolean isNameMatched() {
      return index >= name.length();
    }
    
    private boolean canContinuePath() {
      return !MapperUtils.isSimpleType(descriptor.getPropertyType());
    }
    
    private boolean continuePath(char ch) {
      buildPotentials(descriptor.getPropertyType(), ch);
      return !path.isEmpty();
    }
    
    private void buildPotentials(Class<?> type, char ch) {
      path = new ArrayList<Path>();
      for (PropertyDescriptor aDescriptor : PropertyUtils.getPropertyDescriptors(type)) {
        Path aPath = new Path(aDescriptor);
        if (aPath.match(ch, false)) {
          path.add(aPath);
        }
      }
    }
    
  }
  
  
  
}
