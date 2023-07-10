## run from root of sqlancer: python .\src\count_loc.py <commit_before> <commit_after> <name_of_action>
import subprocess
import sys
import threading

def cal_loc(first_commit, second_commit, db_name, action):
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
            print(f"\nThere are {code_modified} codes modified, {code_added} codes added, {code_removed} codes removed from {db_name}")
            if db_name == "SQLite3":
                db_name = db_name.replace("3", "")
            print(f"Overleaf commands for {db_name}:")
            print(f"\\newcommand{{\\loc{db_name}Removed{action}}}[0]{{{code_removed}}}")
            print(f"\\newcommand{{\\loc{db_name}Modified{action}}}[0]{{{code_modified}}}")
            print(f"\\newcommand{{\\loc{db_name}Added{action}}}[0]{{{code_added}}}")
            break


if __name__ == '__main__':
    args = sys.argv
    try:
        first_commit = args[1]
        second_commit = args[2]
        action = args[3]
    except:
        print("Specify 2 commits and a name of action as arguments.")
        sys.exit(1)
    
    threading.Thread(target=cal_loc, args=(first_commit, second_commit, "SQLite3", action)).start()
    threading.Thread(target=cal_loc, args=(first_commit, second_commit, "Postgres", action)).start()
    threading.Thread(target=cal_loc, args=(first_commit, second_commit, "MySQL", action)).start()

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
            print(f"\nThere are {code_modified} codes modified, {code_added} codes added, {code_removed} codes removed from common classes")
            print("Overleaf commands for common:")
            print(f"\\newcommand{{\\locCommonRemoved{action}}}[0]{{{code_removed}}}")
            print(f"\\newcommand{{\\locCommonModified{action}}}[0]{{{code_modified}}}")
            print(f"\\newcommand{{\\locCommonAdded{action}}}[0]{{{code_added}}}")
            break
