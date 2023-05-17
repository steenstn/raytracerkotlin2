import kotlin.math.sqrt
import kotlin.random.Random

data class Vector (val x: Double, val y: Double, val z: Double) {

    constructor() : this(0.0, 0.0, 0.0) {
    }

    companion object {
        fun random() : Vector{
            return Vector(
                Random.nextDouble(-1.0,1.0),
                Random.nextDouble(-1.0,1.0),
                Random.nextDouble(-1.0,1.0))
        }
    }
    fun normalize(): Vector {
        val abs = sqrt(x*x+y*y+z*z)
        return Vector(x/abs, y/abs, z/abs)
    }

    fun dot(vec : Vector) : Double {
        return x*vec.x + y*vec.y + z*vec.z
    }

    fun length() : Double {
        return sqrt(x*x+y*y+z*z)
    }

    fun mixWhite() : Vector {
        return Vector((x+1.0)/2.0, (y+1.0)/2.0,(z+1.0)/2)
    }

    fun cross(vec: Vector) : Vector {
        return Vector(y*vec.z-z*vec.y,-1*(x*vec.z-z*vec.x),x*vec.y-y*vec.x)
    }
    operator fun minus(vec: Vector) : Vector {
        return Vector(x-vec.x, y-vec.y, z-vec.z)
    }

    operator fun div(value : Double) : Vector {
        return Vector(x/value, y/value, z/value)
    }

    operator fun times(value : Double) : Vector {
        return Vector(this.x * value, this.y * value, this.z * value)
    }

    operator fun times(vec : Vector) : Vector {
        return Vector(this.x * vec.x, this.y * vec.y, this.z * vec.z)
    }

    operator fun plus(vec: Vector): Vector {
        return Vector(this.x + vec.x, this.y + vec.y, this.z + vec.z)
    }


}