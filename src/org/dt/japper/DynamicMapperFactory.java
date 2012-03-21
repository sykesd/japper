package org.dt.japper;

import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

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
 * POSSIBILITY OF SUCH DAMAGE. * @author Administrator
 * 
 * 
 */

/**
 * A factory class that creates a new instance of {@link Mapper} to map a given result
 * set to objects of a given type.
 *
 * The factory generates a new class each time that will map the result-set of a specific
 * query to an instance of the given class.
 * 
 */
public class DynamicMapperFactory {

  private static final Log log = LogFactory.getLog(Japper.class);
  
  private static final ClassPool CLASS_POOL = new ClassPool(true);
  
  @SuppressWarnings("unchecked")
  public static <T> Mapper<T> create(Class<T> resultType, ResultSetMetaData metaData) throws SQLException {
    CtClass impl = createClass();

    StringBuilder sb = new StringBuilder(1024).append("  public Object map(java.sql.ResultSet rs) {\n");
    buildMapMethod(sb, resultType, metaData);
    sb.append("    return dest;\n").append("  }");
    
    try {
      if (log.isDebugEnabled()) {
        log.debug("Generated method:\n"+sb);
      }
      
      CtMethod mapImpl = CtNewMethod.make(sb.toString(), impl);
      impl.addMethod(mapImpl);
      
      return (Mapper<T>) impl.toClass().newInstance();
    }
    catch (Exception ex) {
      throw new IllegalStateException("Could not create instance of dynamic mapper!", ex);
    }
  }
  
  private static CtClass createClass() {
    try {
      CtClass intf = CLASS_POOL.get(Mapper.class.getName());
      CtClass impl = CLASS_POOL.makeClass(makeNewClassName());
      impl.addInterface(intf);
      return impl;
    }
    catch (NotFoundException nfEx) {
      throw new IllegalStateException("Very weird, Javassist can't find Mapper interface!", nfEx);
    }
  }
  
  private static <T> void buildMapMethod(StringBuilder source, Class<T> resultType, ResultSetMetaData metaData) throws SQLException {
    source.append("    ").append(resultType.getName()).append(" dest = new ").append(resultType.getName()).append("();\n\n");

    int tempCounter = 0;
    
    for (int i = 1; i <= metaData.getColumnCount(); i++) {
      String columnName = metaData.getColumnName(i);
      PropertyDescriptor[] path = MapperUtils.findPropertyPath(resultType, columnName);
      if (path != null) {
        tempCounter = buildPropertySetter(source, i, metaData, path, tempCounter);
      }
    }
  }
  
  private static int buildPropertySetter(StringBuilder source, int columnIndex, ResultSetMetaData metaData, PropertyDescriptor[] path, int tempCounter) throws SQLException {
    PropertySetter ps = new PropertySetter(columnIndex, metaData, path, buildReference(source, path));
    
    if (!ps.isNullable() && !ps.isNeedsConversion()) {
      source.append("    ").append(ps.reference).append('.').append(ps.writerMethod).append("( rs.").append(ps.readerMethod).append("(").append(columnIndex).append(") );\n");
    }
    else {
      // read into temp variable
      String tempName = "t"+tempCounter++;
      
      source.append("    ").append(ps.writeType.getName()).append(" ").append(tempName).append(" = rs.").append(ps.readerMethod).append("(").append(ps.columnIndex).append(");\n");

      if (ps.isNullable()) {
        // add NULL check
        source.append("    if (").append(tempName).append(" != null) {\n");
      }
      
      if (ps.isNeedsConversion()) {
        String sourceTempName = tempName;
        tempName = "c"+sourceTempName;
        source.append("    ").append(ps.writeType.getName()).append(" ").append(tempName).append(";\n");
        tempCounter = buildConversion(source, ps, tempName, sourceTempName, tempCounter);
      }
      
      // actually set the property value
      source.append("      ").append(ps.reference).append('.').append(ps.writerMethod).append("(").append(tempName).append(");\n");
      
      if (ps.isNullable()) {
        // complete NULL check and add else clause
        source.append("    }\n");
        source.append("    else {\n");
        source.append("      ").append(ps.reference).append('.').append(ps.writerMethod).append("(null);\n");
        source.append("    }\n");
      }
    }
    
    return tempCounter;
  }

  
  private static int buildConversion(StringBuilder source, PropertySetter ps, String tempName, String sourceTempName, int tempCounter) {
    if (ps.writeType.equals(java.sql.Timestamp.class) && ps.readType.equals(java.sql.Date.class) ) {
      source.append("    ").append(tempName).append(" = new java.sql.Timestamp(").append(sourceTempName).append(".getTime());\n");
      return tempCounter;
    }
    
    // TODO Handle all the fun numeric data types and combinations
    
    return tempCounter;
  }

  private static String buildReference(StringBuilder source, PropertyDescriptor[] path) {
    if (path.length == 1) return "dest";
    
    StringBuilder reference = new StringBuilder(path[0].getReadMethod().getName()).append("()");
    
    for (int i = 0; i < path.length-1; i++) {
      reference.append(i == 0 ? "" : ".").append(path[0].getReadMethod().getName()).append("()");
      source.append("    if (").append(reference).append(" == null) ").append(path[i].getWriteMethod().getName()).append("( new ").append(path[i].getPropertyType().getName()).append("() );\n");
    }
    
    return reference.toString();
  }
  
  private static int counter = 1;
  
  private static String makeNewClassName() {
    return "Mapper_"+counter++;
  }
  
  
  
  private static class PropertySetter {
    public final PropertyDescriptor descriptor;
    public final Class<?> writeType;
    public final String writerMethod;
    public final String reference;
    
    public final int columnIndex;
    public final int sqlType;
    public final boolean nullable;
    
    public final String readerMethod;
    public final Class<?> readType;
    
    public PropertySetter(int columnIndex, ResultSetMetaData metaData, PropertyDescriptor[] path, String reference) throws SQLException {
      this.descriptor = path[path.length-1];
      this.writeType = descriptor.getPropertyType();
      this.writerMethod = descriptor.getWriteMethod().getName();
      this.reference = reference;
      
      this.columnIndex = columnIndex;
      this.sqlType = metaData.getColumnType(columnIndex);
      this.nullable = ( metaData.isNullable(columnIndex) != ResultSetMetaData.columnNoNulls );
      
      switch(sqlType) {
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.CLOB:
          readerMethod = "getString";
          readType = String.class;
          break;
          
        case Types.TIMESTAMP:
          readerMethod = "getTimestamp";
          readType = Timestamp.class;
          break;
          
        case Types.DATE:
          readerMethod = "getDate";
          readType = java.sql.Date.class;
          break;
          
        case Types.NUMERIC:
          if (writeType.isAssignableFrom(BigDecimal.class)) {
            readerMethod = "getBigDecimal";
            readType = BigDecimal.class;
          }
          else {
            readerMethod = "getBigDecimal";
            readType = BigDecimal.class;
          }
          break;
          
        default:
          readerMethod = null;
          readType = null;
      }
    }
    
    public boolean isNullable() {
      return !readType.isPrimitive() && nullable;
    }
    
    public boolean isNeedsConversion() { 
      return !writeType.isAssignableFrom(readType);
    }
  }
}
