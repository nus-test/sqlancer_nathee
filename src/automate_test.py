import subprocess
import sys
import argparse
import threading
import shutil

def generate(database, queries):
  return subprocess.run(f'java -jar ./sqlancer-2.0.0.jar --max-generated-databases 1 --num-threads 8 --num-tries 8 --num-queries {queries} {database} --oracle FUZZER', cwd='./target').returncode


def run(queries, run_number):
  threads = []
  try:
    sqlite_thread = threading.Thread(target=generate, args=['sqlite3', queries])
    sqlite_thread.start()
    threads.append(sqlite_thread)
    mysql_thread = threading.Thread(target=generate, args=['mysql', queries])
    mysql_thread.start()
    threads.append(mysql_thread)
    postgres_thread = threading.Thread(target=generate, args=['postgres', queries])
    postgres_thread.start()
    threads.append(postgres_thread)
    for thread in threads:
      thread.join()
  except:
    print('error generating')
    return -1
  
  diff_process = subprocess.run(f'python ./src/differential_testing.py 8 file {run_number}')
  return diff_process.returncode


if __name__ == '__main__':
  parser = argparse.ArgumentParser()
  parser.add_argument('--num-queries', required=False, default=50, help='number of SELECT statements')
  parser.add_argument('--max-run', required=False, default=-1, help='maximum number of runs', dest='max')
  args = parser.parse_args()
  run_number = 0
  try:
    shutil.rmtree('./target/logs/errors')
  except:
    pass
  while run_number != int(args.max):
    code = run(args.num_queries, run_number)
    if code != 0:
      sys.exit(1)
    run_number += 1
