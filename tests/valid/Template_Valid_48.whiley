public type Node<T> is  {T f} | int

public function div<T>(Node<T>[] children) -> Node<T>:
    return 0

function to_folder_html(Node<int> h) -> Node<int>:
    return div([h])

public export method test():
    //
    assume to_folder_html({f:0}) == 0
    assume to_folder_html(0) == 0