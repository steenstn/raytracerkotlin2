import kotlin.math.PI
import kotlin.math.sqrt

class Sphere(x: Double, y: Double, z: Double, val radius : Double, material : Material) : Mesh(x, y, z, material) {

    override fun getIntersection(start: Vector, direction: Vector): SurfacePoint? {
        val center  = this.position
        val v = start - center

        val wee=(v.dot(direction))*(v.dot(direction))-(v.x*v.x+v.y*v.y+v.z*v.z-this.radius*this.radius)
        if(wee > 0) {
            val intersectionDistance = arrayOf(v.dot(direction)*-1+sqrt(wee),
                v.dot(direction)*-1-sqrt(wee))

            val intersectionsInDirection = intersectionDistance.filter { it > 0.00001 }

            val closestIntersection = intersectionsInDirection.minOrNull() ?: return null

            val endDistance = direction * closestIntersection
            val endPosition = start + endDistance

            return SurfacePoint(endPosition, getNormal(endPosition), this.material)
        }
        return null
    }

    fun getNormal(pos: Vector) : Vector {
        return pos.minus(position).normalize()
    }

    override fun getRandomPoint() : SurfacePoint {
        val randomPoint = Vector.random().normalize() * radius + position
        return SurfacePoint(randomPoint, getNormal(randomPoint), material)
    }

    override fun getArea() : Double {
        return 4*PI*radius*radius
    }

}