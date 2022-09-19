package org.dt.japper;

import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.dt.japper.lob.BlobReader;

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


public class DefaultMapper<T> implements Mapper<T> {

  private final Class<T> resultType;
  private final ResultSetMetaData metaData;

  private final List<PropertyDescriptor[]> cachedPaths = new ArrayList<>();
  
  private static final PropertyDescriptor[] EMPTY_PATH = {};
  
  public DefaultMapper(Class<T> resultType, ResultSetMetaData metaData) {
    this.resultType = resultType;
    this.metaData = metaData;
    
    cachedPaths.add(EMPTY_PATH);
  }
  
  @Override
  public T map(ResultSet rs, RowProcessor<T> rowProcessor) throws SQLException {
    T dest = create(resultType);
    
    PropertyMatcher matcher = null;
    
    for (int i = 1; i <= metaData.getColumnCount(); i++) {
      if (cachedPaths.size() <= i) {
        if (matcher == null) {
          matcher = new PropertyMatcher(resultType);
        }
        
        PropertyDescriptor[] path = matcher.match(metaData.getColumnLabel(i), metaData.getTableName(i), metaData.getColumnName(i));
        if (path != null) {
          cachedPaths.add(path);
        }
        else {
          cachedPaths.add(EMPTY_PATH);
        }
      }
      
      PropertyDescriptor[] path = cachedPaths.get(i);
      if (path != EMPTY_PATH) {
        setProperty(path, dest, rs, i);
      }
    }

    if (rowProcessor != null) {
      rowProcessor.process(dest, rs);
    }

    return dest;
  }
  

  
  private void setProperty(PropertyDescriptor[] path, Object dest, ResultSet rs, int columnIndex) {
    try {
      for (int i = 0; i < path.length-1; i++) {
        Object value = PropertyUtils.getProperty(dest, path[i].getName());
        if (value == null) {
          value = MapperUtils.create(path[i].getPropertyType());
          PropertyUtils.setProperty(dest, path[i].getName(), value);
        }
        dest = value;
      }
      
      setProperty(dest, path[path.length-1], rs, columnIndex);
    }
    catch (Exception ex) {
      String columnName = null;
      try { columnName = metaData.getColumnName(columnIndex); } catch (SQLException ignored) {}
      throw new IllegalArgumentException("Could not set value of property '"+getPropertyRef(path)+"' from column '"+columnName, ex);
    }
  }

  private String getPropertyRef(PropertyDescriptor[] path) {
    StringBuilder ref = new StringBuilder();
    String prefix = "";
    for (PropertyDescriptor descriptor : path) {
      ref.append(prefix).append(descriptor.getName());
      prefix = ".";
    }
    return ref.toString();
  }
  
  private void setProperty(Object dest, PropertyDescriptor writeDescriptor, ResultSet rs, int columnIndex) {
    try {
      String propertyName = writeDescriptor.getName();
      int sqlType = metaData.getColumnType(columnIndex);
      
      switch(sqlType) {
        case Types.CHAR:
          setProperty(dest, propertyName, writeDescriptor.getPropertyType(), MapperUtils.trimRight(rs.getString(columnIndex)));
          break;
          
        case Types.VARCHAR:
        case Types.CLOB:
          setProperty(dest, propertyName, writeDescriptor.getPropertyType(), rs.getString(columnIndex));
          break;
          
        case Types.NUMERIC:
        case Types.INTEGER:
        case Types.FLOAT:
        case Types.DOUBLE:
          setProperty(dest, propertyName, writeDescriptor.getPropertyType(), rs.getBigDecimal(columnIndex));
          break;
          
        case Types.DATE:
          setProperty(dest, propertyName, writeDescriptor.getPropertyType(), rs.getDate(columnIndex));
          break;
          
        case Types.TIMESTAMP:
          setProperty(dest, propertyName, writeDescriptor.getPropertyType(), rs.getTimestamp(columnIndex));
          break;
          
        case Types.BLOB:
          setProperty(dest, propertyName, writeDescriptor.getPropertyType(), BlobReader.read(rs, columnIndex));
          break;
          
      }
    }
    catch (Exception ex) {
      String columnName = null;
      try { columnName = metaData.getColumnName(columnIndex); } catch (SQLException ignored) {}
      throw new IllegalArgumentException("Could not set value of property '"+writeDescriptor.getName()+"' from column '"+columnName, ex);
    }
  }
  
