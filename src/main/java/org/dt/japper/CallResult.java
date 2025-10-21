package org.dt.japper;

import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;

/**
 * Class that provides dynamic access to the results of a 
 * {@link Japper#call(Connection, String, Object...)}, if any.
 * <p> 
 * This can be thought of analogous to {@link QueryResult}, but for call-like
 * SQL statements.
 */
public class CallResult {

  private final List<Parameter> parameters = new ArrayList<>();
  private final Map<String, Object> valueMap = new HashMap<>();
  
  CallResult() {}

  /**
   * Extract the value named {@code name} from the result of a call statement,
   * casting the result to type {@code resultType}.
   *
   * @param name the name of the value to extract
   * @param resultType the {@link Class} to try and cast the value to
   * @return the value, cast to type {@link Class} of {@link T}
   * @param <T> the actual result type
   */
  public <T> T get(String name, Class<T> resultType) {
    if (resultType.isPrimitive()) {
      return magicBox(resultType, valueMap.get(name));
    }

    return resultType.cast(valueMap.get(name));
  }

  @SuppressWarnings("unchecked")
  private <T> T magicBox(Class<T> resultType, Object value) {
    if (resultType == short.class) {
      return (T) Short.valueOf(value != null ? (short) value : 0);
    }
    if (resultType == int.class) {
      return (T) Integer.valueOf(value != null ? (int) value : 0);
    }
    if (resultType == long.class) {
      return (T) Long.valueOf(value != null ? (long) value : 0);
    }
    if (resultType == float.class) {
      return (T) Float.valueOf(value != null ? (float) value : 0);
    }
    if (resultType == double.class) {
      return (T) Double.valueOf(value != null ? (double) value : 0);
    }

    throw new IllegalArgumentException("Unsupported primitive type in OUT parameter wrangling: " + resultType.getName() + " from " + (value == null ? "(null)" : value.getClass()));
  }

  void register(CallableStatement cs, String name, Class<?> type, int index) throws SQLException {
    Parameter p = new Parameter(name, type, index);
    
    int sqlType = getSqlType(type);
    cs.registerOutParameter(index, sqlType);
    
    parameters.add(p);
  }
  
  void readResults(CallableStatement cs) throws SQLException {
    for (Parameter p : parameters) {
      setValue(cs, p); 
    }
  }
  
  <T> T mapResults(CallableStatement cs, Class<T> targetType) {
    PropertyMatcher matcher = new PropertyMatcher(targetType);
    
    T result = MapperUtils.create(targetType);
    
    for (Parameter p : parameters) {
      PropertyDescriptor[] path = matcher.match(p.getName(), null, null);
      if (path != null && path.length == 1) {
        setValue(cs, p, path[0], result);
      }
    }
    
    return result;
  }
  

  private void setValue(CallableStatement cs, Parameter p, PropertyDescriptor pd, Object result) {
    try {
      setValue(cs, p);
      PropertyUtils.setProperty(result, pd.getName(), valueMap.get(p.getName()));
    }
    catch (Exception ex) {
      throw new IllegalArgumentException("Could not set property '"+pd.getName()+"' on type "+result.getClass().getName()+" from parameter "+p.getName(), ex);
    }
  }
  
  private void setValue(CallableStatement cs, Parameter p) throws SQLException {
    if (p.getType().equals(String.class)) {
      valueMap.put(p.getName(), cs.getString(p.getIndex()));
      return;
    }
    
    if (p.getType().equals(BigDecimal.class)) {
      valueMap.put(p.getName(), cs.getBigDecimal(p.getIndex()));
      return;
    }
    
    if (p.getType().equals(Timestamp.class)) {
      valueMap.put(p.getName(), cs.getTimestamp(p.getIndex()));
      return;
    }

    if (p.getType().equals(Date.class)) {
      valueMap.put(p.getName(), cs.getDate(p.getIndex()));
      return;
    }

    if (p.getType().equals(Short.class) || p.getType().equals(short.class)) {
      valueMap.put(p.getName(), cs.getShort(p.getIndex()));
      return;
    }

    if (p.getType().equals(Integer.class) || p.getType().equals(int.class)) {
      valueMap.put(p.getName(), cs.getInt(p.getIndex()));
      return;
    }

    if (p.getType().equals(Long.class) || p.getType().equals(long.class)) {
      valueMap.put(p.getName(), cs.getLong(p.getIndex()));
      return;
    }

    if (p.getType().equals(Float.class) || p.getType().equals(float.class)) {
      valueMap.put(p.getName(), cs.getFloat(p.getIndex()));
      return;
    }

    if (p.getType().equals(Double.class) || p.getType().equals(double.class)) {
      valueMap.put(p.getName(), cs.getDouble(p.getIndex()));
      return;
    }

    throw new IllegalArgumentException("Unsupported OUT parameter type: "+p.getName()+"("+p.getType().getName()+")");
  }

  private int getSqlType(Class<?> type) {
    if (type.equals(String.class)) {
      return Types.VARCHAR;
    }
    
    if (type.equals(BigDecimal.class) ||
        type.equals(Short.class) || type.equals(short.class) ||
        type.equals(Integer.class) || type.equals(int.class) ||
        type.equals(Long.class) || type.equals(long.class) ||
        type.equals(Float.class) || type.equals(float.class) ||
        type.equals(Double.class) || type.equals(double.class)
    ) {
      return Types.NUMERIC;
    }
    
    if (type.equals(Timestamp.class)) {
      return Types.TIMESTAMP;
    }

    if (type.equals(Date.class)) {
      return Types.DATE;
    }

    throw new IllegalArgumentException("Unsupported OUT parameter type: "+type.getName());
  }

  private static class Parameter {
    private final int index;
    private final Class<?> type;
    private final String name;
    
    public Parameter(String name, Class<?> type, int index) {
      this.name = name;
      this.type = type;
      this.index = index;
    }
    
    public String getName() { return name; }
    public Class<?> getType() { return type; }
    public int getIndex() { return index; }
  }
  
}
