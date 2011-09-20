import * from whiley.lang.*

void f([int] x, int i) requires |x| > 0:
    if(i < 0 || i >= |x|):
        i = 0
    y = x[i]
    z = x[i]
    assert y == z

void ::main(System sys,[string] args):
    arr = [1,2,3]
    f(arr, 1)
    sys.out.println(str(arr))
    f(arr, 2)
    sys.out.println(str(arr))
    f(arr, 3)
    sys.out.println(str(arr))
    f(arr, -1)
    sys.out.println(str(arr))
    f(arr, 4)
