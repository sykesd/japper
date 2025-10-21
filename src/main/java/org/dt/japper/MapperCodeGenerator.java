package org.dt.japper;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

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
 * <p>
 * The factory generates a new class each time that will map the result-set of a specific
 * query to an instance of the given class.
 * </p>
 */
public class MapperCodeGenerator {

  private static final Log log = LogFactory.getLog(Japper.class);
  
  @SuppressWarnings("unchecked")
  public static <T> Mapper<T> create(Class<T> resultType, ResultSetMetaData metaData) throws SQLException {
    CtClass impl = createClass();

    String methodBody = buildMapMethodBody(resultType, metaData);
    
    String source = "  public Object map(org.dt.japper.JapperConfig config, java.sql.ResultSet rs, org.dt.japper.RowProcessor rowProcessor) {\n" +
                    methodBody +
                    "    if (rowProcessor != null) {\n" +
                    "      rowProcessor.process(dest, rs);\n" +
                    "    }\n" +
                    "    return dest;\n" +
                    "  }";
    
    try {
      if (log.isTraceEnabled()) {
        log.trace("Generated method:\n"+source);
      }
      
      CtMethod mapImpl = CtNewMethod.make(source, impl);
      impl.addMethod(mapImpl);
      
      return (Mapper<T>) impl.toClass().getDeclaredConstructor().newInstance();
    }
    catch (Exception ex) {
      throw new IllegalStateException("Could not create instance of generated mapper!", ex);
    }
  }
  
  private static CtClass createClass() {
    try {
      ClassPool classPool = getClassPool();
      CtClass intf = classPool.get(Mapper.class.getName());
      CtClass impl = classPool.makeClass(makeNewClassName());
      impl.addInterface(intf);
      return impl;
    }
    catch (NotFoundException nfEx) {
      throw new IllegalStateException("Very weird, Javassist can't find Mapper interface!", nfEx);
    }
  }

  private static final Object CLASS_POOL_MUTEX = new Object();
  private static final int CLASS_POOL_REUSE_COUNTER = 100;
  private static int classCounter = CLASS_POOL_REUSE_COUNTER+1;     // Ensure that the first time call to getClassPool() will create a new class pool instance
  
  private static ClassPool mapperClassPool = null;
  
  private static ClassPool getClassPool() {
    synchronized (CLASS_POOL_MUTEX) {
      classCounter++;
      if (classCounter > CLASS_POOL_REUSE_COUNTER) {
        classCounter = 1;
        mapperClassPool = new ClassPool(true);
        mapperClassPool.insertClassPath(new ClassClassPath(Mapper.class));
      }
      return mapperClassPool;
    }
  }
  
  /**
   * Build the method body to convert the query results described by metaData to instances
   * of resultType.
   *
   * <h4>Graph Guards</h4>
   *
   * {@code resultType} may have complex properties, i.e. a column in {@code metaData} gets
   * mapped to a property of a property of {@code resultType}, or a property of a property
   * of a property of {@code resultType}.
   *
   * <p>
   * The constructor for {@code resultType} may not create an instance of the complex property,
   * instead relying on its clients to set an instance. In order to allow for this we need to
   * put a check in before access to any complex property in order to make sure we don't throw a
   * {@code NullPointerException}.
   * </p>
   *
   * <p>
   * Obviously, we only need to check each complex property once, and we need to make sure that we
   * build the full object graph in the right order. To achieve this we keep a list of the complex
   * properties we have accessed as we come across them. The {@link TreeMap} allows us to keep this
   * list sorted by depth as we build it. Once we have been through each property we produce the
   * code for each guard clause and insert to near the top of the method body.
   * </p>
   *
   * @param resultType the type we are mapping to
   * @param metaData the result set we are mapping from
   * @return the source code of the method body to perform the mapping
   */
  private static <T> String buildMapMethodBody(Class<T> resultType, ResultSetMetaData metaData) throws SQLException {
    StringBuilder source = new StringBuilder().append("    ").append(resultType.getName()).append(" dest = new ").append(resultType.getName()).append("();\n\n");

    int tempCounter = 0;
    Map<String, String> graphGuardMap = new TreeMap<>(new GraphGuardComparator());
    StringBuilder setterSource = new StringBuilder();
    
    PropertyMatcher matcher = new PropertyMatcher(resultType);
    
    for (int i = 1; i <= metaData.getColumnCount(); i++) {
      PropertyDescriptor[] path = matcher.match(metaData.getColumnLabel(i), metaData.getTableName(i), metaData.getColumnName(i));
      if (path != null && isWriteablePath(path)) {
        tempCounter = buildPropertySetter(setterSource, graphGuardMap, i, metaData, path, tempCounter);
      }
    }
    
    String graphGuards = buildGraphGuards(graphGuardMap);
    source.append(graphGuards).append(setterSource);
    
    return source.toString();
  }

