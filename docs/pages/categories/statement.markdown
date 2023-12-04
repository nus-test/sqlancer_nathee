---
layout: minimal
title: Statements
parent: Uncommon SQL Changes
nav_order: 1
---

# Statements

| _ID_     | _Name_                                    | _Difference_  | _Action Taken_ | _DBMS_                      | _Description_ |
| :------- | :---------------------------------------- | :------------ | :------------- | :-------------------------- | :------------ |
| **ST01** | **Uncommon Data Definition statements**   | **Syntactic** | **Remove**     | **MySQL, Postgres, SQLite** |               |
|          | CREATE STATISTICS                         | Syntactic     | Remove         | Postgres                    |               |
|          | DROP STATISTICS                           | Syntactic     | Remove         | Postgres                    |               |
|          | CREATE SEQUENCE                           | Syntactic     | Remove         | Postgres                    |               |
|          | CREATE VIRTUAL TABLE                      | Syntactic     | Remove         | SQLite                      |               |
|          | TRUNCATE                                  | Syntactic     | Remove         | MySQL, Postgres             |               |
|          | REINDEX                                   | Syntactic     | Remove         | Postgres, SQLite            |               |
|          | CLUSTER                                   | Syntactic     | Remove         | Postgres                    |               |
|          | COMMENT                                   | Syntactic     | Remove         | Postgres                    |               |
|          | SET CONSTRAINTS                           | Syntactic     | Remove         | Postgres                    |               |
|          |                                           |               |                |                             |               |
| **ST02** | **Uncommon Data Manipulation statements** | **Syntactic** | **Remove**     | **MySQL, SQLite**           |               |
|          | REPLACE                                   | Syntactic     | Remove         | MySQL                       |               |
|          | INSERT into dbms-specific tables          | Syntactic     | Remove         | SQLite                      |               |
|          |                                           |               |                |                             |               |
| **ST03** | **Uncommon Administrative statements**    | **Syntactic** | **Remove**     | **MySQL, Postgres, SQLite** |               |
|          | FLUSH                                     | Syntactic     | Remove         | MySQL                       |               |
|          | RESET                                     | Syntactic     | Remove         | MySQL, Postgres             |               |
|          | SHOW                                      | Syntactic     | Remove         | MySQL                       |               |
|          | SET                                       | Syntactic     | Remove         | MySQL, Postgres             |               |
|          | REPAIR                                    | Syntactic     | Remove         | MySQL                       |               |
|          | OPTIMIZE                                  | Syntactic     | Remove         | MySQL                       |               |
|          | CHECKSUM                                  | Syntactic     | Remove         | MySQL                       |               |
|          | ANALYZE TABLE                             | Syntactic     | Remove         | MySQL                       |               |
|          | ANALYZE                                   | Syntactic     | Remove         | Postgres, SQLite            |               |
|          | PRAGMA                                    | Syntactic     | Remove         | SQLite                      |               |
|          | VACUUM                                    | Syntactic     | Remove         | Postgres, SQLite            |               |
|          | DISCARD                                   | Syntactic     | Remove         | Postgres                    |               |
|          | CHECK TABLE                               | Syntactic     | Remove         | MySQL                       |               |
|          |                                           |               |                |                             |               |
| **ST04** | **Other uncommon statements**             | **Syntactic** | **Remove**     | **MySQL, Postgres, SQLite** |               |
|          | NOTIFY                                    | Syntactic     | Remove         | Postgres                    |               |
|          | LISTEN                                    | Syntactic     | Remove         | Postgres                    |               |
|          | UNLISTEN                                  | Syntactic     | Remove         | Postgres                    |               |
|          | SELECT from dbms-specific tables          | Syntactic     | Remove         | MySQL, SQLite               |               |
