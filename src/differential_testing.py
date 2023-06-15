import os
import sys
import subprocess
import re
import glob
import mysql.connector #This needs to be downloaded: pip install mysql-connector-python
import psycopg #This needs to be downloaded: pip install "psycopg[binary]"
import sqlite3


if __name__ == '__main__':

    # ## get log file with command line
    # print(sys.platform)
    # logs = None
    # if sys.platform == "win32":
    #     logs = subprocess.Popen("powershell -c cat ./target/logs/sqlite3/database0-cur.log", shell=True, stdout=subprocess.PIPE).stdout
    # else:
    #     logs = subprocess.Popen("cat ./target/logs/sqlite3/database0-cur.log", shell=True, stdout=subprocess.PIPE).stdout
    #
    # ## extract information and statements from log file from command line
    # time = logs.readline().decode()
    # database_name = logs.readline().decode().split(':')[1].strip()
    # database_version = logs.readline().decode()
    # database_seed = logs.readline().decode()
    # statements = ""
    # select_statements = list()
    # while True:
    #     line = logs.readline().decode().strip().split(';')[0]
    #     if not line:
    #         break
    #     if line.upper().startswith("SELECT"):
    #         select_statements.append(line + ';')
    #     else:
    #         statements += line + ';'


    ## get log file with python lib
    log_files = glob.glob("./target/logs/*/database*")
    logs = None
    for file in log_files:
        logs = open(file, 'r')

    # extract information and statements from log file from python lib
    time = logs.readline()
    database_name = logs.readline().split(':')[1].strip()
    database_version = logs.readline()
    database_seed = logs.readline()
    statements = ""
    select_statements = list()
    while True:
        line = logs.readline().strip().split(';')[0]
        if not line:
            break
        if line.upper().startswith("SELECT"):
            select_statements.append(line + ';')
        else:
            statements += line + ';'


    ## get docker containers id
    ps = subprocess.Popen("docker ps", shell=True, stdout=subprocess.PIPE).stdout 
    mysql_id = ""
    postgres_id = ""
    while True:
        line = ps.readline().decode()
        if not line:
            break
        tokens = line.split()
        if tokens[1] == "mysql":
            mysql_id = tokens[0]
        if tokens[1] == "postgres":
            postgres_id = tokens[0]
    print(mysql_id)
    print(postgres_id)
    

    # ## drop existing database from mysql with command line
    # drop_command = ""
    # mysql_console = subprocess.Popen(f"""docker exec -i {mysql_id} bash -c "mysql -uroot -proot -sN" """, shell=True, stdout=subprocess.PIPE, stdin=subprocess.PIPE)
    # list_command = f"""SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE "database%"; """
    # mysql_databases = mysql_console.communicate(list_command.encode())[0].decode().split()
    # for database in mysql_databases:
    #     drop_command += f"DROP DATABASE {database};"
    # subprocess.Popen(f"""docker exec {mysql_id} bash -c "mysql -uroot -proot -e '{drop_command}' """, shell=True, stdout=subprocess.PIPE, stdin=subprocess.PIPE)
    #
    # ## drop existing database from postgres with command line
    # drop_command = ""
    # psql_console = subprocess.Popen(f"""docker exec -i {postgres_id} bash -c "psql -t -q -U sqlancer postgres" """, shell=True, stdout=subprocess.PIPE, stdin=subprocess.PIPE)
    # list_command = """ SELECT datname FROM pg_database WHERE datname LIKE 'database%'; """
    # postgres_databases = psql_console.communicate(list_command.encode())[0].decode().split()
    # for database in postgres_databases:
    #     drop_command += f"DROP DATABASE {database};"
    # psql_console = subprocess.Popen(f"""docker exec -i {postgres_id} bash -c "psql -t -q -U sqlancer postgres " """, shell=True, stdout=subprocess.PIPE, stdin=subprocess.PIPE)
    # psql_console.communicate(drop_command.encode())
    #
    ## drop existing database from sqlite with command line
    # if sys.platform == "win32":
    #     try:
    #         drop_command = f"""del ".\\target\\databases\\database*" """
    #     except:
    #         pass
    # else:
    #     drop_command = f"""rm ./target/databases/database*  """
    # subprocess.Popen(drop_command, shell=True, stdout=subprocess.PIPE, stdin=subprocess.PIPE)
        
        
    ## drop existing database from mysql with python lib
    mysql_con = mysql.connector.connect(user="root", password="root")
    mysql_cur = mysql_con.cursor()
    mysql_cur.execute("""SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE 'database%'; """)
    for res in mysql_cur.fetchall():
        mysql_cur.execute(f"DROP DATABASE {res[0]};")

    ## drop existing database from postgres with python lib
    postgres_con = psycopg.connect("dbname=postgres user=sqlancer password=sqlancer")
    postgres_con.autocommit = True
    postgres_cur = postgres_con.cursor()
    postgres_cur.execute("SELECT datname FROM pg_database WHERE datname LIKE 'database%';")
    for res in postgres_cur.fetchall():
        postgres_cur.execute(f"DROP DATABASE {res[0]};")

    ## drop existing database from sqlite with python lib
    sqlite_databases = glob.glob("./target/databases/database*")
    for database in sqlite_databases:
        os.remove(database)


    # ## create database and execute statements on mysql with command line
    # mysql_command = f"""docker exec -i {mysql_id} bash -c "mysql -uroot -proot -sN" """
    # mysql = subprocess.Popen(mysql_command, shell=True, stdout=subprocess.PIPE, stdin=subprocess.PIPE)
    # mysql_result = mysql.communicate(f"CREATE DATABASE {database_name}; USE {database_name}; {statements}".encode())[0].decode()
    #
    # ## create database and execute statements on postgres with command line
    # postgres_command = f"""docker exec -i {postgres_id} bash -c "psql -q -U sqlancer test -c 'CREATE DATABASE {database_name}'; psql -t -q -U sqlancer {database_name}" """
    # postgres = subprocess.Popen(postgres_command, shell=True, stdout=subprocess.PIPE, stdin=subprocess.PIPE)
    # postgres_result = postgres.communicate(statements.encode())[0].decode()
    #
    # ## create database and execute statements on sqlite with command line
    # sqlite_command = f""" sqlite3.exe ./target/databases/{database_name}.db"""
    # sqlite_console = subprocess.Popen(sqlite_command, shell=True, stdout=subprocess.PIPE, stdin=subprocess.PIPE)
    # sqlite_result = sqlite_console.communicate(statements.encode())[0].decode()


    ## create database and execute statements on mysql with python lib
    mysql_list = list()
    try:
        mysql_cur.execute(f"CREATE DATABASE {database_name};")
        mysql_con = mysql.connector.connect(user="root", password="root", database=database_name)
        mysql_con.autocommit = True #not actually needed
        mysql_cur = mysql_con.cursor()
        mysql_cur.execute(statements, multi=True)
        for cur in mysql_cur.execute(statements, multi=True):
            pass
        for select in select_statements:
            mysql_cur.execute(select)
            for res in mysql_cur.fetchall():
                mysql_list.append(res[0])
                print(res[0])
    except mysql.connector.Error as e: #mysql stop executing after finding an error
        print("MySQL error:")
        print(e)

    ## create database and execute statements on postgres with python lib
    postgres_list = list()
    try:
        postgres_cur.execute(f"CREATE DATABASE {database_name};")
        postgres_con = psycopg.connect(f"dbname={database_name} user=sqlancer password=sqlancer")
        postgres_con.autocommit = True
        postgres_cur = postgres_con.cursor()
        postgres_cur.execute(statements)
        for select in select_statements:
            postgres_cur.execute(select)
            for res in postgres_cur.fetchall():
                postgres_list.append(res[0])
                print(res[0])
    except psycopg.Error as e: #postgres will not execute from the start if an error is found
        print("PostgreSQL error:")
        print(e)

    ## create database and execute statements on sqlite with python lib
    sqlite_list = list()
    try:
        sqlite_con = sqlite3.connect(f"./target/databases/{database_name}.db")
        sqlite_cur = sqlite_con.cursor()
        res = sqlite_cur.executescript(statements)
        for select in select_statements:
            res = sqlite_cur.execute(select)
            for tuple in res.fetchall():
                sqlite_list.append(tuple[0])
                print(tuple[0])
    except sqlite3.Error as e: #sqlite stop executing after finding an error
        print("SQLite error: ")
        print(e)


    # print(len(mysql_list) == len(postgres_list))
    # print(len(sqlite_list) == len(postgres_list))
    # len = len(sqlite_list)
    # for i in range(len):
    #     print(mysql_list[i] == postgres_list[i], sqlite_list[i] == postgres_list[i], mysql_list[i] == sqlite_list[i])

    # mysql always escape \ but the other two do not. 'NO_BACKSLASH_ESCAPES' mode can disable it