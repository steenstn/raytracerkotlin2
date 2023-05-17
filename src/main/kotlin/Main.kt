import kotlinx.coroutines.*
import java.io.File
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

fun clamp(value: Double, min: Double, max: Double): Double {
    return if (value > max) max else if (value < min) min else value
}

val width = 800
val height = 500

/*
Vänsterorienterat
x går höger
y går neråt
z går mot skärmen
*/

val scene = SceneCreator.molecule()
val xmax = 5
val ymax = 5
val maxBounces = 10

//var numBounces = 0
var endImage = arrayListOf<Double>()
var numPasses = 1
val DoF: Double = 0.0
val focusLength = 7.0
val numRays = 10
val pixels = (0 until height).map { y ->
    (0 until width).map { x -> Pixel(x, y) }
}.flatten()

@OptIn(ExperimentalTime::class)
fun main() = runBlocking {


    for (i in 0 until width * height * 3) {
        endImage.add(0.0)
    }

    while (true) {
        val time = measureTime {
            raytrace()
        }
        println(time.inWholeMilliseconds)
    }
}

fun screenToDirection(screenX: Int, screenY: Int, direction: Vector): Vector {
    val x = (screenX * 6.0) / width - 3.0
    val y = (screenY * 6.0) * height / width / height - 3.0 * height / width
    return Vector(
        (x + direction.x) / xmax,
        (y + direction.y) / ymax,
        direction.z
    ).normalize()
}

fun unfocus(start: Vector, direction: Vector): Pair<Vector, Vector> {
    val s2 = start + Vector.random() * DoF
    val position2 = start + direction * focusLength
    return s2 to (position2 - s2).normalize()
}

fun raytrace() = runBlocking(Dispatchers.Default) {

    val pixelColor = pixels.map { pixel ->
        async {
            val direction = screenToDirection(pixel.x, pixel.y, scene.cameraDirection)

            val calls: List<Vector> = (0 until numRays).map {
                val (newStart, newDirection) = unfocus(scene.cameraPosition, direction);
                { shootRay(newStart, newDirection.normalize(), 0) }
            }.map { it.invoke() }

            calls.reduce { acc, res -> acc + res } / numRays.toDouble()
        }

    }.awaitAll()

    var index = 0
    pixelColor.forEach {
        endImage[index] += it.x
        endImage[index + 1] += it.y
        endImage[index + 2] += it.z
        index += 3
    }

    val imageToRender = endImage.map { clamp(it / numPasses, 0.0, 1.0) }
    println("numpasses: $numPasses")
    numPasses++
    saveFile(imageToRender)

}

fun saveFile(image: List<Double>) {
    println("saving to file")
    val outFile = File("test.ppm")
    val writer = outFile.writer()
    writer.write("P3\n")
    writer.write("$width $height\n")
    writer.write("${(image.max() * 255).toInt()}\n")
    image.forEach { writer.write("${(it * 255).toInt()} ") }
    writer.close()
}


fun shootRay(start: Vector, direction: Vector, numBounces: Int): Vector {
    if (numBounces > maxBounces) {
        return Vector()
    }
    val intersections = scene.meshes.mapNotNull { it.getIntersection(start, direction) }
    val closestIntersection = intersections.minByOrNull { (it.position - start).length() } ?: return scene.ambientColor

    if (closestIntersection.material.types.contains(Material.Type.LIGHT)) {
        return closestIntersection.material.emittance
    } else if (closestIntersection.material.types.contains(Material.Type.GLASS) && Random.nextDouble() > 0.1) {
        return shootRefractedRay(closestIntersection.position, direction, closestIntersection, 1.0, numBounces)
    } else {
        val explicitRay = explicitRay(closestIntersection)
        val randomVector = Vector.random()
        val crossed = randomVector.cross(closestIntersection.normal).normalize()
        val eps1 = Random.nextDouble() * 3.14159 * 2.0
        val eps2 = sqrt(Random.nextDouble())

        val x = cos(eps1) * eps2
        val y = sin(eps1) * eps2
        val z = sqrt(1.0 - eps2 * eps2)

        val tangent = closestIntersection.normal.cross(crossed)

        val newDirection =
            if (closestIntersection.material.types.contains(Material.Type.SPECULAR) && Random.nextDouble() > 0.4) {
                direction - closestIntersection.normal * 2.0 * (closestIntersection.normal.dot(direction))
            } else {
                crossed * x + tangent * y + closestIntersection.normal * z
            }

        val reflected = shootRay(closestIntersection.position, newDirection.normalize(), numBounces + 1)
        return closestIntersection.material.color * (reflected) //explicitRay
    }

}

fun explicitRay(surfacePoint: SurfacePoint): Vector {
    val lights = scene.meshes.filter { it.material.isLight() }
    var lightValue = Vector()
    lights.forEach {
        val randomPointOnLight = it.getRandomPoint()
        val direction = (randomPointOnLight.position - surfacePoint.position).normalize()

        val intersections = scene.meshes.mapNotNull { s -> s.getIntersection(surfacePoint.position, direction) }
        val closestIntersection = intersections.minBy { s -> (s.position - surfacePoint.position).length() }

        if (closestIntersection != null && closestIntersection.material.isLight()) {

            lightValue += (surfacePoint.material.color * surfacePoint.normal.dot(direction))
        }
    }
    return lightValue
}

fun shootRefractedRay(
    start: Vector,
    direction: Vector,
    surfacePoint: SurfacePoint,
    refractionIndex: Double,
    numBounces: Int
): Vector {
    val refractionIndex2 = 1.5

    val n = refractionIndex / refractionIndex2
    val cosI = surfacePoint.normal.dot(direction)
    val sinT2 = n * n * (1.0 - cosI * cosI)

    val refracted = direction * n + surfacePoint.normal * (n * cosI - sqrt(1.0 - sinT2))

    val newDirection = refracted.normalize()
    val newStart = start + direction * 0.0001

    val intersections = scene.meshes.mapNotNull { s -> s.getIntersection(newStart, newDirection) }
    val closestIntersection = intersections.minBy { s -> (s.position - newStart).length() } ?: return Vector()
    val normalOut = closestIntersection.normal * -1.0

    val cosOut = normalOut.dot(direction)
    val sinT2Out = n * n * (1.0 - cosOut * cosOut)
    val refractedOut = direction * n + normalOut * (n * cosOut - sqrt(1.0 - sinT2Out))
    val directionOut = refractedOut.normalize()
    return if (sinT2Out > 1) {
        Vector()
    } else {
        shootRay(closestIntersection.position + directionOut * 0.0001, directionOut, numBounces)
    }


}