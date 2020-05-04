
import groovy.lang.Closure
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// begin common
plugins {
    `java-library`
    kotlin("jvm") version "1.3.11"
    id("org.jetbrains.dokka")
    `maven-publish`
}

base.archivesBaseName = "Prism"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.3.0-M1")
    testImplementation("com.nhaarman", "mockito-kotlin-kt1.1", "1.5.0")
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
    compile(kotlin("reflect"))
    compile(kotlin("stdlib-jdk8"))
    compile("com.github.thecodewarrior", "Mirror", "cdd1ec3034")
}

tasks.withType<DokkaTask> {
    val out = "$projectDir/docs"
    outputFormat = "html"
    outputDirectory = out
    jdkVersion = 8
    doFirst {
        println("Cleaning doc directory $out...")
        project.delete(fileTree(out))
    }

    kotlinTasks(Any().dokkaDelegateClosureOf<Any?> { emptyList<Any?>() })

    sourceDirs = listOf("src/main/kotlin").map { projectDir.resolve(it) }
    samples = listOf("src/samples/java", "src/samples/kotlin")
    includes = projectDir.resolve("src/main/docs").walkTopDown()
            .filter { it.isFile }
            .toList()
}

fun <T> Any.dokkaDelegateClosureOf(action: T.() -> Unit) = object : Closure<Any?>(this, this) {
    @Suppress("unused") // to be called dynamically by Groovy
    fun doCall() = org.gradle.internal.Cast.uncheckedCast<T>(delegate)?.action()
}

val sourcesJar by tasks.creating(Jar::class) {
    classifier = "sources"
    from(java.sourceSets["main"].allSource)
}

publishing {
    publications.create("publication", MavenPublication::class.java) {
        from(components["java"])
        artifact(sourcesJar)
        this.artifactId = base.archivesBaseName
    }
}
