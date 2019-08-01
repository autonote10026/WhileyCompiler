

type Point is {int y, int x, ...}

type Point3D is {int z, int y, int x}

type Points is Point | Point3D

method isPoint3D(Points t) -> bool:
    if t is Point3D:
        return true
    else:
        return false

public export method test() :
    Point3D p3d = {z: 3, y: 2, x: 1}
    bool result = isPoint3D(p3d)
    assume result == true
    
    Point p2d = {y: 2, x: 1}
    result = isPoint3D(p2d)
    assume result == false
    //
    p2d = {w: 3, y: 2, x: 1}
    result = isPoint3D(p2d)
    assume result == false
