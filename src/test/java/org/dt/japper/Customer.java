package org.dt.japper;


/*
 * Copyright (c) 2013, David Sykes and Tomasz Orzechowski 
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
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * 
 */

public class Customer {

  private String sitecode;
  
  private String customerno;
  
  private String alternateCustomerNo1;
  
  private String status;
  
  private String creditStatus;
  
  private String badDebtFlag;

  private String taxItems;
  
  private String taxable;     // NOTE: actually denotes "tax exempt"
  
  private String paymentTerm;
  
  private String shippingTerm;
  
  private String deliveryTerm;
  
  private String currencyCode;
  
  private String invoiceto;
  
  private Address address;

  public String getSitecode() {
    return sitecode;
  }

  public void setSitecode(String sitecode) {
    this.sitecode = sitecode;
  }

  public String getCustomerno() {
    return customerno;
  }

  public void setCustomerno(String customerno) {
    this.customerno = customerno;
  }

  public String getAlternateCustomerNo1() {
    return alternateCustomerNo1;
  }

  public void setAlternateCustomerNo1(String alternateCustomerNo1) {
    this.alternateCustomerNo1 = alternateCustomerNo1;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getCreditStatus() {
    return creditStatus;
  }

  public void setCreditStatus(String creditStatus) {
    this.creditStatus = creditStatus;
  }

  public String getBadDebtFlag() {
    return badDebtFlag;
  }

  public void setBadDebtFlag(String badDebtFlag) {
    this.badDebtFlag = badDebtFlag;
  }

  public String getTaxItems() {
    return taxItems;
  }

  public void setTaxItems(String taxItems) {
    this.taxItems = taxItems;
  }

  public String getTaxable() {
    return taxable;
  }

  public void setTaxable(String taxable) {
    this.taxable = taxable;
  }

  public String getPaymentTerm() {
    return paymentTerm;
  }

  public void setPaymentTerm(String paymentTerm) {
    this.paymentTerm = paymentTerm;
  }

  public String getShippingTerm() {
    return shippingTerm;
  }

  public void setShippingTerm(String shippingTerm) {
    this.shippingTerm = shippingTerm;
  }

  public String getDeliveryTerm() {
    return deliveryTerm;
  }

  public void setDeliveryTerm(String deliveryTerm) {
    this.deliveryTerm = deliveryTerm;
  }

  public String getCurrencyCode() {
    return currencyCode;
  }

  public void setCurrencyCode(String currencyCode) {
    this.currencyCode = currencyCode;
  }

  public String getInvoiceto() {
    return invoiceto;
  }

  public void setInvoiceto(String invoiceto) {
    this.invoiceto = invoiceto;
  }

  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

}
