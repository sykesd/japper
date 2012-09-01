Japper - A lightweight ResultSet-to-object mapping tool
=======================================================

This project is inspired by the [blog](http://samsaffron.com/archive/2011/03/30/How+I+learned+to+stop+worrying+and+write+my+own+ORM) [posts](http://samsaffron.com/archive/2011/09/05/Digging+ourselves+out+of+the+mess+Linq-2-SQL+created) [Sam Saffron](http://samsaffron.com/) of [Stack Overflow](http://stackoverflow.com/) fame wrote on the [Dapper .NET](http://code.google.com/p/dapper-dot-net/) library.

Where we are coming from:

* Strong level of comfort writing SQL
* Existing database schema with many aspects that do not conform to what many libraries expect, and so require lots of boiler plate to accommodate (e.g. compound primary keys)
* Lots more code devoted to reading than writing
* Suffering under N+1 query problems using Hibernate

The goals of the project are:

* Zero configuration, or as close to zero as possible
* Avoid N+1 query problems
* Be a library, not a framework - i.e. impose as few restrictions as possible on the objects used



