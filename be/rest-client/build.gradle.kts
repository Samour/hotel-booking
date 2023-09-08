plugins {
    kotlin("jvm")
    id("org.openapi.generator") version "6.6.0"
}

val generatedSourcesPath = "$buildDir/generated/openapi"
val apiDescriptionFile = "$rootDir/openapi/hotel-booking.yaml"
val apiRootName = "me.aburke.hotelbooking.rest.client"

openApiGenerate {
    generatorName = "java"
    inputSpec = apiDescriptionFile
    outputDir = generatedSourcesPath
    apiPackage = "$apiRootName.api"
    invokerPackage = "$apiRootName.invoker"
    modelPackage = "$apiRootName.model"
}

tasks["compileKotlin"].dependsOn("openApiGenerate")

sourceSets["main"].java.srcDir("$generatedSourcesPath/src/main/java")

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Copied from open API generated sources
    implementation("io.swagger:swagger-annotations:1.6.8")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation("com.google.code.gson:gson:2.9.1")
    implementation("io.gsonfire:gson-fire:1.8.5")
    implementation("javax.ws.rs:jsr311-api:1.1.1")
    implementation("javax.ws.rs:javax.ws.rs-api:2.1.1")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("jakarta.annotation:jakarta.annotation-api:1.3.5")
    // Manually added to resolve some compile errors
    implementation("javax.annotation:javax.annotation-api:1.3.2")
}
