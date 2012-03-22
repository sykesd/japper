package org.dt.japper;

import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;

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
 * POSSIBILITY OF SUCH DAMAGE. * @author Administrator
 * 
 * 
 */


public class DefaultMapper<T> implements Mapper<T> {

  private Class<T> resultType;
  private ResultSetMetaData metaData;

  public DefaultMapper(Class<T> resultType, ResultSetMetaData metaData) {
    this.resultType = resultType;
    this.metaData = metaData;
  }
  
  @Override
  public T map(ResultSet rs) throws SQLException {
    T dest = create(resultType);
    
    for (int i = 1; i <= metaData.getColumnCount(); i++) {
      MapperUtils.Ref ref = MapperUtils.findPropertyInGraph(dest, metaData.getColumnName(i));
      if (ref != null) {
        setProperty(ref, rs, i);
      }
    }
    
    return dest;
  }
  
  
  private void setProperty(MapperUtils.Ref ref, ResultSet rs, int columnIndex) {
    try {
      Object dest = ref.getBean();
      String propertyName = ref.getName();
      PropertyDescriptor writeDescriptor = ref.getDescriptor();
      int sqlType = metaData.getColumnType(columnIndex);
      
      switch(sqlType) {
        case Types.CHAR:
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
          
      }
    }
    catch (Exception ex) {
      String columnName = null;
      try { columnName = metaData.getColumnName(columnIndex); } catch (SQLException ignored) {}
      throw new IllegalArgumentException("Could not set value of property '"+ref.getName()+"' from column '"+columnName, ex);
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
  private void setProperty(Object dest, String propertyName, Class<?> writeType, String value) {
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
      PropertyUtils.setProperty(dest, propertyName, value);
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
    catch (InstantiationException iEx) {
      throw new IllegalArgumentException("Type "+targetType.getName()+" does not have a default constructor!", iEx);
    }
    catch (IllegalAccessException iaEx) {
      throw new IllegalArgumentException("Type "+targetType.getName()+" does not have a default constructor!", iaEx);
    }
  }

}
