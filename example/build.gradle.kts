plugins {
    id("java")
    id("application")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("com.google.googlejavaformat:google-java-format:1.23.0")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass = "org.komamitsu.example.Main"
    applicationDefaultJvmArgs = listOf("-javaagent:/home/komamitsu/Downloads/chaos-dukey-1.4.0-all.jar=configFile=/home/komamitsu/src/chaos-dukey/example/chaos-dukey.properties")
}
