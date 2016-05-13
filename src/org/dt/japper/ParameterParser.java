package org.dt.japper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Array;
import java.util.*;

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
 * 
 */


public class ParameterParser {

  public class ParameterValue {
    private final String name;
    private final Object value;
    private final int replaceCount;
    private final List<Integer> startIndexes = new ArrayList<Integer>();

    private ParameterValue(String name, Object value) {
      this.name = name;
      this.value = value;
      this.replaceCount = inferReplaceCount();
    }

    public String getName() { return name; }

    /**
     * Is the value a {@link List}-type that requires expansion?
     * <p>
     *   <tt>List</tt>-type values are ones that:
     *   <ul>
     *     <li>an array (but not <tt>byte[]</tt></li>
     *     <li>are a {@link Collection}</li>
     *   </ul>
     * </p>
     *
     * @return true if the value is a <tt>List</tt>-type
     */
    public boolean isListTypeValue() {
      if (value == null) {
        return false;
      }
      if (value instanceof byte[]) {
        return false;
      }
      if (value.getClass().isArray()) {
        return true;
      }
      if (value instanceof Collection) {
        return true;
      }
      return false;
    }

    public Object getValue() { return value; }

    public int getReplaceCount() { return replaceCount; }

    public List<Integer> getStartIndexes() { return Collections.unmodifiableList(startIndexes); }

    public int addStartIndex(int index) {
      startIndexes.add(index);
      return index+replaceCount;
    }

    public Integer getFirstIndex() {
      if (startIndexes == null || startIndexes.isEmpty()) {
        return -1;
      }
      return startIndexes.get(0);
    }

    private int inferReplaceCount() {
      if (value == null) {
        return 1;
      }

      if (value instanceof byte[]) {
        // byte[] is a special case of an array since that is how we get BLOB values
        return 1;
      }

      if (value instanceof Collection) {
        Collection<?> bag = (Collection<?>) value;
        return bag.size();
      }

      if (value.getClass().isArray()) {
        return Array.getLength(value);
      }

      return 1;
    }
  }

  private String originalSql;
  private String resultSql;

  private Map<String, ParameterValue> paramValueMap = new HashMap<String, ParameterValue>();

  public ParameterParser(String query, Object...params) {
    this.originalSql = query;

    if (params != null && params.length > 0) {
      if (params.length % 2 != 0) throw new IllegalArgumentException("Mismatched param/value pairs!");

      for (int i = 0; i < params.length; i += 2) {
        String name = (String) params[i];
        Object value = params[i + 1];
        paramValueMap.put(name.toLowerCase(), new ParameterValue(name, value));
      }
    }
  }
  
  public ParameterParser parse() {
    
    for (int index = 0; index < originalSql.length(); ) {
      int ch = originalSql.charAt(index);
      boolean consume = handleChar(ch);
      
      if (consume) index++;
    }
    handleChar(EOS);
    
    resultSql = sql.toString();
    
    return this;
  }

  public Collection<ParameterValue> getParameterValues() {
    return Collections.unmodifiableCollection(paramValueMap.values());
  }

  public String getSql() { return resultSql; }

  /**
   * Get the given parameter value.
   * <p>
   *   This method is here only for testing. It is not part of the public API of this method.
   * </p>
   * @param name the name of parameter we want the parsed value for
   * @return the parsed value
   */
  ParameterValue getParameterValue(String name) {
    return paramValueMap.get(name.toLowerCase());
  }

  private enum State { IN_SQL, PARAM_START, PARAM_NAME, IN_SINGLE, IN_DOUBLE }
  private State state = State.IN_SQL;
  
  private static final int EOS = -1;
  private StringBuffer sql = new StringBuffer();
  private StringBuffer paramName = new StringBuffer();
  private int paramRefIndex = 1;
  
  private boolean handleChar(int ch) {
    switch(state) {
      case IN_SQL:
        return handleInSQL(ch);
        
      case PARAM_START:
        return handleParamStart(ch);
        
      case PARAM_NAME:
        return handleParamName(ch);
        
      case IN_DOUBLE:
        return handleInDouble(ch);
        
      case IN_SINGLE:
        return handleInSingle(ch);
    }
    
    throw new IllegalStateException("In state "+state+", could not process char "+((char) ch));
  }
  
  private boolean handleInSQL(int ch) {
    if (ch == EOS) {
      return true;
    }
    
    if (ch == ':') {
      state = State.PARAM_START;
      return true;
    }
    
    addToResult(ch);
    
    if (ch == '\'') {
      state = State.IN_SINGLE;
    }
    else if (ch == '"') {
      state = State.IN_DOUBLE;
    }
    
    return true;
  }

  private boolean handleParamStart(int ch) {
    if (Character.isLetter((char) ch)) {
      paramName.setLength(0);
      paramName.append((char) ch);
      state = State.PARAM_NAME;
      return true;
    }
    
    // This is basically an unknown use of the colon - just add it to the output
    // and return to the base state
    addToResult(':');
    addToResult(ch);
    state = State.IN_SQL;
    return true;
  }
  
  private boolean handleParamName(int ch) {
    if (Character.isLetterOrDigit((char) ch) || ch == '_') {
      paramName.append((char) ch);
      return true;
    }
    
    addParamRef();
    state = State.IN_SQL;
    return false;       // we need to process this character again, but from the IN_SQL state
  }
  
  private void addParamRef() {
    String name = paramName.toString().toLowerCase();
    ParameterValue paramValue = paramValueMap.get(name);
    if (paramValue == null) {
      throw new IllegalArgumentException("Referenced parameter has no value: "+paramName.toString());
    }

    paramRefIndex = paramValue.addStartIndex(paramRefIndex);

    for (int i = 0; i < paramValue.getReplaceCount(); i++) {
      if (i > 0) {
        addToResult(',');
      }
      addToResult('?');
    }
  }
  
  private boolean handleInDouble(int ch) {
    if (ch == '"') {
      state = State.IN_SQL;
    }
    
    addToResult(ch);
    return true;
  }
  
  private boolean handleInSingle(int ch) {
    if (ch == '\'') {
      state = State.IN_SQL;
    }
    
    addToResult(ch);
    return true;
  }
  
  private void addToResult(int ch) {
    if (ch != EOS) sql.append((char) ch);
  }
}
