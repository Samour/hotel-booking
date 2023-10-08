plugins {
    kotlin("jvm")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation(project(":ports-secondary"))

    implementation("io.insert-koin:koin-core:${properties["koinVersion"]}")
    implementation("org.postgresql:postgresql:${properties["postgresqlVersion"]}")
    implementation("com.zaxxer:HikariCP:${properties["hikariCPVersion"]}")

    testImplementation(project(":tool-postgres-migrations"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:${properties["junitVersion"]}")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${properties["junitVersion"]}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${properties["junitVersion"]}")
    testImplementation("org.assertj:assertj-core:${properties["assertjVersion"]}")
    testImplementation("io.mockk:mockk:${properties["mockkVersion"]}")
}
