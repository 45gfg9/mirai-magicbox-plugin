plugins {
    val kt = "1.4.21"

    kotlin("jvm") version kt
    kotlin("kapt") version kt
    id("net.mamoe.mirai-console") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "net.im45.bot"
version = "1.0.0-dev-1"

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

mirai {
    coreVersion = "2.1.0"
}

kotlin.sourceSets.all {
    languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
}

kotlin.target.compilations.all {
    kotlinOptions.freeCompilerArgs += "-Xjvm-default=enable"
    kotlinOptions.jvmTarget = "11"
}
