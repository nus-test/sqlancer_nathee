import os
import sys
import subprocess
import re

if __name__ == '__main__':

    ## get log file
    print(sys.platform)
    logs = None
    if sys.platform == "win32":
        logs = subprocess.Popen("powershell -c cat ./target/logs/sqlite3/database0-cur.log", shell=True, stdout=subprocess.PIPE).stdout
    else:
        logs = subprocess.Popen("cat ./target/logs/sqlite3/database0-cur.log", shell=True, stdout=subprocess.PIPE).stdout
   
    ## extract information and statements from log file
    time = logs.readline().decode()
    database_name = logs.readline().decode().split(':')[1].strip()
    database_version = logs.readline().decode()
    database_seed = logs.readline().decode()
    statements = ""
    while True:
        line = logs.readline().decode().strip().split(';')[0]
        if not line:
            break
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
    
    ## drop existing database from mysql
    drop_command = ""
    mysql_console = subprocess.Popen(f"""docker exec -i {mysql_id} bash -c "mysql -uroot -proot -sN" """, shell=True, stdout=subprocess.PIPE, stdin=subprocess.PIPE)
    list_command = f"""SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE "database%"; """
    mysql_tables = mysql_console.communicate(list_command.encode())[0].decode().split()
    for table in mysql_tables:
        drop_command += f"DROP DATABASE {table};"
    subprocess.Popen(f"""docker exec {mysql_id} bash -c "mysql -uroot -proot -e '{drop_command}' """, shell=True, stdout=subprocess.PIPE, stdin=subprocess.PIPE)

    ## drop existing database from postgres
    drop_command = ""
    psql_console = subprocess.Popen(f"""docker exec -i {postgres_id} bash -c "psql -t -q -U sqlancer postgres" """, shell=True, stdout=subprocess.PIPE, stdin=subprocess.PIPE)
    list_command = """ SELECT datname FROM pg_database WHERE datname LIKE 'database%'; """
    postgres_tables = psql_console.communicate(list_command.encode())[0].decode().split()
    for table in postgres_tables:
        drop_command += f"DROP DATABASE {table};"
    psql_console = subprocess.Popen(f"""docker exec -i {postgres_id} bash -c "psql -t -q -U sqlancer postgres " """, shell=True, stdout=subprocess.PIPE, stdin=subprocess.PIPE)
    psql_console.communicate(drop_command.encode())
    
    ## drop existing database from sqlite
    if sys.platform == "win32":
        try:
            drop_command = f"""del ".\\target\\databases\\database*" """
        except:
            pass
    else:
        drop_command = f"""rm ./target/databases/database*  """
    subprocess.Popen(drop_command, shell=True, stdout=subprocess.PIPE, stdin=subprocess.PIPE)
        
    ## create database and execute statements on mysql
    mysql_command = f"""docker exec -i {mysql_id} bash -c "mysql -uroot -proot -sN" """
    mysql = subprocess.Popen(mysql_command, shell=True, stdout=subprocess.PIPE, stdin=subprocess.PIPE)
    mysql_result = mysql.communicate(f"CREATE DATABASE {database_name}; USE {database_name}; {statements}".encode())[0].decode()
    print(mysql_result)

    ## create database and execute statements on postgres
    postgres_command = f"""docker exec -i {postgres_id} bash -c "psql -q -U sqlancer test -c 'CREATE DATABASE {database_name}'; psql -t -q -U sqlancer {database_name}" """
    postgres = subprocess.Popen(postgres_command, shell=True, stdout=subprocess.PIPE, stdin=subprocess.PIPE)
    postgres_result = postgres.communicate(statements.encode())[0].decode()
    print(postgres_result[:-1])

    ## create database and execute statements on sqlite
    sqlite_command = f""" sqlite3.exe ./target/databases/{database_name} "{statements}" """
    sqlite_command = f""" sqlite3.exe ./target/databases/{database_name}.db"""
    sqlite_console = subprocess.Popen(sqlite_command, shell=True, stdout=subprocess.PIPE, stdin=subprocess.PIPE)
    sqlite_result = sqlite_console.communicate(statements.encode())[0].decode()
    print(sqlite_result)

    print(mysql_result == postgres_result[:-1])
    print(mysql_result == sqlite_result)
    print(sqlite_result == postgres_result[:-1])