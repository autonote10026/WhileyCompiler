type expr is int[] | int

method f(expr e) -> void:
    if e is int[]:
        debug ("GOT [INT]")
    else:
        if e is int:
            debug ("GOT INT")
        else:
            debug ("GOT SOMETHING ELSE?")
