import println from whiley.lang.System

function extract([int] ls) => int
ensures $ >= |ls|:
    i = 0
    while i < |ls|:
        i = i + 1
    return i

method main(System.Console sys) => void:
    rs = extract([-2, -3, 1, 2, -23, 3, 2345, 4, 5])
    sys.out.println(Any.toString(rs))