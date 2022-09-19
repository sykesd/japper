package org.dt.japper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

  public static class ParameterValue {
    private final String name;
    private Object value;
    private final int replaceCount;
    private final List<Integer> startIndexes = new ArrayList<>();

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

    void setValue(Object value) {
      this.value = value;
    }

    public int getReplaceCount() { return replaceCount; }

    public List<Integer> getStartIndexes() { return Collections.unmodifiableList(startIndexes); }

    public int addStartIndex(int index) {
      startIndexes.add(index);
      return index+replaceCount;
    }

    public Integer getFirstIndex() {
      if (startIndexes.isEmpty()) {
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

  private final String originalSql;
  private String resultSql;

  private final Map<String, ParameterValue> paramValueMap = new HashMap<>();

  /**
   * Are we parsing the statement for use in a JDBC batch?
   */
  private boolean parsingForBatch;

  public ParameterParser(String query, Object...params) {
    this.originalSql = query;

    parseParameters(params);
  }

  public ParameterParser parse() {
    return parse(false);
  }

  /**
   * Simple iterator-like class for managing the current position in the input SQL
   * and allowing lookahead to aid in parsing comments
   */
  private static class InputChar {
    private final String input;
    private int index;
    private final int length;

    private InputChar(String input) {
      this.input = input;
      this.index = 0;
      this.length = input.length();
    }

    public boolean inBounds() {
      return index < length;
    }

    public int get() {
      if (!inBounds()) {
        return EOS;
      }
      return input.charAt(index);
    }

    public void consume() {
      index++;
    }

    @Override
    public String toString() {
      return "InputChar{inBounds=" + inBounds() + ", index=" + index + ", char=" + (inBounds() ? (char) get() : ' ') + "}";
    }
  }

  public ParameterParser parse(boolean forBatch) {
    this.parsingForBatch = forBatch;

    InputChar input = new InputChar(originalSql);
    while (input.inBounds()) {
      handleChar(input);
    }

    // Do one more handleChar(...) in the "out-of-bounds" state to make sure we capture any remaining input
    handleChar(input);
    
    resultSql = sql.toString();
    
    return this;
  }

  /**
   * Set the values to those referenced in the given parameter set.
   * <p>
   * This method replaces the current values with those
   * </p>
   * @param params the next set of query parameters for the next statement in the batch
   */
  public void setNextParameterValueSet(Object[] params) {
    // save away the current list of parameters - this will be the complete list referenced by
    // the query
    Map<String, ParameterValue> originalMap = new HashMap<>(paramValueMap);

    // clear the internal field and then parse the the new parameter list into it
    paramValueMap.clear();
    parseParameters(params);

    // now we need to restore the original map, clear all the values...
    Map<String, ParameterValue> newSet = new HashMap<>(paramValueMap);
    paramValueMap.clear();
    paramValueMap.putAll(originalMap);
    paramValueMap.values().forEach(value -> value.setValue(null));

    // ...and set all the new ones given
    Set<String> names = paramValueMap.keySet();
    newSet.keySet().stream()
            .filter(names::contains)
            .forEach(name -> paramValueMap.get(name).setValue(newSet.get(name).getValue()))
    ;
  }

  public Collection<ParameterValue> getParameterValues() {
    return Collections.unmodifiableCollection(paramValueMap.values());
  }

  public String getSql() { return resultSql; }

  /**
   * Parse the parameter values set and populate the {@link #paramValueMap} with the name/value pairs.
   *
   * @param params the parameter set to parse
   */
  private void parseParameters(Object[] params) {
    if (params != null && params.length > 0) {
      if (params.length % 2 != 0) throw new IllegalArgumentException("Mismatched param/value pairs!");

      for (int i = 0; i < params.length; i += 2) {
        String name = (String) params[i];
        Object value = params[i + 1];
        paramValueMap.put(name.toLowerCase(), new ParameterValue(name, value));
      }
    }
  }

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

  private enum State { IN_SQL, PARAM_START, PARAM_NAME, IN_SINGLE, IN_DOUBLE, IN_COMMENT  }
  private State state = State.IN_SQL;

  private static final int EOS = -1;
  private final StringBuffer sql = new StringBuffer();
  private final StringBuffer paramName = new StringBuffer();
  private int paramRefIndex = 1;
  
  private void handleChar(InputChar input) {
    switch(state) {
      case IN_SQL:
        handleInSQL(input);
        return;
        
      case PARAM_START:
        handleParamStart(input);
        return;
        
      case PARAM_NAME:
        handleParamName(input);
        return;

      case IN_COMMENT:
        handleInComment(input);
        return;

      case IN_DOUBLE:
        handleInDouble(input);
        return;
        
      case IN_SINGLE:
        handleInSingle(input);
        return;
    }
    
    throw new IllegalStateException("In state " + state + ", could not process char " + input);
  }
  
  private void handleInSQL(InputChar input) {
    if (!input.inBounds()) {
      return;
    }

    int ch = input.get();
    if (ch == ':') {
      state = State.PARAM_START;
      input.consume();
      return;
    }

    addToResult(ch);
    input.consume();

    if (ch == '\'') {
      state = State.IN_SINGLE;
    }
    else if (ch == '"') {
      state = State.IN_DOUBLE;
    }
    else if (ch == '/' && input.get() == '*') {
      addToResult('*');
      // Make sure to consume the '*' as well before continuing
      input.consume();
      state = State.IN_COMMENT;
    }

  }

  private void handleParamStart(InputChar input) {
    int ch = input.get();

    if (Character.isLetter((char) ch)) {
      paramName.setLength(0);
      paramName.append((char) ch);
      state = State.PARAM_NAME;
      input.consume();
      return;
    }
    
    // This is basically an unknown use of the colon - just add it to the output
    // and return to the base state
    addToResult(':');
    addToResult(ch);
    state = State.IN_SQL;
    input.consume();
  }
  
  private void handleParamName(InputChar input) {
    int ch = input.get();

    if (Character.isLetterOrDigit((char) ch) || ch == '_') {
      paramName.append((char) ch);
      input.consume();
      return;
    }
    
    addParamRef();
    state = State.IN_SQL;
    // We need to process this character again, but from the IN_SQL state, so we don't consume it
  }
  
  private void addParamRef() {
    String name = paramName.toString().toLowerCase();
    if (parsingForBatch && !paramValueMap.containsKey(name)) {
      paramValueMap.put(name, new ParameterValue(name, null));
    }

    ParameterValue paramValue = paramValueMap.get(name);
    if (paramValue == null) {
      throw new IllegalArgumentException("Referenced parameter has no value: " + paramName);
    }

    paramRefIndex = paramValue.addStartIndex(paramRefIndex);

    for (int i = 0; i < paramValue.getReplaceCount(); i++) {
      if (i > 0) {
        addToResult(',');
      }
      addToResult('?');
    }
  }

  private void handleInComment(InputChar input) {
    int ch = input.get();
    addToResult(ch);
    input.consume();

    if (ch == '*' && input.get() == '/') {
      addToResult('/');
      input.consume();

      state = State.IN_SQL;
      return;
    }
  }

  private void handleInDouble(InputChar input) {
    int ch = input.get();

    if (ch == '"') {
      state = State.IN_SQL;
    }
    
    addToResult(ch);
    input.consume();
  }
  
  private void handleInSingle(InputChar input) {
    int ch = input.get();

    if (ch == '\'') {
      state = State.IN_SQL;
    }
    
    addToResult(ch);
    input.consume();
  }
  
  private void addToResult(int ch) {
    if (ch != EOS) sql.append((char) ch);
  }
}
