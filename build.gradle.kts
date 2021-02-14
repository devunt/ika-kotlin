group = "org.ozinger"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.4.30" apply false
}

allprojects {
    repositories {
        jcenter()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            useIR = true
            jvmTarget = "15"
        }
    }
}

