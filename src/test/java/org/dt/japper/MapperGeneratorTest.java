package org.dt.japper;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MapperGeneratorTest {

  @Test
  public void simpleInterfaceTest() throws Exception {
    ClassPool pool = new ClassPool(true); 
    CtClass intf = pool.get("org.dt.japper.SimpleInterface");
    CtClass impl = pool.makeClass("org.dt.japper.SimpleImpl");
    impl.addInterface(intf);
    
    CtMethod fooImpl = CtNewMethod.make("public String foo() { return \"bar\"; }", impl);
    impl.addMethod(fooImpl);

    SimpleInterface si = (SimpleInterface) impl.toClass().getDeclaredConstructor().newInstance();
    assertEquals("bar", si.foo());
  }
  
  
  @Test
  public void genericInterfaceTest() throws Exception {
    ClassPool pool = new ClassPool(true);
    CtClass intf = pool.get("org.dt.japper.Mapper");
    CtClass impl = pool.makeClass("org.dt.japper.Mapper_123");
    impl.addInterface(intf);
    
    CtMethod mapImpl = CtNewMethod.make("public Object map(org.dt.japper.JapperConfig config, java.sql.ResultSet rs, org.dt.japper.RowProcessor rowProcessor) { return \"bar\"; }", impl);
    impl.addMethod(mapImpl);
    
    @SuppressWarnings("unchecked")
    Mapper<String> mapper = (Mapper<String>) impl.toClass().getDeclaredConstructor().newInstance();
    
    assertEquals("bar", mapper.map(null,null, null));
  }
  
  
  @Test
  public void genericInterfaceWithLookupTest() throws Exception {
    Mapper<String> mapper = createMapper(String.class).getDeclaredConstructor().newInstance();
    assertEquals("bar", mapper.map(null,null, null));
  }
  

  @SuppressWarnings({"unchecked", "cast", "SameParameterValue"})
  private <T> Class<Mapper<T>> createMapper(Class<T> ignoredResultType) throws Exception {
    ClassPool pool = new ClassPool(true);
    CtClass intf = pool.get("org.dt.japper.Mapper");
    CtClass impl = pool.makeClass("org.dt.japper.Mapper_234");
    impl.addInterface(intf);
    
    CtMethod mapImpl = CtNewMethod.make("public Object map(org.dt.japper.JapperConfig config, java.sql.ResultSet rs, org.dt.japper.RowProcessor rowProcessor) { return \"bar\"; }", impl);
    impl.addMethod(mapImpl);
    
    return (Class<Mapper<T>>) impl.toClass();
  }
  
}
