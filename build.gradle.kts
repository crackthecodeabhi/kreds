plugins {
    kotlin("jvm") version "1.6.0"
    id("org.jetbrains.dokka") version "1.6.0"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    `java-library`
    `maven-publish`
    signing
    id("com.dorongold.task-tree") version "2.1.0"
}

group = "io.github.crackthecodeabhi"
version = "0.5"

repositories {
    mavenCentral()
}

nexusPublishing {
    repositories {
        sonatype{
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(System.getProperty("SONATYPE_USERNAME"))
            password.set(System.getProperty("SONATYPE_PASSWORD"))
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
    useInMemoryPgpKeys(
        System.getProperty("GPG_PRIVATE_KEY"),
        System.getProperty("GPG_PRIVATE_PASSWORD")
    )
    sign(publishing.publications)
}