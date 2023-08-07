plugins {
    kotlin("jvm")
    id("org.openapi.generator") version "6.6.0"
}

val generatedSourcesPath = "$buildDir/generated/openapi"
val apiDescriptionFile = "$projectDir/openapi/hotel-booking.yaml"
val apiRootName = "me.aburke.hotelbooking.rest.client"

sourceSets {
    create("contractTest") {
        compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        java.srcDir("$generatedSourcesPath/src/main/java")
    }
}

configurations["contractTestImplementation"].extendsFrom(configurations.testImplementation.get())
configurations["contractTestRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())

val contractTestImplementation by configurations.getting

openApiGenerate {
    generatorName = "java"
    inputSpec = apiDescriptionFile
    outputDir = generatedSourcesPath
    apiPackage = "$apiRootName.api"
    invokerPackage = "$apiRootName.invoker"
    modelPackage = "$apiRootName.model"
}

tasks["compileContractTestKotlin"].dependsOn("openApiGenerate")

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

    // Copied from open API generated sources
    contractTestImplementation("io.swagger:swagger-annotations:1.6.8")
    contractTestImplementation("com.google.code.findbugs:jsr305:3.0.2")
    contractTestImplementation("com.squareup.okhttp3:okhttp:4.10.0")
    contractTestImplementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    contractTestImplementation("com.google.code.gson:gson:2.9.1")
    contractTestImplementation("io.gsonfire:gson-fire:1.8.5")
    contractTestImplementation("javax.ws.rs:jsr311-api:1.1.1")
    contractTestImplementation("javax.ws.rs:javax.ws.rs-api:2.1.1")
    contractTestImplementation("org.openapitools:jackson-databind-nullable:0.2.6")
    contractTestImplementation("org.apache.commons:commons-lang3:3.12.0")
    contractTestImplementation("jakarta.annotation:jakarta.annotation-api:1.3.5")
    // Manually added to resolve some compile errors
    contractTestImplementation("javax.annotation:javax.annotation-api:1.3.2")
}
