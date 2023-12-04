---
layout: minimal
title: SELECT
parent: Uncommon SQL Changes
nav_order: 6
---

# SELECT

| _ID_    | _Name_                                | _Difference_  | _Action Taken_ | _DBMS_                      | _Description_                                                                         |
| :------ | :------------------------------------ | :------------ | :------------- | :-------------------------- | :------------------------------------------------------------------------------------ |
| **S01** | **Uncommon JOIN**                     | **Syntactic** | **Remove**     | **Postgres, SQLite**        |                                                                                       |
|         | NATURAL JOIN ON                       | Syntactic     | Remove         | SQLite                      | NATURAL JOIN is still present, but not with ON                                        |
|         | CROSS JOIN ON                         | Syntactic     | Remove         | SQLite                      | CROSS JOIN is still present, but not with ON                                          |
|         | FULL OUTER JOIN                       | Syntactic     | Remove         | Postgres, SQLite            |                                                                                       |
|         |                                       |               |                |                             |                                                                                       |
| **S02** | **Uncommon aggregate functions**      | **Syntactic** | **Remove**     | **Postgres, SQLite**        |                                                                                       |
|         | BIT_AND                               | Syntactic     | Remove         | Postgres                    |                                                                                       |
|         | BIT_OR                                | Syntactic     | Remove         | Postgres                    |                                                                                       |
|         | BOOL_AND                              | Syntactic     | Remove         | Postgres                    |                                                                                       |
|         | BOOL_OR                               | Syntactic     | Remove         | Postgres                    |                                                                                       |
|         | EVERY                                 | Syntactic     | Remove         | Postgres                    |                                                                                       |
|         | GROUP_CONCAT                          | Syntactic     | Remove         | SQLite                      |                                                                                       |
|         | TOTAL                                 | Syntactic     | Remove         | SQLite                      |                                                                                       |
|         |                                       |               |                |                             |                                                                                       |
| **S03** | **Locking clause**                    | **Syntactic** | **Remove**     | **Postgres**                |                                                                                       |
|         | FOR                                   | Syntactic     | Remove         | Postgres                    |                                                                                       |
|         | UPDATE                                | Syntactic     | Remove         | Postgres                    |                                                                                       |
|         | NO KEY UPDATE                         | Syntactic     | Remove         | Postgres                    |                                                                                       |
|         | SHARE                                 | Syntactic     | Remove         | Postgres                    |                                                                                       |
|         | KEY SHARE                             | Syntactic     | Remove         | Postgres                    |                                                                                       |
|         |                                       |               |                |                             |                                                                                       |
| **S04** | **Uncommon window functions options** | **Syntactic** | **Remove**     | **SQLite**                  |                                                                                       |
|         | FILTER                                | Syntactic     | Remove         | SQLite                      |                                                                                       |
|         | EXCLUDE NO OTHERS                     | Syntactic     | Remove         | SQLite                      |                                                                                       |
|         | EXCLUDE CURRENT ROW                   | Syntactic     | Remove         | SQLite                      |                                                                                       |
|         | EXCLUDE GROUP                         | Syntactic     | Remove         | SQLite                      |                                                                                       |
|         | EXCLUDE TIES                          | Syntactic     | Remove         | SQLite                      |                                                                                       |
|         | GROUPS                                | Syntactic     | Remove         | SQLite                      |                                                                                       |
|         |                                       |               |                |                             |                                                                                       |
| **S05** | **Uncommon ORDER BY options**         | **Syntactic** | **Remove**     | **Postgres, SQLite**        |                                                                                       |
|         | NULLS FIRST                           | Syntactic     | Remove         | Postgres, SQLite            |                                                                                       |
|         | NULLS LAST                            | Syntactic     | Remove         | Postgres, SQLite            |                                                                                       |
|         | Expressions in ORDER BY               | Syntactic     | Remove         | SQLite                      | Postgres does not allow expressions in ORDER BY unless it is specified in select list |
|         |                                       |               |                |                             |                                                                                       |
| **S06** | **Other specific options**            | **Syntactic** | **Remove**     | **SQLite, MySQL, Postgres** |                                                                                       |
|         | NOT INDEXED                           | Syntactic     | Remove         | SQLite                      |                                                                                       |
|         | INDEXED BY                            | Syntactic     | Remove         | SQLite                      |                                                                                       |
|         | DISTINCTROW                           | Syntactic     | Remove         | MySQL                       |                                                                                       |
|         | ONLY                                  | Syntactic     | Remove         | Postgres                    |                                                                                       |
|         | \*                                    | Syntactic     | Remove         | Postgres                    | This \* is the one after table name, for including descendant tables                  |
|         | ON                                    | Syntactic     | Remove         | Postgres                    |                                                                                       |
