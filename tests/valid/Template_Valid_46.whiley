public type Event is { int timeStamp }
public type MouseEvent is { bool altKey }

public type iohandler<E,T> is function(E,T)->(T)

public type MouseEventAttribute<T> is {
    iohandler<MouseEvent,T> handler
}

public type Attribute<T> is null | MouseEventAttribute<T>

function f<T>(Attribute<T> attr) -> int:
    if attr is MouseEventAttribute<T>:
        return 0
    else:
        return 1

function h(MouseEvent e, int t) -> int:
    if e.altKey:
        return t + 1
    else:
        return t - 1

public export method test():
    assume f<int>(null) == 1
    assume f<int>(&h) == 0