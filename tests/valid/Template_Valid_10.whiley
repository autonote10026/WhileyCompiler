function id<T>(T x) -> (T y):
    return x

public export method test():
    int x = id(1)
    //
    assume x == 1