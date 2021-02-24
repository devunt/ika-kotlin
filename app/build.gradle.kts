plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization") version "1.4.30"

    application
    idea
}

dependencies {
    val koinVersion = "2.2.2"

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.1")

    compileOnly(project(":annotation"))
    kapt(project(":annotation-processor"))

    implementation("io.ktor:ktor-network:1.5.1")
    implementation("org.koin:koin-core:$koinVersion")
    implementation("org.koin:koin-core-ext:$koinVersion")
    implementation("com.charleskorn.kaml:kaml:0.27.0")

    testImplementation(platform("org.junit:junit-bom:5.7.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.koin:koin-test:$koinVersion")
    testImplementation("org.koin:koin-test-junit5:$koinVersion")
    testImplementation("io.mockk:mockk:1.10.6")
    testImplementation(platform("io.strikt:strikt-bom:0.29.0"))
    testImplementation("io.strikt:strikt-jvm")
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

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xuse-experimental=kotlinx.serialization.ExperimentalSerializationApi",
            "-Xopt-in=org.koin.core.component.KoinApiExtension"
        )
    }
}