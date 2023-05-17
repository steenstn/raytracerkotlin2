import kotlin.random.Random

class SceneCreator {
    companion object {
        fun standardScene(): Scene {
            /*
            val DoF = 0.1
            val focusLength = 7.0
             */
            val cameraPostion = Vector(0.0, -2.0, 8.0)
            val cameraDirection = Vector(0.0, 0.5, -1.0)
            val meshes = listOf(
                Sphere(-4.5, -5.0, 3.0, 2.0, Material.light(5.0)),
                Sphere(
                    -2.0,
                    -1.0,
                    -3.0,
                    2.0,
                    Material(Vector(1.0, 0.6, 0.1).mixWhite(), Vector(), Material.Type.SPECULAR)
                ),
                Sphere(
                    1.0,
                    0.0,
                    0.8,
                    1.0,
                    Material(
                        Vector(0.2, 0.5, 1.0).mixWhite(),
                        Vector(),
                        setOf(Material.Type.GLASS, Material.Type.SPECULAR)
                    )
                ),
                Sphere(
                    3.0,
                    -2.0,
                    -3.0,
                    3.0,
                    Material(Vector(0.8, 0.2, 0.2).mixWhite(), Vector(), Material.Type.DIFFUSE)
                ),
                Plane(
                    0.0,
                    1.0,
                    0.0,
                    Vector(0.0, -1.0, 0.0),
                    Material(Vector(0.2, 0.3, 0.2).mixWhite(), Vector(), Material.Type.DIFFUSE)
                ),
                Plane(0.0, -1000.0, 0.0, Vector(0.0, 1.0, 0.0), Material.light(0.9, 0.9, 1.0))
            )
            return Scene(meshes, cameraPostion, cameraDirection, Vector())
        }

        fun molecule(): Scene {

            val cameraPostion = Vector(0.0, -0.5, 30.0)
            val cameraDirection = Vector(0.0, 0.0, -1.0)
            val spheres = mutableListOf<Mesh>(Sphere(0.0, 0.0, 0.0, 1.0, Material.diffuse(0.2, 0.3, 1.0)))
            var position = Vector()
            for (i in 0..1000) {
                val newDir = Vector.random().normalize() * 1.4
                position += newDir
                val color = if (Random.nextDouble() > 0.7) Vector(0.15, 0.1, 0.23).mixWhite() else Vector(
                    0.05,
                    0.5,
                    0.64
                ).mixWhite()
                spheres.add(
                    Sphere(
                        position.x,
                        position.y,
                        position.z,
                        1.0,
                        Material.diffuse(color.x, color.y, color.z)
                    )
                )
            }
            return Scene(spheres, cameraPostion, cameraDirection, Vector(1.0, 1.0, 1.0))
        }

        fun wall(): Scene {
            val cameraPostion = Vector(0.0, 0.5, 20.0)
            val cameraDirection = Vector(0.0, -1.0, -1.0)
            val min = -10
            val max = 10
            val spheres = (min..max).map { x ->
                (min..max).map { y ->
                    Sphere(x.toDouble(), y.toDouble(), 0.0, 1.0, Material.diffuseRandom())
                }
            }.flatten()
            return Scene(spheres, cameraPostion, cameraDirection, Vector(1.0, 1.0, 1.0))
        }
    }
}