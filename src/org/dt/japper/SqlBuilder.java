package org.dt.japper;

import java.util.HashMap;
import java.util.Map;

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
 * A shameless rip-off of the Dapper .NET SqlBuilder class
 * See http://samsaffron.com/archive/2011/09/05/Digging+ourselves+out+of+the+mess+Linq-2-SQL+created
 * 
 * The syntax is as follows:
 * Your SQL template can have placeholders that can be replaced by calling replaceFragment
 *    select * from table /**where**/ /**orderby**/           
/**       
 * The SQL statement itself is still a valid statement even in its templated form
 * The id to be replaced is whatever is inside the comment markers, e.g. where and orderby
 * 
 * Each instance of SqlBuilder can manage multiple statements, and the fragments are
 * replaced on mass. The canonical use for this is a query to count the number of results
 * and the query to retrieve the results. You would have the same fragement id's in each query
 * and add both to the same SqlBuilder instance. Then you can add the necessary conditions to
 * both queries at the same time.
 * 
 * The addTemplate() method returns an instance of Template. You can keep a reference to it
 * for convenience, but it remains owned by its SqlBuilder instance, and replacement of the
 * fragments is performed through it
 * 
 *
 */
public class SqlBuilder {

  private Map<String, Template> templateMap = new HashMap<String, Template>();
  
  public Template addTemplate(String id, String sqlTemplate) {
    templateMap.put(id, new Template(sqlTemplate));
    return templateMap.get(id);
  }
  
  public void replaceFragment(String id, String fragment) {
    for (Template template : templateMap.values()) {
      template.replaceFragment(id, fragment);
    }
  }
  
  public String getSql(String id) {
    return templateMap.get(id).getSql();
  }
  
  public void reset() {
    for (Template template : templateMap.values()) {
      template.reset();
    }
  }
  
}
