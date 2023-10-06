plugins {
    kotlin("jvm")
}

task<JavaExec>("bootstrapDb") {
    mainClass = "me.aburke.hotelbooking.migrations.postgres.BootstrapDbKt"
    classpath = sourceSets.main.get().runtimeClasspath
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.postgresql:postgresql:${properties["postgresqlVersion"]}")
    implementation("org.mybatis:mybatis:${properties["mybatisVersion"]}")
}
