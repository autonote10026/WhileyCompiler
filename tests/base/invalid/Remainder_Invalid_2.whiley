import * from whiley.lang.*

int f(int x, real y):
    return x % y    

void ::main(System sys,[string] args):
    sys.out.println(str(f(10,5.23)))
    sys.out.println(str(f(10,4)))
    sys.out.println(str(f(1,4)))
    sys.out.println(str(f(103,2)))
    sys.out.println(str(f(-10,5.23)))
    sys.out.println(str(f(-10,4)))
    sys.out.println(str(f(-1,4)))
    sys.out.println(str(f(-103,2)))
    sys.out.println(str(f(-10,-5.23)))
    sys.out.println(str(f(-10,-4)))
    sys.out.println(str(f(-1,-4)))
    sys.out.println(str(f(-103,-2)))
    sys.out.println(str(f(10,-5.23)))
    sys.out.println(str(f(10,-4)))
    sys.out.println(str(f(1,-4)))
    sys.out.println(str(f(103,-2)))
