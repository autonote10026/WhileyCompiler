import * from whiley.lang.*

// this is a comment!
define cr3nat as int

cr3nat f(cr3nat x):
    return 1

void ::main(System sys,[string] args):
    y = f(9)
    sys.out.println(str(y))