  private static boolean isWriteablePath(PropertyDescriptor[] path) {
    return Arrays.stream(path)
            .noneMatch(pd -> pd.getWriteMethod() == null);
  }

  private static class GraphGuardComparator implements Comparator<String> {
    @Override
    public int compare(String g1, String g2) {
      int c1 = countDots(g1);
      int c2 = countDots(g2);
      if (c1 == c2) {
        return g1.compareTo(g2);
      }
      
      return (c1 < c2 ? -1 : 1); 
    }
    
    private int countDots(String s) {
      int count = 0;
      int index = -1;
      while ((index = s.indexOf('.', index)) >= 0) {
        count++;
        index++;
      }
      return count;
    }
  }
  
  private static String buildGraphGuards(Map<String, String> graphGuardMap) {
    StringBuilder guards = new StringBuilder();
    for (String reference : graphGuardMap.keySet()) {
      guards.append( graphGuardMap.get(reference) );
    }
    
    return guards.append("\n").toString();
  }

  private static int buildPropertySetter(StringBuilder source, Map<String,String> graphGuardMap, int columnIndex, ResultSetMetaData metaData, PropertyDescriptor[] path, int tempCounter) throws SQLException {
    PropertySetter ps = new PropertySetter(columnIndex, metaData, path, buildReference(path, graphGuardMap));
    
    if (ps.isBlob()) {
      source.append("    ").append(ps.reference).append('.').append(ps.writerMethod).append("( ").append(ps.readerMethod).append("(config, ").append("rs, ").append(columnIndex).append(") );\n");
    }
    else if (!ps.isNullable() && !ps.isNeedsConversion()) {
      source.append("    ").append(ps.reference).append('.').append(ps.writerMethod).append("( rs.").append(ps.readerMethod).append("(").append(columnIndex).append(") );\n");
    }
    else {
      // read into temp variable
      String tempName = "t"+tempCounter++;
      
      source.append("    ").append(ps.readType.getName()).append(" ").append(tempName).append(" = rs.").append(ps.readerMethod).append("(").append(ps.columnIndex).append(");\n");

      if (ps.isNullable()) {
        // add NULL check
        source.append("    if (").append(tempName).append(" != null) {\n");
      }
      
      if (ps.isNeedsConversion()) {
        String sourceTempName = tempName;
        tempName = "c"+sourceTempName;
        source.append("    ").append(ps.writeType.getName()).append(" ").append(tempName).append(";\n");
        buildConversion(source, ps, tempName, sourceTempName);
      }
      
      // actually set the property value
      source.append("      ").append(ps.reference).append('.').append(ps.writerMethod).append("(").append(tempName).append(");\n");
      
      if (ps.isNullable()) {
        // complete NULL check and add else clause
        source.append("    }\n");
        source.append("    else {\n");
        source.append("      ").append(ps.reference).append('.').append(ps.writerMethod).append("(").append(getNullValue(ps)).append(");\n");
        source.append("    }\n");
      }
    }
    
    return tempCounter;
  }

  private static String getNullValue(PropertySetter ps) {
    if (ps.writeType.isPrimitive()) {
      if (ps.writeType.equals(boolean.class)) return "false";
      if (ps.writeType.equals(float.class) || ps.writeType.equals(double.class)) return "0.0";
      if (ps.writeType.equals(long.class)) return "0L";
      if (ps.writeType.equals(int.class) || ps.writeType.equals(short.class)) return "0";
      return "0";
    }
    
    return "null";
  }
  
