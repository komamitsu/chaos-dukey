plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.komamitsu"
version = "1.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.bytebuddy:byte-buddy:1.14.12")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testImplementation("org.mockito:mockito-core:4.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:4.11.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes(mapOf("Premain-Class" to "org.komamitsu.chaosdukey.ChaosDukeyAgent"))
    }
}
