import * from whiley.lang.*

bool f(char c):
    return c <= '9'

void ::main(System sys,[string] args):
    x = f('0')    
    sys.out.println(str(x))
    x = f('1')    
    sys.out.println(str(x))
    x = f('2')    
    sys.out.println(str(x))
    x = f('3')    
    sys.out.println(str(x))
    x = f('4')    
    sys.out.println(str(x))
    x = f('5')    
    sys.out.println(str(x))
    x = f('6')    
    sys.out.println(str(x))
    x = f('7')    
    sys.out.println(str(x))
    x = f('8')    
    sys.out.println(str(x))
    x = f('9')    
    sys.out.println(str(x))
    x = f('a')    
    sys.out.println(str(x))
    x = f('b')    
    sys.out.println(str(x))
    x = f('c')    
    sys.out.println(str(x))
    x = f('d')    
    sys.out.println(str(x))
    x = f('e')    
    sys.out.println(str(x))
