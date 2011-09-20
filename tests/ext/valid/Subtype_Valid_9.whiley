import * from whiley.lang.*

define sr9nat as int where $ > 0
define sr9tup as {sr9nat f, int g} where g > f 
define sr9arr as [{sr9nat f, int g}] where some { z in $ | z.f == 1}

void ::main(System sys,[string] args):
    x = [{f:1,g:2},{f:1,g:8}]
    x[0].f = 2 
    sys.out.println(str(x))
