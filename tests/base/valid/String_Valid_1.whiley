import * from whiley.lang.*

// replace all occurrences of "old" with "new" in string "str".  TO BE
// DEPRECATED
public bool has(char c, string str):
    i = 0
    while i < |str|:
        if str[i] == c:
            return true
        i = i + 1
    return false

void ::main(System sys,[string] args):
    s = "Hello World"
    sys.out.println(str(has('l',s)))
    sys.out.println(str(has('e',s)))
    sys.out.println(str(has('h',s)))
    sys.out.println(str(has('z',s)))
    sys.out.println(str(has('H',s)))
