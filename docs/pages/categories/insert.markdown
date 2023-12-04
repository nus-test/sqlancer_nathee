---
layout: minimal
title: INSERT
parent: Uncommon SQL Changes
nav_order: 7
---

# INSERT

| _ID_    | _Name_                     | _Difference_  | _Action Taken_ | _DBMS_               | _Description_ |
| :------ | :------------------------- | :------------ | :------------- | :------------------- | :------------ |
| **I01** | **Priority modifiers**     | **Syntactic** | **Remove**     | **MySQL**            |               |
|         | LOW_PRIORITY               | Syntactic     | Remove         | MySQL                |               |
|         | DELAYED                    | Syntactic     | Remove         | MySQL                |               |
|         | HIGH_PRIORITY              | Syntactic     | Remove         | MySQL                |               |
|         | IGNORE                     | Syntactic     | Remove         | MySQL                |               |
|         |                            |               |                |                      |               |
| **I02** | **Upsert clause**          | **Syntactic** | **Remove**     | **Postgres, SQLite** |               |
|         | ON CONFLICT                | Syntactic     | Remove         | Postgres, SQLite     |               |
|         | DO NOTHING                 | Syntactic     | Remove         | Postgrse, SQLite     |               |
|         | UPDATE SET                 | Syntactic     | Remove         | SQLite               |               |
|         |                            |               |                |                      |               |
| **I03** | **DEFAULT keyword**        | **Syntactic** | **Remove**     | **Postgres**         |               |
|         |                            |               |                |                      |               |
| **I04** | **Other specific options** | **Syntactic** | **Remove**     | **Postgres, SQLite** |               |
|         | OR IGNORE                  | Syntactic     | Remove         | SQLite               |               |
|         | OR REPLACE                 | Syntactic     | Remove         | SQLite               |               |
|         | OR ABORT                   | Syntactic     | Remove         | SQLite               |               |
|         | OR FAIL                    | Syntactic     | Remove         | SQLite               |               |
|         | OR ROLLBACK                | Syntactic     | Remove         | SQLite               |               |
|         | OVERRIDING USER VALUE      | Syntactic     | Remove         | Postgres             |               |
|         | OVERRIDING SYSTEM VALUE    | Syntactic     | Remove         | Postgres             |               |
