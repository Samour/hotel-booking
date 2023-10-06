plugins {
    kotlin("jvm")
}

sourceSets {
    create("contractTest") {
        compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    }
}

configurations["contractTestImplementation"].extendsFrom(configurations.testImplementation.get())
configurations["contractTestRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())

val contractTestImplementation by configurations.getting

task<Test>("contractTest") {
    testClassesDirs = sourceSets["contractTest"].output.classesDirs
    classpath = sourceSets["contractTest"].runtimeClasspath
    group = "verification"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation(project(":ports"))

    implementation("ch.qos.logback:logback-classic:${properties["logbackVersion"]}")
    implementation("io.insert-koin:koin-core:${properties["koinVersion"]}")
    implementation("io.javalin:javalin:${properties["javalinVersion"]}")
    implementation("com.fasterxml.jackson.core:jackson-databind:${properties["jacksonVersion"]}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${properties["jacksonVersion"]}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${properties["jacksonVersion"]}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${properties["junitVersion"]}")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${properties["junitVersion"]}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${properties["junitVersion"]}")
    testImplementation("org.assertj:assertj-core:${properties["assertjVersion"]}")
    testImplementation("io.mockk:mockk:${properties["mockkVersion"]}")
    testImplementation("org.skyscreamer:jsonassert:${properties["jsonAssertVersion"]}")
    testImplementation("io.javalin:javalin-testtools:${properties["javalinVersion"]}")

    contractTestImplementation(project(":test-rest-client"))
}
