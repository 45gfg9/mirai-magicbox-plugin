plugins {
    val kt = "1.4.20"

    kotlin("jvm") version kt
    kotlin("kapt") version kt
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "net.im45.bot"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

dependencies {
    val miraiCore = "1.3.3"
    val miraiConsole = "1.1.0"
    val autoService = "1.0-rc7"

    kapt("com.google.auto.service:auto-service:$autoService")
    compileOnly(kotlin("stdlib"))
    compileOnly("com.google.auto.service:auto-service-annotations:$autoService")
    compileOnly("net.mamoe:mirai-core:$miraiCore")
    compileOnly("net.mamoe:mirai-console:$miraiConsole")
}

kotlin.sourceSets.all {
    languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
}

kotlin.target.compilations.all {
    kotlinOptions.freeCompilerArgs += "-Xjvm-default=enable"
    kotlinOptions.jvmTarget = "11"
}
