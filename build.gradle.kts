import com.jfrog.bintray.gradle.BintrayExtension

plugins {
    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
    `java-gradle-plugin`

    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.3.70"

    id("com.jfrog.bintray") version "1.8.5"
    id("maven-publish")
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

gradlePlugin {
    // Define the plugin
    val dslVanillaPlugin by plugins.creating {
        id = "io.kuberig.dsl.vanilla.plugin"
        implementationClass = "io.kuberig.dsl.vanilla.plugin.KuberigDslVanillaPluginPlugin"
    }
}

// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

gradlePlugin.testSourceSets(functionalTestSourceSet)
configurations.getByName("functionalTestImplementation").extendsFrom(configurations.getByName("testImplementation"))

// Add a task to run the functional tests
val functionalTest by tasks.creating(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
}

val check by tasks.getting(Task::class) {
    // Run the functional tests as part of `check`
    dependsOn(functionalTest)
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")

    val sourceSets: SourceSetContainer by project
    from(sourceSets["main"].allSource)
}

val bintrayApiKey : String by project
val bintrayUser : String by project

configure<PublishingExtension> {

    publications {
        register(project.name, MavenPublication::class.java) {
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }

}

configure<BintrayExtension> {
    user = bintrayUser
    key = bintrayApiKey
    publish = true

    pkg(closureOf<BintrayExtension.PackageConfig> {
        repo = "rigeldev-oss-maven"
        name = project.name
        setLicenses("Apache-2.0")
        vcsUrl = "https://github.com/teyckmans/kuberig-dsl-kubernetes"
    })

    setPublications(project.name)
}