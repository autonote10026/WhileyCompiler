type fun_t<T> is function(T)->(T)

function inc(int x) -> (int r):
    return x + 1

function to_int(int|bool y) -> int:
    if y is int:
        return y
    else if y:
        return 1
    else:
        return 0

function func<T>(fun_t<T> f1, fun_t<T> f2) -> fun_t<T>:
    if f1 == f2:
        return f1
    else:
        return f2

public export method test():
    //
    fun_t<int> fun1 = func(&inc, &to_int)
    fun_t<int> fun2 = func(&to_int, &inc)
    //
    assume fun1(1) == 1
    assume fun2(1) == 2
    
    