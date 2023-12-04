---
layout: default
title: Different Behaviors
---

# Different Behaviors

This page lists out the differences in behaviors of MySQL, PostgreSQL, and SQLite that may not be a bug.

---

## Bitwise Operation on Integers
When using bitwise operators on an interger value, MySQL behavior is different from the other two. MySQL treats the number as unsigned integer by default while PostgreSQL and SQLite treat the number as signed interger.
```sql
mysql> SELECT ~1; -- returns 18446744073709551614
mysql> SELECT CAST(~1 AS SIGNED); -- returns -2
postgresql> SELECT ~1; -- returns -2
sqlite> SELECT ~1; -- returns -2

mysql> SELECT -5 | -1; -- returns 18446744073709551615
mysql> SELECT CAST(-5 | -1 AS SIGNED); -- returns -1
postgresql> SELECT -5 | -1; -- returns -1
sqlite> SELECT -5 | -1; -- returns -1
```

---

## Handling Integers Operations
When doing operations with integer values, PostgreSQL and SQLite treat the value as integer for every operations done, but MySQL only treats the value as integer when explicitly specified (e.g storing to an integer column, casted as integer) by rounding the floating point not truncating it. See <https://bugs.mysql.com/bug.php?id=9440>.
```sql
mysql> SELECT 5/2 -- returns 2.5000
mysql> SELECT CAST(5/2 AS SIGNED); -- returns 3
postgresql> SELECT 5/2; -- returns 2
sqlite> SELECT 5/2; -- returns 2

mysql> SELECT 5/2*2; -- returns 5.0000
postgresql> SELECT 5/2*2; -- returns 4
sqlite> SELECT 5/2*2; -- returns 4
```

---

## Comparing String and Numeric Values
When comparing numeric value with a string of the same number, SQLite returns false while others return true. However, SQLite will still implicitly cast string values to numeric values when used with other operators. See <https://www.sqlite.org/quirks.html#sqlite_distinguishes_between_integer_and_text_literals>.
```sql
mysql> SELECT 1 = '1'; -- returns true
postgresql> SELECT 1 = '1'; -- returns true
sqlite> SELECT 1 = '1'; -- returns false

mysql> SELECT 1 + '1'; -- returns 2
postgresql> SELECT 1 + '1'; -- returns 2
sqlite> SELECT 1 + '1'; -- returns 2
```

---

## Unary Prefix Operator of String Expressions
When using unary prefix operators (i.e +, -) in front of a string expressions with numeric values, PostgreSQL treats the value inconsistently.
```sql
mysql> SELECT +'4'; -- returns 4
postgresql> SELECT +'4'; -- returns 4
sqlite> SELECT +'4'; -- returns 4

mysql> SELECT '4'/3; -- returns 1.3333333333333333
postgresql> SELECT '4'/3; -- returns 1
sqlite> SELECT '4'/3; -- returns 1

mysql> SELECT +'4'/3; -- returns 1.3333333333333333
postgresql> SELECT +'4'/3; -- returns 1.3333333333333333
sqlite> SELECT +'4'/3; -- returns 1

mysql> SELECT +'4'/3; -- returns -1.3333333333333333
postgresql> SELECT +'4'/3; -- ERROR:  operator is not unique: - unknown
sqlite> SELECT +'4'/3; -- returns -1
```
When using + in front of a string with numeric values in PostgreSQL, the type becomes floating point, resulting in differences in '4'/3 and +'4'/3. Using - in front of the string also causes an error. However, that might have been from strict typing of PostgreSQL.

---

