plugins {
    id("java")
    id("java-library")
}

group = "com.juxest"
version = "0.2.2"

repositories {
    mavenCentral()
}

dependencies {
    // flex
    compileOnly("com.mybatis-flex:mybatis-flex-core:1.9.5")
    // reactor
    api("io.projectreactor:reactor-core:3.6.6")
}