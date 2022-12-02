import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/*
 *  Copyright (C) 2021 Abhijith Shivaswamy
 *   See the notice.md file distributed with this work for additional
 *   information regarding copyright ownership.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

println("============================")
println("Gradle Running on Java: ${JavaVersion.current()}")
println("============================")

plugins {
    kotlin("jvm") version "1.6.21"
    id("org.jetbrains.dokka") version "1.7.10"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    `java-library`
    `maven-publish`
    signing
    id("com.dorongold.task-tree") version "2.1.0"
    id("io.gitlab.arturbosch.detekt") version "1.18.0"
    jacoco
}

group = "io.github.crackthecodeabhi"
version = "0.8"

repositories {
    mavenCentral()
}

jacoco {
    toolVersion = "0.8.7"
    reportsDirectory.set(layout.buildDirectory.dir("reports/jacoco"))
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        html.required.set(false)
        xml.required.set(true)
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(System.getProperty("SONATYPE_USERNAME"))
            password.set(System.getProperty("SONATYPE_PASSWORD"))
        }
    }
}

dependencies {
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")
    implementation("io.netty:netty-codec-redis:4.1.82.Final")
    implementation("io.netty:netty-handler:4.1.85.Final")
    implementation(kotlin("stdlib"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    testImplementation("io.kotest:kotest-runner-junit5:5.5.4")
    testImplementation("io.kotest:kotest-assertions-core:5.5.4")
    testImplementation("net.swiftzer.semver:semver:1.2.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
    testImplementation("ch.qos.logback:logback-classic:1.4.4")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
    systemProperty("REDIS_PORT",System.getProperty("REDIS_PORT")?: "6379")
}

tasks.withType(JavaCompile::class) {
    targetCompatibility = "11"
    sourceCompatibility = "17"
}

kotlin {
    explicitApi()
}

tasks {
    withType<KotlinCompile>{
        kotlinOptions {
            jvmTarget = "11"
        }
    }
    register<Jar>("dokkaJar") {
        from(dokkaHtml)
        dependsOn(dokkaHtml)
        archiveClassifier.set("javadoc")
    }
    withType<Jar> {
        metaInf.with(
            copySpec {
                from("${project.rootDir}/LICENSE")
            }
        )
    }
    afterEvaluate {
        check {
            dependsOn(withType<io.gitlab.arturbosch.detekt.Detekt>())
        }
    }

    /*withType<PublishToMavenRepository>{
        doFirst {
            val sonaTypeUsername = System.getProperty("SONATYPE_USERNAME")
            val sonaTypePassword = System.getProperty("SONATYPE_PASSWORD")
            if(sonaTypeUsername == null || sonaTypePassword == null || sonaTypeUsername.isBlank() || sonaTypePassword.isBlank())
                throw GradleException("Did you forget to provide SONATYPE_USERNAME || SONATYPE_PASSWORD as System property?")
            println("sonaTypeUsername Length = ${sonaTypeUsername.length}, sonaTypePassword Length =  ${sonaTypePassword.length} ")

            val privateKey = System.getProperty("GPG_PRIVATE_KEY")
            val privatePassword = System.getProperty("GPG_PRIVATE_PASSWORD")
            if(privateKey == null || privatePassword == null || privateKey.isBlank() || privatePassword.isBlank())
                throw GradleException("Did you forget to provide GPG_PRIVATE_KEY || GPG_PRIVATE_PASSWORD as System property?")
            println("GPG_PRIVATE_KEY Length = ${privateKey.length}, GPG_PRIVATE_PASSWORD Length = ${privatePassword.length}")
        }
    }*/
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
            artifacts {
                artifact(tasks["dokkaJar"])
                artifact(tasks.kotlinSourcesJar) {
                    classifier = "sources"
                }
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        System.getProperty("GPG_PRIVATE_KEY"),
        System.getProperty("GPG_PRIVATE_PASSWORD")
    )
    sign(publishing.publications)
}

detekt {
    buildUponDefaultConfig = true
    config = files(rootDir.resolve("detekt.yml"))
    parallel = true
    ignoreFailures = true
    reports {
        html.enabled = false
        txt.enabled = false
    }
}