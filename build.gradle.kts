import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm") version "1.9.20"

    id("com.vanniktech.maven.publish") version "0.28.0"
}

group = "com.juxest"
version = "0.1"

repositories {
    mavenCentral()
}

java {
    withSourcesJar()
    withJavadocJar()
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "com.vanniktech.maven.publish")

    tasks.javadoc {
        options {
            encoding = "UTF-8"
        }
    }

    mavenPublishing {
        pom {
            name = "Mybatis Flex Reactor Extensions"
            description = "Mybatis Flex 响应式扩展包"
            licenses {
                license {
                    name = "The Apache License, Version 2.0"
                    url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                }
            }
            developers {
                developer {
                    id = "Juxest"
                    name = "Juxest"
                    email = "1477007136@qq.com"
                }
            }
        }

        publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

        signAllPublications()
    }
}