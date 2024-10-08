plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    if (JavaVersion.current().isJava11Compatible) {
        id("com.diffplug.spotless") version "6.25.0"
    }
    else {
        id("com.diffplug.spotless") version "6.13.0"
    }
}

group = "org.komamitsu"
version = "1.4.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.bytebuddy:byte-buddy:1.14.12")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testImplementation("org.mockito:mockito-core:4.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:4.11.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes(mapOf("Premain-Class" to "org.komamitsu.chaosdukey.Agent"))
    }
}

spotless {
    java {
        target("src/*/java/**/*.java")
        importOrder()
        removeUnusedImports()
        googleJavaFormat()
    }
}

sourceSets {
    create("intTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val intTestImplementation by configurations.getting {
    extendsFrom(configurations.implementation.get())
}
val intTestRuntimeOnly by configurations.getting

configurations["intTestRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())

dependencies {
    intTestImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    intTestRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val integrationTest = task<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = sourceSets["intTest"].output.classesDirs
    classpath = sourceSets["intTest"].runtimeClasspath

    dependsOn("shadowJar")

    useJUnitPlatform()

    val configFile = project.layout.buildDirectory.dir("resources").get()
            .dir("intTest").file("chaos-dukey.properties").asFile.absoluteFile

    jvmArgs("-javaagent:build/libs/${project.name}-${project.version}-all.jar=configFile=${configFile}")
}

tasks.check { dependsOn(integrationTest) }
