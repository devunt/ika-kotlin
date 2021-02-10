plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization") version "1.4.21"

    application
    idea
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.1")

    compileOnly(project(":annotation"))
    kapt(project(":annotation"))

    implementation("io.ktor:ktor-network:1.5.1")
    implementation("org.koin:koin-core:2.2.2")
    implementation("org.koin:koin-core-ext:2.2.2")
    implementation("com.charleskorn.kaml:kaml:0.27.0")
}


application {
    mainClass.set("org.ozinger.ika.ApplicationKt")
}

kapt {
    correctErrorTypes = true
    useBuildCache = false
}

idea {
    module {
        sourceDirs.add(file("build/generated/source/kaptKotlin/main"))
        generatedSourceDirs.add(file("build/generated/source/kaptKotlin/main"))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "15"
        freeCompilerArgs += listOf(
            "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xuse-experimental=kotlinx.serialization.ExperimentalSerializationApi",
            "-Xopt-in=org.koin.core.component.KoinApiExtension"
        )
    }
}