

type state is {int[] input, int pos} where (pos >= 0) && (pos <= |input|)

public function isLetter(int c) -> bool:
    return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z')

function f(state st) -> int:
    if st.pos < |st.input|:
        if isLetter(st.input[st.pos]):
            return st.input[st.pos]
    return ' '

public export method test() -> void:
    assume f({input: "hello", pos: 0}) == 104
