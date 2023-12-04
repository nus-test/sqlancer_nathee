---
layout: minimal
title: CREATE INDEX
parent: Uncommon SQL Changes
nav_order: 5
---

# CREATE INDEX

| _ID_     | _Name_                          | _Difference_            | _Action Taken_  | _DBMS_              | _Description_                                                                  |
| :------- | :------------------------------ | :---------------------- | :-------------- | :------------------ | :----------------------------------------------------------------------------- |
| **CI01** | **Fully qualified table name**  | **Syntactic**           | **Add**         | **MySQL**           | **Add option to disable fully qualified table names**                          |
|          |                                 |                         |                 |                     |                                                                                |
| **CI02** | **Uncommon index expressions**  | **Syntactic, Semantic** | **Add, Remove** | **MySQL, SQLite**   |                                                                                |
|          | Index expression inner bracket  | Syntactic               | Add             | SQLite              | SQLite accept (expression) but others only accept ((expression))               |
|          | Null index expression           | Syntactic               | Add             | SQLite              | Add option to disable null expression, Postgres, MySQL do not allow null index |
|          | Text index expression           | Semantic                | Remove          | MySQL               | Postgres and SQLite confuse text expressions as column names                   |
|          |                                 |                         |                 |                     |                                                                                |
| **CI03** | **Uncommon index sort options** | **Syntactic**           | **Remove**      | **Postgres**        |                                                                                |
|          | NULLS FIRST                     | Syntactic               | Remove          | Postgres            |                                                                                |
|          | NULLS LAST                      | Syntactic               | Remove          | Postgres            |                                                                                |
|          |                                 |                         |                 |                     |                                                                                |
| **CI04** | **Uncommon clauses**            | **Syntactic**           | **Remove**      | **Postgre, SQLite** |                                                                                |
|          | INCLUDE                         | Syntactic               | Remove          | Postgres            |                                                                                |
|          | WHERE                           | Syntactic               | Remove          | Postgres, SQLite    | MySQL does not support WHERE here                                              |
|          | COLLATE                         | Syntactic               | Remove          | SQLite              | MySQL does not support COLLATE here                                            |
|          | IF NOT EXISTS                   | Syntactic               | Remove          | SQLite              | MySQL does not support IF NOT EXISTS here                                      |
|          |                                 |                         |                 |                     |                                                                                |
| **CI05** | **Other specific options**      | **Syntactic**           | **Remove**      | **MySQL, Postgres** |                                                                                |
|          | Index length                    | Syntactic               | Remove          | MySQL               |                                                                                |
|          | Index type                      | Syntactic               | Remove          | MySQL, Postgres     |                                                                                |
|          | Index option                    | Syntactic               | Remove          | MySQL               |                                                                                |
|          | algorithm options               | Syntactic               | Remove          | MySQL               |                                                                                |
|          | USING                           | Syntactic               | Remove          | MySQL, Postgres     |                                                                                |
|          | opclass                         | Syntactic               | Remove          | Postgres            |                                                                                |
|          | ONLY                            | Syntactic               | Remove          | Postgres            |                                                                                |
