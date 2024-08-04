plugins {
    id("java")
}

group = "com.juxest"
version = "0.1"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    compileOnly("com.mybatis-flex:mybatis-flex-core:1.9.5")
    compileOnly("com.juxest:mybatis-flex-reactor-core:0.1")
    compileOnly("io.projectreactor:reactor-core:3.6.6")
    // SpringBoot 注解
    compileOnly("org.springframework:spring-beans:6.1.8")
}