  private static void buildConversion(StringBuilder source, PropertySetter ps, String tempName, String sourceTempName) {
    if (ps.writeType.equals(String.class) && ps.readType.equals(String.class)) {
      /*
       * No actual type conversion, but end up here if the read column is CHAR type 
       * So we need to strip trailing space
       */
      source.append("    ").append(tempName).append(" = org.dt.japper.MapperUtils.trimRight(").append(sourceTempName).append(");\n");
      return;
    }
    
    if (ps.writeType.equals(java.sql.Timestamp.class) && ps.readType.equals(java.sql.Date.class) ) {
      source.append("    ").append(tempName).append(" = new java.sql.Timestamp(").append(sourceTempName).append(".getTime());\n");
      return;
    }

    if ( (ps.writeType.equals(long.class) || ps.writeType.equals(Long.class)) && (ps.readType.equals(java.sql.Timestamp.class) || ps.readType.equals(java.sql.Date.class)) ) {
      source.append("    ").append(tempName).append(" = ").append(sourceTempName).append(".getTime();\n");
      return;
    }

    if (ps.writeType.equals(BigDecimal.class)) {
      if (ps.readType.equals(short.class) ||
          ps.readType.equals(int.class) ||
          ps.readType.equals(long.class) ||
          ps.readType.equals(float.class) ||
          ps.readType.equals(double.class)
      ) {
        source.append("    ").append(tempName).append(" = new java.math.BigDecimal(").append(sourceTempName).append(");\n");
        return;
      }
    }

    if (ps.writeType.equals(short.class)) {
      if (ps.readType.equals(float.class) || ps.readType.equals(double.class)) {
        source.append("    ").append(tempName).append(" = (short) ").append(sourceTempName).append(";\n");
        return;
      }

      if (ps.readType.equals(BigDecimal.class)) {
        source.append("    ").append(tempName).append(" = ").append(sourceTempName).append(".shortValue();\n");
        return;
      }
    }

    if (ps.writeType.equals(Short.class)) {
      // It appears that the Javassist compiler does not handle auto-boxing very well. If the destination is
      // an Integer (not an int), then we need to make sure we assign an Integer!
      if (ps.readType.equals(float.class) || ps.readType.equals(double.class)) {
        source.append("    ").append(tempName).append(" = Short.valueOf((short) ").append(sourceTempName).append(");\n");
        return;
      }

      if (ps.readType.equals(BigDecimal.class)) {
        source.append("    ").append(tempName).append(" = Short.valueOf(").append(sourceTempName).append(".shortValue());\n");
        return;
      }
    }

    if (ps.writeType.equals(int.class)) {
      if (ps.readType.equals(float.class) || ps.readType.equals(double.class)) {
        source.append("    ").append(tempName).append(" = (int) ").append(sourceTempName).append(";\n");
        return;
      }

      if (ps.readType.equals(BigDecimal.class)) {
        source.append("    ").append(tempName).append(" = ").append(sourceTempName).append(".intValue();\n");
        return;
      }
    }

    if (ps.writeType.equals(Integer.class)) {
      // It appears that the Javassist compiler does not handle auto-boxing very well. If the destination is
      // an Integer (not an int), then we need to make sure we assign an Integer!
      if (ps.readType.equals(float.class) || ps.readType.equals(double.class)) {
        source.append("    ").append(tempName).append(" = Integer.valueOf((int) ").append(sourceTempName).append(");\n");
        return;
      }

      if (ps.readType.equals(BigDecimal.class)) {
        source.append("    ").append(tempName).append(" = Integer.valueOf(").append(sourceTempName).append(".intValue());\n");
        return;
      }
    }

    if (ps.writeType.equals(long.class)) {
      if (ps.readType.equals(float.class) || ps.readType.equals(double.class)) {
        source.append("    ").append(tempName).append(" = (long) ").append(sourceTempName).append(";\n");
        return;
      }

      if (ps.readType.equals(BigDecimal.class)) {
        source.append("    ").append(tempName).append(" = ").append(sourceTempName).append(".longValue();\n");
        return;
      }
    }

    if (ps.writeType.equals(Long.class)) {
      // It appears that the Javassist compiler does not handle auto-boxing very well. If the destination is
      // an Integer (not an int), then we need to make sure we assign an Integer!
      if (ps.readType.equals(float.class) || ps.readType.equals(double.class)) {
        source.append("    ").append(tempName).append(" = Long.valueOf((long) ").append(sourceTempName).append(");\n");
        return;
      }

      if (ps.readType.equals(BigDecimal.class)) {
        source.append("    ").append(tempName).append(" = Long.valueOf(").append(sourceTempName).append(".longValue());\n");
        return;
      }
    }

    if (ps.writeType.equals(float.class)) {
      if (ps.readType.equals(short.class) ||
          ps.readType.equals(int.class) ||
          ps.readType.equals(double.class)
      ) {
        source.append("    ").append(tempName).append(" = (float) ").append(sourceTempName).append(";\n");
        return;
      }
      
      if (ps.readType.equals(BigDecimal.class)) {
        source.append("    ").append(tempName).append(" = ").append(sourceTempName).append(".floatValue();\n");
        return;
      }
    }

    if (ps.writeType.equals(double.class)) {
      if (ps.readType.equals(short.class) ||
          ps.readType.equals(int.class) ||
          ps.readType.equals(long.class) ||
          ps.readType.equals(float.class)
      ) {
        source.append("    ").append(tempName).append(" = (double) ").append(sourceTempName).append(";\n");
        return;
      }
      
      if (ps.readType.equals(BigDecimal.class)) {
        source.append("    ").append(tempName).append(" = ").append(sourceTempName).append(".doubleValue();\n");
        return;
      }
    }
    
    throw new IllegalArgumentException("Cannot convert from "+ps.readType.getName()+" to "+ps.writeType.getName()+", property: "+ps.readerMethod+"/"+ps.writerMethod);
  }

  
  
  
  private static String buildReference(PropertyDescriptor[] path, Map<String, String> graphGuardMap) {
    if (path.length == 1) return "dest";
    
    StringBuilder reference = new StringBuilder("dest");
    
    for (int i = 0; i < path.length-1; i++) {
      String referenceToHere = reference.toString();
      reference.append(".").append(path[i].getReadMethod().getName()).append("()");
      
      String guardReference = referenceToHere+"."+path[i].getName();
      if (!graphGuardMap.containsKey(guardReference)) {
        String guard = "    if ("+reference+" == null) "+referenceToHere+"."+path[i].getWriteMethod().getName()+"( new "+path[i].getPropertyType().getName()+"() );\n";
        graphGuardMap.put(guardReference, guard);
      }
    }
    
    return reference.toString();
  }
  
