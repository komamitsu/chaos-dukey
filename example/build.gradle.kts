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
        "-javaagent:/home/komamitsu/Downloads/chaos-dukey-1.4.0-all.jar=configFile=/home/komamitsu/src/chaos-dukey/example/chaos-dukey.properties",
//        "-Dorg.slf4j.simpleLogger.showDateTime=true",
//        "-Dorg.slf4j.simpleLogger.dateTimeFormat='yyyy-MM-dd HH:mm:ss:SSS Z'"
    )
}