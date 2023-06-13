import os
import sys
import subprocess
import re

if __name__ == '__main__':

    print(sys.platform)
    logs = None
    if sys.platform == "win32":
        logs = subprocess.Popen("powershell -c cat ./target/logs/sqlite3/database0-cur.log", shell=True, stdout=subprocess.PIPE).stdout
    else:
        logs = subprocess.Popen("cat ./target/logs/sqlite3/database0-cur.log", shell=True, stdout=subprocess.PIPE).stdout
   
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
        # print(line)

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
    
    mysql_command = f"""docker exec -i {mysql_id} bash -c "mysql -uroot -proot" """
    # print(s)
    mysql = subprocess.Popen(mysql_command, shell=True, stdout=subprocess.PIPE, stdin=subprocess.PIPE)
    print(mysql.communicate(f"CREATE DATABASE {database_name}; USE {database_name}; {statements}".encode())[0].decode())

    postgres_command = f"""docker exec -i {postgres_id} bash -c "psql -U sqlancer test -c 'CREATE DATABASE {database_name}'; psql -U sqlancer {database_name}" """
    postgres = subprocess.Popen(postgres_command, shell=True, stdout=subprocess.PIPE, stdin=subprocess.PIPE)
    print(postgres.communicate(statements.encode())[0].decode())