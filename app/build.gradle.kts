
plugins {
    scala
    application
    `java-library`
}

version = "1.0"

tasks.jar {
    manifest {
        attributes(mapOf("Implementation-Title" to project.name,
                "Implementation-Version" to project.version))
    }
    project.setProperty("archivesBaseName", "towerdefense")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.scala-lang:scala-library:2.13.6")
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("org.scalafx:scalafx_3:18.0.1-R27")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnit("4.13.2")

            dependencies {
                implementation("org.scalatest:scalatest_3:3.2.11")
                implementation("org.scalatestplus:junit-4-13_3:3.2.12.0")
                runtimeOnly("org.scala-lang.modules:scala-xml_2.13:1.2.0")
            }
        }
    }
}

application {
    mainClass.set("ScalaTowerDefense.App")
}
