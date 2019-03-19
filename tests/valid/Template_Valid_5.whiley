type Ref<T> is (&T r)
 
public export method test():
    Ref<int> r1 = new 1
    Ref<int> r2 = new 2    
    //
    assert r1 != r2
    assert *r1 != *r2
    assert *r1 == 1
    assert *r2 == 2
    //
    *r1 = 2
    //
    assert *r1 == *r2
    assert r1 != r2