package org.dt.japper;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MapperGeneratorTest {

  @Test
  public void simpleInterfaceTest() throws Exception {
    ClassPool pool = new ClassPool(true); 
    CtClass intf = pool.get("org.dt.japper.SimpleInterface");
    CtClass impl = pool.makeClass("org.dt.japper.SimpleImpl");
    impl.addInterface(intf);
    
    CtMethod fooImpl = CtNewMethod.make("public String foo() { return \"bar\"; }", impl);
    impl.addMethod(fooImpl);

    SimpleInterface si = (SimpleInterface) impl.toClass().newInstance();
    assertEquals("bar", si.foo());
  }
  
  
  @Test
  public void genericInterfaceTest() throws Exception {
    ClassPool pool = new ClassPool(true);
    CtClass intf = pool.get("org.dt.japper.Mapper");
    CtClass impl = pool.makeClass("org.dt.japper.Mapper_123");
    impl.addInterface(intf);
    
    CtMethod mapImpl = CtNewMethod.make("public Object map(java.sql.ResultSet rs, org.dt.japper.RowProcessor rowProcessor) { return \"bar\"; }", impl);
    impl.addMethod(mapImpl);
    
    @SuppressWarnings("unchecked")
    Mapper<String> mapper = (Mapper<String>) impl.toClass().newInstance();
    
    assertEquals("bar", mapper.map(null, null));
  }
  
  
  @Test
  public void genericInterfaceWithLookupTest() throws Exception {
    Mapper<String> mapper = createMapper(String.class).newInstance();
    assertEquals("bar", mapper.map(null, null));
  }
  

  @SuppressWarnings({"unchecked", "cast"})
  private <T> Class<Mapper<T>> createMapper(Class<T> resultType) throws Exception {
    ClassPool pool = new ClassPool(true);
    CtClass intf = pool.get("org.dt.japper.Mapper");
    CtClass impl = pool.makeClass("org.dt.japper.Mapper_234");
    impl.addInterface(intf);
    
    CtMethod mapImpl = CtNewMethod.make("public Object map(java.sql.ResultSet rs, org.dt.japper.RowProcessor rowProcessor) { return \"bar\"; }", impl);
    impl.addMethod(mapImpl);
    
    return (Class<Mapper<T>>) impl.toClass();
  }
  
  
  private static class ByteClassLoader extends URLClassLoader {

    private final Map<String, byte[]> extraClassDefs = new HashMap<String, byte[]>();
    
    public ByteClassLoader(URL[] urls, ClassLoader parent) {
      super(urls, parent);
    }

    public void addClass(String name, byte[] bytecode) {
      extraClassDefs.put(name, bytecode);
    }
    
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
      byte[] bytecode = this.extraClassDefs.remove(name);
      if (bytecode != null) {
        return defineClass(name, bytecode, 0, bytecode.length);
      }
      return super.findClass(name);
    }
  }
  
}