## Right Padding of Fixed-length Character Types
When using CHAR(N) data type, Postgres will right pad with whitespace to the N length, MySQL will also right pad the value when stored, but will trim the whitespace when retrieving the value, SQLite does not enforce any length on the value. This is the same when casting to CHAR(N). Additionally, CHAR without N is default to CHAR(1) in PostgreSQL. See <https://dev.mysql.com/doc/refman/8.0/en/char.html> and <https://www.postgresql.org/docs/current/datatype-character.html>.
```sql
CREATE TABLE t0 (c0 CHAR(10));
INSERT INTO t0 VALUES('a');
mysql> SELECT * FROM t0; -- returns 'a'
postgresql> SELECT * FROM t0; -- returns 'a         '
sqlite> SELECT * FROM t0; -- returns 'a'

mysql> SELECT CAST('abc' AS CHAR); -- returns 'abc'
postgresql> SELECT CAST('abc' AS CHAR); -- returns 'a'
sqlite> SELECT CAST('abc' AS CHAR); -- returns 'abc'

mysql> SELECT CAST('abc' AS CHAR(10)); -- returns 'abc'
postgresql> SELECT CAST('abc' AS CHAR(10)); -- returns  'abc       '
sqlite> SELECT CAST('abc' AS CHAR(10)); -- returns 'abc'
```

---

## Using Null in CASE
When using NULL value in CASE, PostgreSQL treat NULL as text type by default while the other 2 treat it as whatever type of the value it is compared with. See <https://www.postgresql.org/docs/current/typeconv-union-case.html>.
```sql
mysql> SELECT CASE NULL WHEN 'a' THEN TRUE ELSE FALSE END; -- returns false
postgresql> SELECT CASE NULL WHEN 'a' THEN TRUE ELSE FALSE END; -- returns false
sqlite> SELECT CASE NULL WHEN 'a' THEN TRUE ELSE FALSE END; -- returns false

mysql> SELECT CASE NULL WHEN 5 THEN TRUE ELSE FALSE END; -- returns false
postgresql> SELECT CASE NULL WHEN 5 THEN TRUE ELSE FALSE END; -- ERROR:  operator does not exist: text = integer
sqlite> SELECT CASE NULL WHEN 5 THEN TRUE ELSE FALSE END; -- returns false
```

---

## Referencing Temporary Table
MySQL will output an error when refering to the same temporary table twice in the same query. This is not the case for the other 2 systems. See <https://dev.mysql.com/doc/refman/8.0/en/temporary-table-problems.html>.
```sql
CREATE TEMPORARY TABLE t0 (c0 INT);
mysql> SELECT * FROM t0 UNION SELECT * FROM t0; -- ERROR 1137 (HY000): Can't reopen table: 't0'
postgresql> SELECT CASE NULL WHEN 5 THEN TRUE ELSE FALSE END; -- Query OK
sqlite> SELECT CASE NULL WHEN 5 THEN TRUE ELSE FALSE END; -- Query OK
```

---

## Type Casting
MySQL expressions can be casted to only some of the data types it supports while the other 2 systems can cast expressions to every type they support. See <https://dev.mysql.com/doc/refman/8.0/en/cast-functions.html#cast-function-descriptions>.
```sql
CREATE TABLE t0 (c0 INT); -- Query OK
mysql> SELECT CAST(3.2 AS INT); -- ERROR 1064 (42000): You have an error in your SQL syntax; check the manual that corresponds to your MySQL server version for the right syntax to use near 'INT)' at line 1
postgresql> SELECT CAST(3.2 AS INT); -- returns 3
sqlite> SELECT CAST(3.2 AS INT); -- returns 3

mysql> SELECT CAST(3 AS DOUBLE PRECISION); -- returns 3
postgresql> SELECT CAST(3 AS DOUBLE PRECISION); -- returns 3
sqlite> SELECT CAST(3 AS DOUBLE PRECISION); -- returns 3.0
```

---

