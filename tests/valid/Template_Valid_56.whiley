function f<T>() -> function(T)->(T):
    return &(T x -> x)

function apply<T>(T x, function(T)->(T) fn) -> T:
    return fn(x)

public export method test():
    // Causes cycle in constraint graph
    int x = apply(1,f())
    //
    assume x == 1