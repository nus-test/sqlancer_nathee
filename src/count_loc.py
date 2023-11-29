## run from root of sqlancer: python .\src\count_loc.py <commit_before> <commit_after>
import subprocess
import sys
import threading

def cal_loc(first_commit, second_commit, db_name):
    command = ""
    if sys.platform == "win32":
        # command = f"powershell -c docker run --rm -v ${{PWD}}:/tmp aldanial/cloc --match-d='/src/sqlancer' --match-f='{db_name}.' --hide-rate --quiet --diff-timeout 0 --diff {first_commit} {second_commit}"
        command = f"powershell -c cloc-1.98.exe  --match-d='/src/sqlancer' --match-f='{db_name}.' --hide-rate --quiet --diff-timeout 0 --diff {first_commit} {second_commit}"
    else:
        command = f"docker run --rm -v ${{PWD}}:/tmp aldanial/cloc --match-d='/src/sqlancer' --match-f='{db_name}.' --hide-rate --quiet --diff-timeout 0 --diff {first_commit} {second_commit}"
    stat = subprocess.run(command, shell=True, stdout=subprocess.PIPE).stdout.decode()
    lines = stat.split('\n')
    for i in range(len(lines)):
        if lines[i].startswith("Java"):
            code_modified = lines[i + 2].split()[4]
            code_added = lines[i + 3].split()[4]
            code_removed = lines[i + 4].split()[4]
            print(f"There are {code_modified} codes modified, {code_added} codes added, {code_removed} codes removed from {db_name}")
            break


if __name__ == '__main__':
    args = sys.argv
    try:
        first_commit = args[1]
        second_commit = args[2]
    except:
        print("Specify 2 commits as arguments.")
    
    threading.Thread(target=cal_loc, args=(first_commit, second_commit, "SQLite3")).start()
    threading.Thread(target=cal_loc, args=(first_commit, second_commit, "Postgres")).start()
    threading.Thread(target=cal_loc, args=(first_commit, second_commit, "MySQL")).start()

    command = ""
    if sys.platform == "win32":
        # command = f"""powershell -c docker run --rm -v ${{PWD}}:/tmp aldanial/cloc --match-d='/src/sqlancer' --not-match-f='(SQLite3^|MySQL^|Postgres).' --hide-rate --quiet --diff-timeout 0 --diff {first_commit} {second_commit}"""
        command = f"""powershell -c cloc-1.98.exe --match-d='/src/sqlancer' --not-match-f='(SQLite3^|MySQL^|Postgres).' --hide-rate --quiet --diff-timeout 0 --diff {first_commit} {second_commit}"""
    else:
        command = f""" docker run --rm -v ${{PWD}}:/tmp aldanial/cloc --match-d='/src/sqlancer' --not-match-f="(SQLite3|MySQL|Postgres)." --hide-rate --quiet --diff {first_commit} {second_commit} """
    stat = subprocess.run(command, shell=True, stdout=subprocess.PIPE).stdout.decode()
    lines = stat.split('\n')
    for i in range(len(lines)):
        if lines[i].startswith("Java"):
            code_modified = lines[i + 2].split()[4]
            code_added = lines[i + 3].split()[4]
            code_removed = lines[i + 4].split()[4]
            print(f"There are {code_modified} codes modified, {code_added} codes added, {code_removed} codes removed from common classes")
            break
