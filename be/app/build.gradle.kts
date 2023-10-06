plugins {
    kotlin("jvm")
    application
}

application {
    mainClass = "me.aburke.hotelbooking.ApplicationKt"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation(project(":domain"))
    implementation(project(":repository-postgres"))
    implementation(project(":repository-redis"))
    implementation(project(":facade-rest"))

    implementation("io.insert-koin:koin-core:${properties["koinVersion"]}")
    implementation("io.javalin:javalin:${properties["javalinVersion"]}")

    testImplementation(project(":ports"))
    testImplementation(project(":tool-postgres-migrations"))
    testImplementation(project(":test-rest-client"))
    testImplementation("redis.clients:jedis:${properties["jedisVersion"]}")

    testImplementation("com.fasterxml.jackson.core:jackson-databind:${properties["jacksonVersion"]}")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:${properties["jacksonVersion"]}")
    testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${properties["jacksonVersion"]}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${properties["junitVersion"]}")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${properties["junitVersion"]}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${properties["junitVersion"]}")
    testImplementation("org.assertj:assertj-core:${properties["assertjVersion"]}")
    testImplementation("io.mockk:mockk:${properties["mockkVersion"]}")
    testImplementation("org.skyscreamer:jsonassert:${properties["jsonAssertVersion"]}")
    testImplementation("io.javalin:javalin-testtools:${properties["javalinVersion"]}")
}
