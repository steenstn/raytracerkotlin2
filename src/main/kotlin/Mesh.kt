abstract class Mesh(x: Double, y: Double, z: Double, val material: Material) {
    val position : Vector = Vector(x, y, z)

    abstract fun getIntersection(start : Vector, direction: Vector) : SurfacePoint?
    abstract fun getRandomPoint() : SurfacePoint
    abstract fun getArea() : Double
}