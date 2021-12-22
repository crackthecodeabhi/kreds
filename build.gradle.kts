plugins {
    kotlin("jvm") version "1.6.0"
    id("org.jetbrains.dokka") version "1.6.0"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    `java-library`
    `maven-publish`
    signing
}

group = "io.github.crackthecodeabhi"
version = "0.3"

repositories {
    mavenCentral()
}

nexusPublishing {
    repositories {
        sonatype{
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(properties["sonaTypeUsername"] as String)
            password.set(properties["sonaTypePassword"] as String)
        }
    }
}

dependencies {
    implementation("io.netty:netty-codec-redis:4.1.72.Final")
    implementation("io.netty:netty-handler:4.1.72.Final")
    implementation(kotlin("stdlib"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-RC3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType(JavaCompile::class) {
    targetCompatibility = "17"
    sourceCompatibility = "17"
}

kotlin {
    explicitApi()
}

tasks {
    register<Jar>("dokkaJar"){
        from(dokkaHtml)
        dependsOn(dokkaHtml)
        archiveClassifier.set("javadoc")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            pom {
                name.set("Kreds")
                description.set("A Non-blocking Redis client for Kotlin based on coroutines.")
                url.set("https://github.com/crackthecodeabhi/kreds")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("abhi")
                        name.set("Abhijith Shivaswamy")
                        email.set("abs@abhijith.page")
                        url.set("abhijith.page")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/crackthecodeabhi/kreds.git")
                    developerConnection.set("scm:git:ssh://github.com/crackthecodeabhi/kreds.git")
                    url.set("https://github.com/crackthecodeabhi/kreds/tree/master")
                }
            }
            groupId = project.group as String
            artifactId = project.name
            version = project.version as String
            from(components["java"])
            artifacts{
                artifact(tasks["dokkaJar"])
                artifact(tasks.kotlinSourcesJar){
                    classifier = "sources"
                }
            }
        }
    }
}

signing {
    sign(publishing.publications)
}