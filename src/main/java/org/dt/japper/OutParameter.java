package org.dt.japper;

public class OutParameter {

  private final Class<?> type;
  
  OutParameter(Class<?> type) {
    this.type = type;
  }
  
  Class<?> getType() { return type; }
  
  @Override
  public String toString() {
    return "OUT("+type.getName()+")";
  }
}