  private static final AtomicInteger counter = new AtomicInteger();
  
  private static String makeNewClassName() {
    return "Mapper_" + counter.incrementAndGet();
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
        case -102:    // id of the Oracle TIMESTAMP WITH LOCAL TIME ZONE data type
          readerMethod = "getTimestamp";
          readType = Timestamp.class;
          break;
          
        case Types.DATE:
          readerMethod = "getDate";
          readType = java.sql.Date.class;
          break;
          
        case Types.INTEGER:
          if (writeType.isAssignableFrom(BigDecimal.class)) {
            readerMethod = "getBigDecimal";
            readType = BigDecimal.class;
          }
          else {
            readerMethod = "getInt";
            readType = int.class;
          }
          break;
          
        case Types.FLOAT:
          if (writeType.isAssignableFrom(BigDecimal.class)) {
            readerMethod = "getBigDecimal";
            readType = BigDecimal.class;
          }
          else {
            readerMethod = "getFloat";
            readType = float.class;
          }
          break;
          
        case Types.DOUBLE:
          if (writeType.isAssignableFrom(BigDecimal.class)) {
            readerMethod = "getBigDecimal";
            readType = BigDecimal.class;
          }
          else {
            readerMethod = "getDouble";
            readType = double.class;
          }
          break;
          
        case Types.NUMERIC:
          readerMethod = "getBigDecimal";
          readType = BigDecimal.class;
          break;
          
        case Types.BLOB:
          readerMethod = "org.dt.japper.lob.BlobReader.read";
          readType = byte[].class;
          break;
          
        default:
          readerMethod = null;
          readType = null;
      }
    }
    
    public boolean isNullable() {
      return !readType.isPrimitive() && nullable;
    }
    
    public boolean isBlob() { return sqlType == Types.BLOB; }
    
    public boolean isNeedsConversion() {
      return !writeType.isAssignableFrom(readType) || sqlType == Types.CHAR;
    }
  }
}