## Foreign Keys in Temporary Tables
MySQL does not allow using foreign keys in temporary tables. PostgreSQL allows this only if the referenced table is also a temporary table. SQLite does not have any restrictitons with foreign keys in temporary tables. See <https://dev.mysql.com/doc/refman/8.0/en/create-table-foreign-keys.html#foreign-key-restrictions>.
```sql
CREATE TABLE t0 (c0 INT); -- Query OK
mysql> CREATE TEMPORARY TABLE t1 (c0, FOREIGN KEY (c0) REFERENCES t0(c0)); -- 1215 (HY000): Cannot add foreign key constraint
postgresql> CREATE TEMPORARY TABLE t1 (c0, FOREIGN KEY (c0) REFERENCES t0(c0)); -- constraints on temporary tables may reference only temporary tables
sqlite> CREATE TEMPORARY TABLE t1 (c0, FOREIGN KEY (c0) REFERENCES t0(c0)); -- Query OK

CREATE TEMPORARY TABLE t0 (c0 INT); -- Query OK
mysql> CREATE TEMPORARY TABLE t1 (c0, FOREIGN KEY (c0) REFERENCES t0(c0)); -- 1215 (HY000): Cannot add foreign key constraint
postgresql> CREATE TEMPORARY TABLE t1 (c0, FOREIGN KEY (c0) REFERENCES t0(c0)); -- Query OK
sqlite> CREATE TEMPORARY TABLE t1 (c0, FOREIGN KEY (c0) REFERENCES t0(c0)); -- Query OK

```

---

## Inline REFERENCES Constraint
MySQL ignores REFERENCES defined as part of the column specification. It accepts only when REFERENCES is defined as part of a separate FOREIGN KEY clause. See <https://dev.mysql.com/doc/mysql-reslimits-excerpt/8.0/en/ansi-diff-foreign-keys.html>.
```sql
mysql> CREATE TABLE t0 (c0 INT, FOREIGN KEY (c0) REFERENCES c1); -- 3734 (HY000): Failed to add the foreign key constraint. Missing column 'c1' for constraint 't0_ibfk_1' in the referenced table 't0'
mysql> CREATE TABLE t0 (c0 INT REFERENCES c1); -- QUERY OK because REFERENCES is ignored
```

---

## Counting with Length Function
Length function in MySQL and PostgreSQL count a multibyte character as multiple bytes. However, SQLite's Length function counts multibyte character as one. SQLite can count multibyte characters as multiple bytes only if it is a GLOB value which is not usable with other DBMSs. See <https://dev.mysql.com/doc/refman/8.0/en/string-functions.html#function_length>, <https://www.postgresql.org/docs/16/functions-string.html>, <https://www.sqlite.org/lang_corefunc.html#length>.
```sql
mysql> SELECT LENGTH('쉕'); -- returns 3
postgres> SELECT LENGTH('쉕'); -- returns 3
sqlite> SELECT LENGTH('쉕'); -- returns 1
```

---

## Casting Boolean values to Text value
Default boolean values for MySQL and SQLite are 1 or 0. However, for PostgreSQL, they are true and false. If these values are casted to text values and concatenated with another text value, they would be different. See <https://www.postgresql.org/docs/16/datatype-boolean.html>, <https://dev.mysql.com/doc/refman/8.0/en/boolean-literals.html>, <https://www.sqlite.org/lang_expr.html#boolean_expressions>.
```sql
mysql> SELECT CAST(1=1 AS CHAR) || 'a'; -- returns '1a'
postgres> SELECT CAST(1=1 AS CHAR) || 'a'; -- returns 'ta'
sqlite> SELECT CAST(1=1 AS CHAR) || 'a'; -- returns '1a'
```

---

## Default Escaping with LIKE Operator
In PostgreSQL, LIKE operator implicitly escapes backslash ('\\') from the second operand. This is not the case in MySQL and SQLite. See <https://www.postgresql.org/docs/current/functions-matching.html#FUNCTIONS-LIKE>.
```sql
mysql> SELECT '\a' LIKE '\a'; -- returns true
postgres> SELECT '\a' LIKE '\a'; -- returns false because it's the same as SELECT '\a' LIKE 'a'
sqlite> SELECT '\a' LIKE '\a'; -- returns true
```