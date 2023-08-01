plugins {
    kotlin("jvm")
}

sourceSets {
    create("integTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

configurations["integTestImplementation"].extendsFrom(configurations.testImplementation.get())
configurations["integTestRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())

task<Test>("integTest") {
    testClassesDirs = sourceSets["integTest"].output.classesDirs
    classpath = sourceSets["integTest"].runtimeClasspath
    group = "verification"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("io.insert-koin:koin-core:${properties["koinVersion"]}")
    implementation("at.favre.lib:bcrypt:${properties["jbcryptVersion"]}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${properties["junitVersion"]}")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${properties["junitVersion"]}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${properties["junitVersion"]}")
    testImplementation("org.assertj:assertj-core:${properties["assertjVersion"]}")
    testImplementation("io.mockk:mockk:${properties["mockkVersion"]}")
}
