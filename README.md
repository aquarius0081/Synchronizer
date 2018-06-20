# Synchronizer
Application has two features:
1. Export DB table contents to XML file
2. Import XML file contents into DB table
 
Application uses such technologies as Java 8, Spring Boot, JDBC, log4j, Hibernate, MS SQL Server 2008 R2.

Table with name "Job" should be created in "Enterprise" DB.
Job table fields:
1. ID int (Primary key)
2. DepCode nvarchar(20)
3. DepJob nvarchar(100)
4. Description nvarchar(255)
(DepCode, DepJob) is a composite unique key

Usage is the following:
Export: test.bat export import.xml
Import: test.bat sync import.xml