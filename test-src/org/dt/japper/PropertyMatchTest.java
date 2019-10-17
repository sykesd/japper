package org.dt.japper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.beans.PropertyDescriptor;

import org.dt.japper.testmodel.ModelWithEnum;
import org.junit.Test;

/*
 * Copyright (c) 2012-2013, David Sykes and Tomasz Orzechowski 
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

public class PropertyMatchTest {

  @Test
  public void simpleMatchTest() throws Exception {
    PropertyMatcher matcher = new PropertyMatcher(A.class);
    
    PropertyDescriptor[] path = matcher.match("ID", "A", "ID");
    assertNotNull(path);
    assertEquals(1, path.length);
    assertEquals("id", path[0].getName());
    
    path = matcher.match("DODGY", "A", "DODGY");
    assertNull(path);
    
    path = matcher.match("PART_DESCRIPTION", "PART", "DESCRIPTION");
    assertNotNull(path);
    assertEquals(2, path.length);
    assertEquals("part", path[0].getName());
    assertEquals("description", path[1].getName());
    
    path = matcher.match("THE_CURRENCY_DESCRIPTION", "CURRENCY", "DESCRIPTION");
    assertNotNull(path);
    assertEquals(2, path.length);
    assertEquals("theCurrency", path[0].getName());
    assertEquals("description", path[1].getName());
    
    path = matcher.match("THE_CURR_DESCRIPTION", "CURRENCY", "DESCRIPTION");
    assertNotNull(path);
    assertEquals(2, path.length);
    assertEquals("theCurrency", path[0].getName());
    assertEquals("description", path[1].getName());
    
    path = matcher.match("QTY", "A", "QTY");
    assertNotNull(path);
    assertEquals(1, path.length);
    assertEquals("qty", path[0].getName());
    
    path = matcher.match("QTY_MIL", "A", "QTY_MILEAGE");
    assertNotNull(path);
    assertEquals(1, path.length);
    assertEquals("qtyMileage", path[0].getName());
  }
  
  @Test
  public void fancyMatchTest() throws Exception {
    PropertyMatcher matcher = new PropertyMatcher(A.class);
    
    PropertyDescriptor[] path = matcher.match("CODE", "CURRENCY", "CODE");
    assertNotNull(path);
    assertEquals(3, path.length);
    assertEquals("part", path[0].getName());
    assertEquals("currency", path[1].getName());
    assertEquals("code", path[2].getName());
    
    path = matcher.match("DESCRIPTION", "CURRENCY", "DESCRIPTION");
    assertNotNull(path);
    assertEquals(1, path.length);
    assertEquals("description", path[0].getName());
    
  }
  
  @Test
  public void indexOutOfBoundsBugTest() {
    PropertyMatcher matcher = new PropertyMatcher(OrderHeader.class);

    PropertyDescriptor[] path = null;
    
    path = matcher.match("CUSTOMERSTORENO", "STORE", "CUSTOMERSTORENO");
    assertNotNull(path);
    
    path = matcher.match("CUST_ADDR_NAME1", "ADDRESS", "NAME1");
    assertNotNull(path);
  }
  
  /**
   * See https://github.com/sykesd/japper/issues/25
   */
  @Test
  public void issue25LongColumnNameBugTest() {
    PropertyMatcher matcher = new PropertyMatcher(OrderHeader.class);

    PropertyDescriptor[] path = null;
    
    path = matcher.match("S_B_SITECODE_PHYSICAL_WAREHOUSE", "", "BRA_SITECODE_PHYSICAL_WAREHOUSE");
    assertNotNull(path);
    assertEquals("store", path[0].getName());
    assertEquals("branch", path[1].getName());
    assertEquals("sitecodePhysicalWarehouse", path[2].getName());
    
    path = matcher.match("BRA_SITECODE_PHYSICAL_WAREHOUSE", "", "BRA_SITECODE_PHYSICAL_WAREHOUSE");
    assertNotNull(path);
    assertEquals("store", path[0].getName());
    assertEquals("branch", path[1].getName());
    assertEquals("sitecodePhysicalWarehouse", path[2].getName());
    
    path = matcher.match("SITECODE_PHYSICAL_WAREHOUSE", "", "SITECODE_PHYSICAL_WAREHOUSE");
    assertNotNull(path);
    assertEquals(3, path.length);
    assertEquals("store", path[0].getName());
    assertEquals("branch", path[1].getName());
    assertEquals("sitecodePhysicalWarehouse", path[2].getName());

    /*
     * The following is how we might actually want to alias this column to assign it
     * correctly, but we currently require an exact match to the last property.
     * TODO review the property matching rules so that the below alias test will pass
     * It is currently disabled since it fails and breaks the build
     */
//    path = matcher.match("STO_BRA_SITE_PHYS_WAREHOUSE", "", "STO_BRA_SITE_PHYS_WAREHOUSE");
//    assertNotNull(path);
//    assertEquals(3, path.length);
//    assertEquals("store", path[0].getName());
//    assertEquals("branch", path[1].getName());
//    assertEquals("sitecodePhysicalWarehouse", path[2].getName());
  }

  /**
   * See https://github.com/sykesd/japper/issues/36
   */
  @Test
  public void enumFieldsAreIgnored() {
    PropertyMatcher matcher = new PropertyMatcher(ModelWithEnum.class);

    PropertyDescriptor[] path = null;

    path = matcher.match("ID", "", "ID");
    assertNotNull(path);
    // If we get to here, then our enum field was successfully ignored!
  }
}
