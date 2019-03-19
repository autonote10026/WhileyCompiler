function project<T>(T[] arr) -> (T r)
requires |arr| > 0
ensures arr[0] == r:
    return arr[0]

public export method test():
    int[] ai = [1,2,3]
    bool[] ab = [false,true]
    {int f}[] ar = [{f:0},{f:1}]
    // 
    assert project(ai) == 1
    assert project(ab) == false
    assert project(ar) == {f:0}
