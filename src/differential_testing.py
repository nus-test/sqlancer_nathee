## run from root of sqlancer: python .\src\differential_testing.py <number of threads to use>
import os
import sys
import subprocess
import re
import glob
import mysql.connector #This needs to be downloaded: pip install mysql-connector-python
import psycopg #This needs to be downloaded: pip install "psycopg[binary]"
import sqlite3
import threading
from collections import Counter

def run_from_file(files, conn):
    for file in files:
        errors = []
        logs = open(file, 'r')
        names = re.split(r'[\\/]', logs.name)
        # extract information and statements from log file from python lib
        time = logs.readline()
        database_name = names[len(names) - 2][0] + logs.readline().split(':')[1].strip() # prefix with databases' first letter
        database_version = logs.readline()
        database_seed = logs.readline()
        statements = list()
        select_statements = list()
        while True:
            line = logs.readline().strip().split(';')[0]
            if not line:
                break
            if line.upper().startswith("SELECT"):
                select_statements.append(line + ';')
            else:
                statements.append(line + ';')


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
        mysql_con = mysql.connector.connect(user="sqlancer", password="sqlancer")
        mysql_cur = mysql_con.cursor()
        mysql_cur.execute(f"DROP DATABASE IF EXISTS {database_name};")
        # mysql_cur.execute("""SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE 'database%'; """)
        # for res in mysql_cur.fetchall():
        #     mysql_cur.execute(f"DROP DATABASE {res[0]};")

        ## drop existing database from postgres with python lib
        postgres_con = conn
        postgres_cur = postgres_con.cursor()
        postgres_cur.execute(f"DROP DATABASE IF EXISTS {database_name};")
        # postgres_cur.execute("SELECT datname FROM pg_database WHERE datname LIKE 'database%';")
        # for res in postgres_cur.fetchall():
        #     postgres_cur.execute(f"DROP DATABASE {res[0]};")

        ## drop existing database from sqlite with python lib
        try:
            os.remove(f"./target/databases/{database_name}.db")
        except:
            pass
        # sqlite_databases = glob.glob("./target/databases/database*")
        # for database in sqlite_databases:
        #     os.remove(database)


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

        
        ## create database on mysql with python lib
        mysql_cur.execute(f"CREATE DATABASE {database_name};")
        sql_mode = ["NO_BACKSLASH_ESCAPES", "PIPES_AS_CONCAT"]
        mysql_con = mysql.connector.connect(user="sqlancer", password="sqlancer", database=database_name, client_flags=[mysql.connector.constants.ClientFlag.FOUND_ROWS], sql_mode=sql_mode)
        mysql_con.autocommit = True #not actually needed
        mysql_cur = mysql_con.cursor()

        ## create database on postgres with python lib
        postgres_cur.execute(f"CREATE DATABASE {database_name};")
        postgres_con = psycopg.connect(f"dbname={database_name} user=sqlancer password=sqlancer host=127.0.0.1")
        postgres_con.autocommit = True
        postgres_cur = postgres_con.cursor()

        ## create database on sqlite with python lib
        sqlite_con = sqlite3.connect(f"./target/databases/{database_name}.db")
        sqlite_cur = sqlite_con.cursor()

        is_successful = True
        for statement in statements:
            # if one statement fails, no point in executing any further
            if not is_successful:
                break
            ## execute statements on mysql with python lib
            try:
                mysql_cur.execute(statement)
                mysql_count = mysql_cur.rowcount
            except mysql.connector.Error as e: #mysql stop executing after finding an error
                errors.append({
                    "where": f"\nMySQL error at {database_name}:",
                    "statement": statement,
                    "error": e,
                    "syntax": "sql syntax" in str(e).lower(),
                    "diff": False
                })
                is_successful = False
            ## execute statements on postgres with python lib
            try:
                postgres_cur.execute(statement)
                postgres_count = postgres_cur.rowcount
            except psycopg.Error as e: #postgres will not execute from the start if an error is detected
                errors.append({
                    "where": f"\nPostgreSQL error at {database_name}:",
                    "statement": statement,
                    "error": e,
                    "syntax": "syntax error" in str(e).lower(),
                    "diff": False
                })
                is_successful = False
            ## execute statements on sqlite with python lib
            try:
                sqlite_cur.execute(statement)
                sqlite_count = sqlite_cur.rowcount
            except sqlite3.Error as e: #sqlite stop executing after finding an error
                errors.append({
                    "where": f"\nSQLite error at {database_name}: ",
                    "statement": statement,
                    "error": e,
                    "syntax": "syntax error" in str(e).lower(),
                    "diff": False
                })
                is_successful = False
            if not statement.upper().startswith('CREATE'):
                if is_successful and ((mysql_count != postgres_count) or (sqlite_count != postgres_count) or (mysql_count != sqlite_count)):
                    errors.append({
                        "where": f"\nDifference error at {database_name}:",
                        "statement": statement,
                        "error": f'Errors: Different number of affected rows\nSQLite: {sqlite_count} | MySQL: {mysql_count} | PostgreSQL: {postgres_count}',
                        "syntax": False,
                        "diff": True
                    })
                    is_successful = False
                
        for select in select_statements:
            # if one statement fails, no point in executing any further
            if not is_successful:
                break
            is_select_successful = True
            ## execute select statements on mysql with python lib
            try:
                mysql_cur.execute(select)
                mysql_results = mysql_cur.fetchall()
                mysql_count = len(mysql_results)
                mysql_multiset = Counter()
                mysql_list = list()
                for res in mysql_results:
                    # print('MySQL: ', res)
                    try:
                        for i in range(len(res)):
                            if type(res[i]) is float:
                                temp = list(res)
                                temp[i] = round(res[i], 12)
                                res = tuple(temp)
                        mysql_multiset[res] += 1
                    except:
                        mysql_multiset[str(res)] += 1
            except mysql.connector.Error as e: #mysql stop executing after finding an error
                errors.append({
                    "where": f"\nMySQL error at {database_name}:",
                    "statement": select,
                    "error": e,
                    "syntax": "sql syntax" in str(e).lower(),
                    "diff": False
                })
                is_select_successful = False
            ## execute select statements on postgres with python lib
            try:
                postgres_cur.execute(select)
                postgres_results = postgres_cur.fetchall()
                postgres_count = len(postgres_results)
                postgres_multiset = Counter()
                postgres_list = list()
                for res in postgres_results:
                    # print('PostgreSQL: ', res)
                    try:
                        for i in range(len(res)):
                            if type(res[i]) is float:
                                temp = list(res)
                                temp[i] = round(res[i], 12)
                                res = tuple(temp)
                        postgres_multiset[res] += 1
                    except:
                        postgres_multiset[str(res)] += 1
            except psycopg.Error as e: #postgres will not execute from the start if an error is detected
                errors.append({
                    "where": f"\nPostgreSQL error at {database_name}:",
                    "statement": select,
                    "error": e,
                    "syntax": "syntax error" in str(e).lower(),
                    "diff": False
                })
                is_select_successful = False
            ## execute select statements on sqlite with python lib
            try:
                sqlite_cur.execute(select)
                sqlite_results = sqlite_cur.fetchall()
                sqlite_count = len(sqlite_results)
                sqlite_multiset = Counter()
                sqlite_list = list()
                for res in sqlite_results:
                    # print('SQLite: ', res)
                    try:
                        for i in range(len(res)):
                            if type(res[i]) is float:
                                temp = list(res)
                                temp[i] = round(res[i], 12)
                                res = tuple(temp)
                        sqlite_multiset[res] += 1
                    except:    
                        sqlite_multiset[str(res)] += 1
            except sqlite3.Error as e: #sqlite stop executing after finding an error
                errors.append({
                    "where": f"\nSQLite error at {database_name}: ",
                    "statement": select,
                    "error": e,
                    "syntax": "syntax error" in str(e).lower(),
                    "diff": False
                })
                is_select_successful = False
            if is_select_successful and ((mysql_count != postgres_count) or (sqlite_count != postgres_count) or (mysql_count != sqlite_count)):
                errors.append({
                    "where": f"\nDifference error at {database_name}:",
                    "statement": select,
                    "error": f'Errors: Different number of returned rows\nSQLite: {sqlite_count} | MySQL: {mysql_count} | PostgreSQL: {postgres_count}',
                    "syntax": False,
                    "diff": True
                })
                continue
            if is_select_successful and (mysql_multiset != postgres_multiset or postgres_multiset != sqlite_multiset or mysql_multiset != sqlite_multiset):
                errors.append({
                    "where": f"\nDifference error at {database_name}:",
                    "statement": select,
                    "error": f'Errors: Different elements in returned rows\nSQLite: {sqlite_multiset} \nMySQL: {mysql_multiset} \nPostgreSQL: {postgres_multiset}',
                    "syntax": False,
                    "diff": True
                })

        # print errors here all at once so errors from same threads are grouped together
        for error in errors:
            print(error["where"] + '\n' + error["statement"] + '\n', error["error"])
            if error["syntax"]:
                print("^-----------------------------------Syntax error-----------------------------------^")
            if error["diff"]:
                print("^<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<Diff error>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>^")


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


    # ## get docker containers id with command line
    # ps = subprocess.Popen("docker ps", shell=True, stdout=subprocess.PIPE).stdout 
    # mysql_id = ""
    # postgres_id = ""
    # while True:
    #     line = ps.readline().decode()
    #     if not line:
    #         break
    #     tokens = line.split()
    #     if tokens[1] == "mysql":
    #         mysql_id = tokens[0]
    #     if tokens[1] == "postgres":
    #         postgres_id = tokens[0]
    # print(mysql_id)
    # print(postgres_id)

    ## get log files with python lib
    try:
        num_threads = int(sys.argv[1])
    except:
        print("Specify number of threads")
        sys.exit(1)
    log_files = glob.glob("./target/logs/*/database*")
    files_per_thread = int(len(log_files) / num_threads)
    remains = len(log_files) % num_threads
    threads = []
    for _ in range(num_threads):
        files = []
        for _ in range(files_per_thread):
            files.append(log_files.pop(0))
        if remains > 0:
            remains = remains - 1
            files.append(log_files.pop(0))
        postgres_con = psycopg.connect("dbname=postgres user=sqlancer password=sqlancer host=127.0.0.1")
        postgres_con.autocommit = True
        t = threading.Thread(target=run_from_file, args=[files, postgres_con])
        threads.append(t)
        t.start()
    
    for t in threads:
        t.join()

    print("Finish running")