plugins {
    id("java")
    id("java-library")
}

group = "com.juxest"
version = "0.2.1"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    compileOnly("com.mybatis-flex:mybatis-flex-core:1.9.5")
    api("com.juxest:mybatis-flex-reactor-core:0.2.1")
    // SpringBoot 注解
    compileOnly("org.springframework:spring-beans:6.1.8")
}