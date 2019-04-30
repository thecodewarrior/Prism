import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// begin common
plugins {
    java
    kotlin("jvm") version "1.3.11"
}

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("reflect"))
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.3.0-M1")
    testCompile("com.nhaarman", "mockito-kotlin-kt1.1", "1.5.0")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.javaParameters = true
}
// end common

dependencies {
    compile(project(":core"))
}
