package org.dt.japper;

import java.util.HashMap;
import java.util.Map;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/*
 * Copyright (c) 2012-2022, David Sykes and Tomasz Orzechowski
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
 * A class for handling templated SQL statements, providing methods for easily replacing fragments
 * of a query at runtime.
 *
 * <p>
 *   Inspired heavily by the similarly named class in
 *   <a href="https://github.com/DapperLib/Dapper">Dapper .NET</a>.
 * </p>
 *
 * <p>
 *   Your SQL statement, most likely a string literal in your source code, may contain embedded
 *   comments of the form: <code>/**fragment_name**&#47;</code>. Using the
 *   {@link #replaceFragment(String, String)} you can then replace that entire comment with other
 *   text at runtime.
 * </p>
 *
 * <p>
 *   Consider a string-literal in your code that is the SQL statement:
 * </p>
 * <code>select * from table /**where**&#47; /**orderby**&#47;</code>
 *
 * <p>
 *   The SQL statement, as is, is a perfectly valid SQL statement, and is designed to be able
 *   to "round-trip" between your Java IDE and an external database tool, like TOAD. You can
 *   then insert further clauses into the statement at using the name inside the comment markers,
 *   e.g. {@code where} and {@code orderby}, in the example above.
 * </p>
 *
 * <p>
 * Each instance of {@code SqlBuilder} can manage multiple statements, and the fragments are
 * replaced en masse. The canonical use for this is a query to count the number of results
 * and the query to retrieve (some of) the results. You would have the same fragment ID's in each
 * query and add both to the same {@code SqlBuilder} instance. Then you can add the necessary conditions
 * to both queries at the same time.
 * </p>
 *
 * <p>
 * The {@link #addTemplate(String, String)} method returns an instance of {@link Template}. You can keep
 * a reference to it for convenience, but it remains owned by its {@code SqlBuilder} instance, and
 * replacement of the fragments is performed through its owning {@code SqlBuilder}
 * </p>
 */
@API(status = Status.STABLE)
public class SqlBuilder {

  private final Map<String, Template> templateMap = new HashMap<>();

  @API(status = Status.STABLE)
  public Template addTemplate(String id, String sqlTemplate) {
    templateMap.put(id, new Template(sqlTemplate));
    return templateMap.get(id);
  }

  @API(status = Status.STABLE)
  public void replaceFragment(String id, String fragment) {
    for (Template template : templateMap.values()) {
      template.replaceFragment(id, fragment);
    }
  }

  @API(status = Status.STABLE)
  public String getSql(String id) {
    return templateMap.get(id).getSql();
  }

  @API(status = Status.STABLE)
  public void reset() {
    for (Template template : templateMap.values()) {
      template.reset();
    }
  }
  
}
