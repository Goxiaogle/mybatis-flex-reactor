plugins {
    id("java")
}

group = "com.juxest"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    // flex
    compileOnly("com.mybatis-flex:mybatis-flex-core:1.9.5")
    // reactor
    compileOnly("io.projectreactor:reactor-core:3.6.6")
}