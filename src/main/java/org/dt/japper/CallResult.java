package org.dt.japper;

import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;

public class CallResult {

  private final List<Parameter> parameters = new ArrayList<>();
  private final Map<String, Object> valueMap = new HashMap<>();
  
  CallResult() {}
  
  public <T> T get(String name, Class<T> resultType) {
    return resultType.cast(valueMap.get(name));
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
    
    // TODO add in options for Java types: Integer, Long, Double, Float
    
    throw new IllegalArgumentException("Unsupported OUT parameter type: "+p.getName()+"("+p.getType().getName()+")");
  }

  private int getSqlType(Class<?> type) {
    if (type.equals(String.class)) {
      return Types.VARCHAR;
    }
    
    if (type.equals(BigDecimal.class)) {
      return Types.NUMERIC;
    }
    
    if (type.equals(Timestamp.class)) {
      return Types.TIMESTAMP;
    }
    
    if (type.equals(Date.class)) {
      return Types.DATE;
    }
    
    // TODO add in options for Java types: Integer, Long, Double, Float
    
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
