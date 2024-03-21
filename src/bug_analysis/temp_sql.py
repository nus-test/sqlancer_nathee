import json
import re
import sys
import pathlib
# sql_file = open('./src/SQLite_expected_errors.txt', 'r', encoding="utf8").read()
sql_file = open('./src/SQLite_expected_errors.txt', 'r', encoding="utf8")

# target_file = open('./src/SQL_statements.json', 'w', encoding="utf8")

# sql = json.loads(sql_file)

# for i in range(len(sql)):
#   for j in range(len(sql[i])):
#     sql[i][j]["type"] = ''

# target_file.write(json.dumps(sql))
# target_file.write('\n') 

# text = ""

# for i in range(len(sql[1])):
#   record = sql[1][i]
#   if record["type"] == "DCL":
#     text = text + record["statement"] + ', '
#     print(record["statement"])

# print(text)

# sqlite_errors = list()
# while True:
#   line = sql_file.readline().strip()
#   if not line:
#       break
#   sqlite_errors.append(line)

# for i in range(len(sqlite_errors)):
#    if re.findall(sqlite_errors[i], 'SQL error or missing database (no such table:'):
#       print(sq
# lite_errors[i])
for i in range(1000):
  path = pathlib.Path(f'./target/logs/errors/run{i}')
  if tuple(path.iterdir()) == ():
    path.rmdir()