package org.dt.japper;

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
 * Helper class for the SqlBuilder. This holds an individual SQL template
 * It state is altered through the public methods of the SqlBuilder class
 * 
 */
public class Template {

  private final String template;
  private String sql;
  
  Template(String template) {
    this.template = template;
    this.sql = template;
  }

  /**
   * Return the current state of the SQL with any placeholders replaced by
   * calls to SqlBuilder.replaceFragment()
   * 
   * @return the current SQL statement
   */
  public String getSql() {
    return sql;
  }

  /**
   * Return this SQL statement to its template state
   * This method should only be called by the SqlBuilder.reset() method
   */
  void reset() { 
    this.sql = this.template;
  }
  
  /**
   * Replace all occurrences of th the given id with the given replacement string,
   * if the id is defined in the template
   * id is case-sensitive
   * 
   * @param id the id to replace
   * @param replacement the value to replace the placeholder with
   */
  void replaceFragment(String id, String replacement) {
    sql = sql.replace("/**"+id+"**/", replacement);
  }
}
