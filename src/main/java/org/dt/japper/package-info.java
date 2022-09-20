/**
 * <h1>Japper - A lightweight object-relational mapping tool.</h1>
 *
 * <p>
 *   Inspired heavily by the
 *   <a href="https://github.com/DapperLib/Dapper">Dapper .NET</a>
 *   project.
 * </p>
 *
 * <p>
 *   This library provides a very configuration-less, easy to use mechanism by which
 *   the results of SQL queries can be mapped onto Java model classes.
 * </p>
 * <p>
 *   Its emphasis is fully on reading data, and not on writing data back. It provides
 *   no mechanisms for tracking changes to the returned models for persisting back
 *   to the database.
 * </p>
 *
 * <p>
 *   The main entry point into the library is the {@link org.dt.japper.Japper} class
 *   and the {@code static} methods {@code query(...)} and {@code queryOne(...)}.
 * </p>
 *
 * <h2>Simple example</h2>
 *
 * Assume we have a simple model class like:
 * <pre style="code">
 *   public class Product {
 *     private String id;
 *     private String description;
 *     private BigDecimal qtyOnHand;
 *
 *     public Product() {} // default constructor is required!
 *
 *     // ... all the standard JavaBean getters and setters elided
 *   }
 * </pre>
 *
 * With a SQL statement like:
 * <pre style="code">
 *   private static final String SQL_PRODUCTS_ON_HAND =
 *       "   SELECT products.id, products.description, products.qty_on_hand " +
 *       "     FROM products " +
 *       "    WHERE products.qty_on_hand &gt; 0 " +
 *       " ORDER BY products.qty_on_hand DESC ";
 * </pre>
 *
 * We can obtain all the results of the query in instances of {@code Product}
 * in a single code line:
 * <pre style="code">
 *   List&lt;Product&gt; productsOnHand = Japper.query(conn, Product.class, SQL_PRODUCTS_ON_HAND);
 * </pre>
 *
 * <p>
 *   The queries can be parameterized with named parameters using the {@code :NAME} notation,
 *   and the values provided in the call to {@code Japper.query(...)}.
 * </p>
 *
 * <h2>Non-goals</h2>
 *
 * The {@code Japper} library is aimed solely at the task of munging the results of queries into
 * Java model objects. It delegates all transaction handling to the calling application.
 * <p>
 *   In addition, support for inserting/updating data in the database is restricted to executing
 *   parameterized
 * </p>
 */
package org.dt.japper;