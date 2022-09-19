package org.dt.japper;

import java.math.BigDecimal;

public class ProcResult {

  private String mangled;
  
  private BigDecimal nameRank;

  public String getMangled() {
    return mangled;
  }

  public void setMangled(String mangled) {
    this.mangled = mangled;
  }

  public BigDecimal getNameRank() {
    return nameRank;
  }

  public void setNameRank(BigDecimal nameRank) {
    this.nameRank = nameRank;
  }
  
}
