import * from whiley.lang.*

real f({(int,real)} xs, int m):
    for i,r in xs:
        if i == m:
            return r
    return -1

void ::main(System sys,[string] args):
    x = f({(1,2.2),(5,3.3)},5)
    sys.out.println(str(x))
    x = f({(1,2.2),(5,3.3)},2)
    sys.out.println(str(x))