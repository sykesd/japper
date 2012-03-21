package org.dt.japper;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

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

public class TestParameterParser {

  @Test
  public void parserTests() {
    String sql = "select * from users where sso_user_name = :ssoUserName";
    ParameterParser pp = new ParameterParser(sql).parse();
    assertEquals("select * from users where sso_user_name = ?", pp.getSql());
    assertEquals(1, pp.getIndexes("ssoUsername").size());
    assertEquals(Integer.valueOf(1), pp.getIndexes("SSOUSERNAME").get(0));
    
    sql = "select a, ':' b from c where :d = 'some string:'";
    pp = new ParameterParser(sql).parse();
    assertEquals("select a, ':' b from c where ? = 'some string:'", pp.getSql());
    
    sql = "select a from b where c = :p and d = :p";
    pp = new ParameterParser(sql).parse();
    assertEquals("select a from b where c = ? and d = ?", pp.getSql());
    assertEquals(2, pp.getIndexes("p").size());
  }
}
