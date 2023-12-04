---
layout: default
title: Configurations Changed
---

# Configurations Changed

This page lists out configurations/options/modes/pragmas changed that make all 3 systems behave in the same way.

---

## Backslash Escape
MySQL use backslash character (\\) as an escape character by default. Using "NO_BACKSLASH_ESCAPES" mode disable this usage, making it behave the same way as the other 2 systems. 
```sql
SELECT '\a'; -- returns a
SET sql_mode = 'NO_BACKSLASH_ESCAPES';
SELECT '\a'; -- returns \a
```

---

## Pipe ( || ) Operator
MySQL pipe operator is used as OR operator by default. Using "PIPES_AS_CONCAT" mode will make it behave as a string concatenating operator, similar to the other 2 systems.
```sql
SELECT '1' || '1'; -- returns 1 ('1' is treated as boolean value true)
SET sql_mode = 'PIPES_AS_CONCAT';
SELECT '1' || '1'; -- returns 11
```
---

## Number of Found Rows
MySQL UPDATE statement returns the amount of changed rows. However, the other 2 systems return amount of found rows. These amounts can be different because a found row for update can have the same value as the update value, meaning the row value is not changed. Using "CLIENT_FOUND_ROWS" client flag will change the amount returned as found rows instead.
```sql
CREATE TABLE t0 (c0 int);
INSERT INTO t0 VALUES(1);
INSERT INTO t0 VALUES(2);
UPDATE t0 SET c0 = 2 WHERE c0 > 0; -- returns 1 changed rows

-- Set "CLIENT_FOUND_ROWS" flag
UPDATE t0 SET c0 = 2 WHERE c0 > 0; -- returns 2 found rows
```

---

## Default  Collation
To have the same behavior when comparing and sorting texts, we change the default collation of MySQL to "BINARY". We also change the default collation of PostgreSQL to "C". These collations compare and sort characters by their byte values.
```
MySQL: we used "mysql_con.set_charset_collation(charset='binary')" in the testing script 
       and "--character-set-server=binary --collation-server=binary" for docker compose command.
PostgreSQL: we configured "--locale=C" for the "POSTGRES_INITDB_ARGS" docker environment variable.
SQLite: SQLite already has "BINARY" as its default collation.
```

---

## Like Case Sensitivity
With the above collation configurations, LIKE operators are case sensitive except for SQLite. "case_sensitive_like" pragma can be used to configure it to be case sensitive.
```sql
SELECT 'a' LIKE 'A'; -- return true
PRAGMA case_sensitive_like = true;
SELECT 'a' LIKE 'A'; -- return false
```


