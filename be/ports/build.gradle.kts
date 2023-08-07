plugins {
    kotlin("jvm")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("io.insert-koin:koin-core:${properties["koinVersion"]}")
}
