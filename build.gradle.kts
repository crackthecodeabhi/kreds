plugins {
    kotlin("jvm") version "1.6.0"
    java
}

group = "io.github.crackthecodeabhi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.netty:netty-codec-redis:4.1.72.Final")
    implementation("io.netty:netty-handler:4.1.72.Final")
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-RC3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.0-RC3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType(JavaCompile::class){
    targetCompatibility = "17"
    sourceCompatibility = "17"
}