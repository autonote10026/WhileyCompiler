import * from whiley.lang.*

define Rec1 as {int x}
define Rec2 as {real x}

int f(Rec2 rec):
    x,y = rec.x
    return x

void ::main(System sys,[string] args):
    rec = {x: 1}
    sys.out.println(str(rec))
    num = f(rec)
    sys.out.println(str(rec))
    sys.out.println(str(num))