  /**
   * Set the property value from a string
   * 
   * @param dest the object to set the value on
   * @param propertyName the property to set the value on
   * @param writeType the type of the property we are setting
   * @param value the value we are setting the property to
   */
  private void setProperty(Object dest, String propertyName, @SuppressWarnings("unused") Class<?> writeType, String value) {
    try {
      PropertyUtils.setProperty(dest, propertyName, value);
    }
    catch (Exception ex) {
      throw new IllegalArgumentException("Could not set property '"+propertyName+"' from String value", ex);
    }
  }
  
  /**
   * Set the property value from a BigDecimal
   * 
   * @param dest the object to set the value on
   * @param propertyName the property to set the value on
   * @param writeType the type of the property we are setting
   * @param value the value we are setting the property to
   */
  private void setProperty(Object dest, String propertyName, Class<?> writeType, BigDecimal value) {
    try {
      if (writeType.equals(int.class) || writeType.equals(Integer.class)) {
        PropertyUtils.setProperty(dest, propertyName, value.intValue());
      }
      else if (writeType.equals(float.class) || writeType.equals(Float.class)) {
        PropertyUtils.setProperty(dest, propertyName, value.floatValue());
      }
      else if (writeType.equals(double.class) || writeType.equals(Double.class)) {
        PropertyUtils.setProperty(dest, propertyName, value.doubleValue());
      }
      else {
        PropertyUtils.setProperty(dest, propertyName, value);
      }
    }
    catch (Exception ex) {
      throw new IllegalArgumentException("Could not set property '"+propertyName+"' from BigDecimal value", ex);
    }
  }
  
  /**
   * Set the property value from a Date
   * 
   * @param dest the object to set the value on
   * @param propertyName the property to set the value on
   * @param writeType the type of the property we are setting
   * @param value the value we are setting the property to
   */
  private void setProperty(Object dest, String propertyName, Class<?> writeType, Date value) {
    try {
      Object valueToSet = value;
      if (writeType.equals(long.class)) {
        if (value != null) {
          valueToSet = value.getTime();
        }
        else {
          valueToSet = 0L;
        }
      }
      
      PropertyUtils.setProperty(dest, propertyName, valueToSet);
    }
    catch (Exception ex) {
      throw new IllegalArgumentException("Could not set property '"+propertyName+"' from BigDecimal value", ex);
    }
  }
  
  /**
   * Set the property value from a Timestamp
   * 
   * @param dest the object to set the value on
   * @param propertyName the property to set the value on
   * @param writeType the type of the property we are setting
   * @param value the value we are setting the property to
   */
  private void setProperty(Object dest, String propertyName, Class<?> writeType, Timestamp value) {
    try {
      Object valueToSet = value;
      if (writeType.equals(long.class)) {
        if (value != null) {
          valueToSet = value.getTime();
        }
        else {
          valueToSet = 0L;
        }
      }
      
      PropertyUtils.setProperty(dest, propertyName, valueToSet);
    }
    catch (Exception ex) {
      throw new IllegalArgumentException("Could not set property '"+propertyName+"' from BigDecimal value", ex);
    }
  }
  
  /**
   * Set the property value from a byte[]
   * 
   * @param dest the object to set the value on
   * @param propertyName the property to set the value on
   * @param writeType the type of the property we are setting
   * @param value the value we are setting the property to
   */
  private void setProperty(Object dest, String propertyName, @SuppressWarnings("unused") Class<?> writeType, byte[] value) {
    try {
      PropertyUtils.setProperty(dest, propertyName, value);
    }
    catch (Exception ex) {
      throw new IllegalArgumentException("Could not set property '"+propertyName+"' from BigDecimal value", ex);
    }
  }
  
  private static <T> T create(Class<T> targetType) {
    try {
      return targetType.newInstance();
    }
    catch (InstantiationException | IllegalAccessException iEx) {
      throw new IllegalArgumentException("Type "+targetType.getName()+" does not have a default constructor!", iEx);
    }
  }

}
