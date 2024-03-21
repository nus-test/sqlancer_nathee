import json
import re
import sys

args = sys.argv
index = int(args[1])
# category = args[2]

bugs_file = open('./common/common_cases_plus_plus.json', 'r', encoding="utf8").read()
bugs = json.loads(bugs_file)
length = len(bugs)

pattern_file = open('./sql_patterns.json', 'r', encoding="utf8").read()
patterns = json.loads(pattern_file)

# temp_file = open('./src/temp1.json', 'r', encoding="utf8").read()
# temp_bugs = json.loads(temp_file)

# target_file = open('./src/temp1.json', 'w', encoding="utf8")

## extracting interested bugs

# print(bugs[0]['dbms'])
# # print(bugs[0]['status'])
# bugs1 = list()
# for i in range(len(patterns)):
#   patterns[i]["index"] = i


  # if status and dbms:
  #   bugs1.append(bugs[i])
  # for j in range(len(statements)):
  #   print(statements[j])
# file = open('./src/sql_patterns.json', 'w', encoding="utf8")
# file.write(json.dumps(patterns))
# print(length)

## extracting bugs into each cases/statements

# count = 0
# case_count = 0
# new_bugs = []
# for i in range(length):
#   bug = bugs[i]
#   is_found = False
#   new_bug = dict()
#   new_bug["statements"] = list()
#   for j in range(len(bug["test"])):
#     # count += 1
#     statement = bug["test"][j]
#     # match = re.findall('\/', statement)
#     match = re.findall(patterns[index]["pattern"], statement)
#     if match:
#       if not is_found:
#         case_count += 1
#         is_found = True
      
#       new_bug["dbms"] = bug["dbms"]
#       # new_bug["category"] = category
#       new_bug["statements"].append(statement)
#       count+=len(match)
#   if new_bug["statements"]:
#     new_bugs.append(new_bug)
# print(case_count)
# print(count)
# target_file.write(json.dumps(new_bugs))
# target_file.write('\n')

## extracting bugs from a statemnet into interested bugs with categories

# count = 0
# case_count = 0
# new_bugs = []
# for i in range(length):
#   bug = bugs[i]
#     # count += 1
#   is_found = False
#   for j in range(len(bug["statements"])):
#     statement = bug["statements"][j]
#     # match = re.findall('rank', statement)
#     match = re.findall(patterns[index]["pattern"], statement)
#     if match:
#       if not is_found:
#           case_count += 1
#           is_found = True
#       new_bug = dict()
#       new_bug["dbms"] = bug["dbms"]
#       new_bug["category"] = category
#       new_bug["statement"] = statement
#       new_bugs.append(new_bug)
#       count+=len(match)
# print(case_count)
# print(count)
# temp_bugs.extend(new_bugs)
# target_file.write(json.dumps(temp_bugs))
# target_file.write('\n')

## filtering existing bugs

# count = 0
# new_bugs = []
# for i in range(length):
#   bug = bugs[i]
#   is_good = True
#   for j in range(len(bug["test"])):
#   # for j in range(len(bug["statements"])):
#     # count += 1
#     statement = bug["test"][j]
#     # statement = bug["statements"][j]
#     # match = re.findall('rank', statement)
#     match = re.findall(patterns[index]["pattern"], statement)
#     if match:
#       is_good = False
    
#   if is_good:
#     new_bugs.append(bug)
#     count += len(bug["test"])
#     # count += len(bug["statements"])

# print(len(new_bugs))
# print(count)
# target_file.write(json.dumps(new_bugs))
# target_file.write('\n')

## Coutning bugs

mysql_count = 0
mysql_statements = 0
sqlite_count = 0
sqlite_statements = 0
postgres_count = 0
postgres_statements = 0
for i in range(length):
  bug = bugs[i]
  if bug["dbms"] == "MySQL":
    mysql_count += 1
    mysql_statements += len(bug["test"])
    # mysql_statements += len(bug["statements"])
  if bug["dbms"] == "SQLite":
    sqlite_count += 1
    sqlite_statements += len(bug["test"])
    # sqlite_statements += len(bug["statements"])
  if bug["dbms"] == "PostgreSQL":
    postgres_count += 1
    postgres_statements += len(bug["test"])
    # postgres_statements += len(bug["statements"])

print('mysql',mysql_count,mysql_statements)
print('sqlite',sqlite_count,sqlite_statements)
print('postgres',postgres_count,postgres_statements)
print(length)

# remove overlap from interested bugs
# count = len(bugs)
# new_bugs = []
# for i in range(count):
#     if bugs[i] not in bugs[i + 1:]:
#         new_bugs.append(bugs[i])
#     else:
#         # print(bugs[i]["category"], bugs[i]["dbms"], bugs[i]["test"])
#         print(bugs[i]["category"], bugs[i]["dbms"], bugs[i]["statement"])
# print(count)
# print(len(new_bugs))
# target_file.write(json.dumps(new_bugs))
# target_file.write('\n') 
