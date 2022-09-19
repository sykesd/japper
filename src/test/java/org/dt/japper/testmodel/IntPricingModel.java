package org.dt.japper.testmodel;

import java.math.BigDecimal;

/**
 * A model class that is identical to {@link PricingModel} except
 * it uses {@link Integer} instead of {@link BigDecimal} in an effort
 * to trigger the {@code VerifyError} bug.
 */
public class IntPricingModel {
  private Integer pricingId;
  private String description;

  public Integer getPricingId() {
    return pricingId;
  }

  public void setPricingId(Integer pricingId) {
    this.pricingId = pricingId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
