plugins {
    id("java")
}

group = "org.komamitsu"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("com.google.googlejavaformat:google-java-format:1.23.0")
    implementation("org.slf4j:slf4j-simple:2.0.16")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs(
        "-javaagent:${rootDir}/chaos-dukey-all.jar=configFile=${rootDir}/chaos-dukey.properties"
    )
}