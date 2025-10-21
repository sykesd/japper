# Japper Quickstart Guide

Clone this repository, `./gradlew build`, and add the generated `.jar` file as a compile-time dependency of your Java project.

Given a database schema of the form ([HSQLDB](https://hsqldb.org/) syntax):
```hsqldb
CREATE TABLE currency (
     currency_code   VARCHAR(3) PRIMARY KEY
   , currency_symbol CHAR(6)
   , description     VARCHAR(60)
 )
```

Create a Java class like the following:
```java
public class Currency {
  private Integer currencyCode;
  private String currencySymbol;
  private String description;
  
  public Integer getCurrencyCode() { return currencyCode; }
  public void setCurrencyCode(Integer value) { this.currencyCode = value; }
  
  public String getCurrencySymbol() { return currencySymbol; }
  public void setCurrencySymbol(String value) { this.currencySymbol = value; }
  
  public String getDescription() { return description; }
  public void setDescription(String value) { this.description = value; }
}
```

Then, at the point in your code where you would like to load data from the database, call `Japper` to execute the query:
```java
static final String SQL_ALL_CURRENCIES = """
        select *
          from currency
        """;

java.sql.Connection conn = ...; // However you get it

List<Currency> currencies = Japper.query(conn, Currency.class, SQL_ALL_CURRENCIES);
// ...do stuff with currencies
```

