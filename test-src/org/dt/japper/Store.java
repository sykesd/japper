package org.dt.japper;

import java.math.BigDecimal;

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

public class Store {

  private String sitecode;
  
  private BigDecimal storeno;
  
  private String sitecodeCustomer;

  private String customerno;

  private String customerstoreno;
  
  private String customergroupno;
  
  private String status;
  
  private String creditStatus;
  
  private BigDecimal mmZone;
  
  private String sitecodeShipFrom;
  
  private String deliveryTerm;
  
  private Address address;

  private Branch branch;
  
  public String getSitecode() {
    return sitecode;
  }

  public void setSitecode(String sitecode) {
    this.sitecode = sitecode;
  }

  public BigDecimal getStoreno() {
    return storeno;
  }

  public void setStoreno(BigDecimal storeno) {
    this.storeno = storeno;
  }

  public String getSitecodeCustomer() {
    return sitecodeCustomer;
  }

  public void setSitecodeCustomer(String sitecodeCustomer) {
    this.sitecodeCustomer = sitecodeCustomer;
  }

  public String getCustomerno() {
    return customerno;
  }

  public void setCustomerno(String customerno) {
    this.customerno = customerno;
  }

  public String getCustomerstoreno() {
    return customerstoreno;
  }

  public void setCustomerstoreno(String customerstoreno) {
    this.customerstoreno = customerstoreno;
  }

  public String getCustomergroupno() {
    return customergroupno;
  }

  public void setCustomergroupno(String customergroupno) {
    this.customergroupno = customergroupno;
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

  public BigDecimal getMmZone() {
    return mmZone;
  }

  public void setMmZone(BigDecimal mmZone) {
    this.mmZone = mmZone;
  }

  public String getSitecodeShipFrom() {
    return sitecodeShipFrom;
  }

  public void setSitecodeShipFrom(String sitecodeShipFrom) {
    this.sitecodeShipFrom = sitecodeShipFrom;
  }

  public String getDeliveryTerm() {
    return deliveryTerm;
  }

  public void setDeliveryTerm(String deliveryTerm) {
    this.deliveryTerm = deliveryTerm;
  }

  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public Branch getBranch() {
    return branch;
  }

  public void setBranch(Branch branch) {
    this.branch = branch;
  }

  
}
