class Plane(x : Double, y : Double, z : Double, val normal : Vector, material: Material) : Mesh(x, y, z, material) {

    override fun getIntersection(start: Vector, direction: Vector): SurfacePoint? {
        val distance = (position-start).dot(normal)/direction.dot(normal)
        if(distance > 0.00001) {
            val endMovement = direction * distance
            val intersectionPoint = start + endMovement
            return SurfacePoint(intersectionPoint, normal, material)
        }
        return null
    }

    override fun getRandomPoint() : SurfacePoint {
        return SurfacePoint(position, normal, material)
    }

    override fun getArea() : Double {
        return 1.0
    }

}