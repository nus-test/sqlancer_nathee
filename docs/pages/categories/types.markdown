---
layout: minimal
title: Data Types
parent: Uncommon SQL Changes
nav_order: 2
---

# Data Types

| _ID_    | _Name_                                  | _Difference_            | _Action Taken_ | _DBMS_                      | _Description_                                                                   |
| :------ | :-------------------------------------- | :---------------------- | :------------- | :-------------------------- | :------------------------------------------------------------------------------ |
| **T01** | **Uncommon interger types**             | **Syntactic, Semantic** | **Remove**     | **MySQL, Postgres**         |                                                                                 |
|         | TINYINT                                 | Syntactic               | Remove         | MySQL                       |                                                                                 |
|         | MEDIUMINT                               | Syntactic               | Remove         | MySQL                       |                                                                                 |
|         | display width                           | Syntactic               | Remove         | MySQL                       | example: (4) from INT(4)                                                        |
|         | UNSIGNED                                | Syntactic               | Remove         | MySQL                       |                                                                                 |
|         | ZEROFILL                                | Syntactic               | Remove         | MySQL                       |                                                                                 |
|         | BIGSERIAL                               | Syntactic               | Remove         | Postgres                    |                                                                                 |
|         | SERIAL                                  | Semantic                | Remove         | Postgres                    | Max of Postgres SERIAL is different from the others                             |
|         | INTERGERS except BIGINT                 | Semantic                | Remove         | Postgres, MySQL,            | SQLite INT can go up to 8 bytes = BIGINT of others                              |
|         |                                         |                         |                |                             |                                                                                 |
| **T02** | **Uncommon string types**               | **Syntactic, Semantic** | **Remove**     | **Postgres, SQLite, MySQL** |                                                                                 |
|         | TINYTEXT                                | Syntactic               | Remove         | MySQL                       |                                                                                 |
|         | MEDIUMTEXT                              | Syntactic               | Remove         | MySQL                       |                                                                                 |
|         | LONGTEXT                                | Syntactic               | Remove         | MySQL                       |                                                                                 |
|         | TEXT                                    | Semantic                | Remove         | Postgres, SQLite, MySQL     | TEXT cannot be a key in MySQL                                                   |
|         | CHAR                                    | Semantic                | Remove         | Postgres                    | Postgres right pad CHAR but others don't                                        |
|         |                                         |                         |                |                             |                                                                                 |
| **T03** | **Uncommon special types**              | **Syntactic**           | **Remove**     | **Postgres, SQLite**        |                                                                                 |
|         | NAME                                    | Syntactic               | Remove         | Postgres                    |                                                                                 |
|         | int4range                               | Syntactic               | Remove         | Postgres                    |                                                                                 |
|         | MONEY                                   | Syntactic               | Remove         | Postgres                    |                                                                                 |
|         | VARYING                                 | Syntactic               | Remove         | Postgres                    | VARYING from BIT VARYING                                                        |
|         | inet                                    | Syntactic               | Remove         | Postgres                    |                                                                                 |
|         | BLOB                                    | Syntactic               | Remove         | SQLite                      |                                                                                 |
|         | NONE                                    | Syntactic               | Remove         | SQLite                      | NONE is empty type for column                                                   |
|         | BIT                                     | Syntactic               | Remove         | Postgres                    | BIT type is common but its value expression is not                              |
|         |                                         |                         |                |                             |                                                                                 |
| **T04** | **DOUBLE PRECISION type**               | **Syntactic**           | **Modify**     | **MySQL**                   | **Change DOUBLE to DOUBLE PRECISION, Postgres does not know DOUBLE**            |
|         |                                         |                         |                |                             |                                                                                 |
| **T05** | **DOUBLE PRECISION type**               | **Semantic**            | **Add**        | **SQLite, Postgres**        | **Add DOUBLE PRECISION as the main float type, other types behave differently** |
|         |                                         |                         |                |                             |                                                                                 |
| **T06** | **VARCHAR(255) type**                   | **Semantic**            | **Add**        | **SQLite**                  | **Add VARCHAR(255) as the main string type, other types behave differently**    |
|         |                                         |                         |                |                             |                                                                                 |
| **T07** | VARBINARY type                          | **Syntactic**           | **Add**        | **Syntactic**               | **Add VARBINARY as a possible column type to support binary collation**         |
|         |                                         |                         |                |                             |                                                                                 |
| **T08** | **Uncommon floating point types**       | **Semantic**            | **Remove**     | **Postgres, SQLite, MySQL** |                                                                                 |
|         | DECIMAL                                 | Semantic                | Remove         | Postgres, MySQL             | MySQL treats DECIMAL differently                                                |
|         | Floating points except DOUBLE PRECISION | Semantic                | Remove         | Postgres, SQLite, MySQL     | Postgres, MySQL treats float point types differently except DOUBLE PRECISION    |
