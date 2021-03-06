plugins {
    kotlin("jvm")
    kotlin("kapt")
}

dependencies {
    implementation(project(":annotation"))

    implementation("com.squareup:kotlinpoet:1.7.2")
    implementation("com.squareup:kotlinpoet-metadata-specs:1.7.2")
    implementation("com.google.auto.service:auto-service:1.0-rc7")
    kapt("com.google.auto.service:auto-service:1.0-rc7")
}
