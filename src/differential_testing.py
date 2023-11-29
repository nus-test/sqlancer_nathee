import os
import sys
import subprocess
import re

if __name__ == '__main__':
    print(sys.platform)
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

    logs = subprocess.Popen("powershell -c cat ./target/logs/sqlite3/database0-cur.log", shell=True, stdout=subprocess.PIPE).stdout
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

    print(statements)
    s = f"""docker exec -i {mysql_id} sh -c "mysql -uroot -proot -e 'CREATE DATABASE {database_name}; USE {database_name};' " """
    print(s)
    subprocess.Popen(s, shell=True, stdout=subprocess.PIPE, stdin=subprocess.PIPE)
    # mysql = subprocess.Popen(f"""docker exec -i {mysql_id} sh -c "mysql -uroot -proot" -e {statements} """, shell=True, stdout=subprocess.PIPE, stdin=subprocess.PIPE)
    # print(mysql.stdin.write(b"CREATE TABLE IF NOT EXISTS t0 (c0 TEXT); -- 20ms;"))
    # print(mysql.stdout.read())
    # print(mysql.communicate(b"SELECT * FROM t0;")[0])
    # print(mysql.communicate(b"SHOW DATABASES;")[0])
    # print(mysql.stdout.read().decode())
    # mysql = subprocess.Popen(f"powershell", shell=True, stdout=subprocess.PIPE, stdin=subprocess.PIPE)
    # print(mysql.communicate(b"ls"))

    
    