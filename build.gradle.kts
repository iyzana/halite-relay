plugins {
    application
    kotlin("jvm") version "1.2.71"
    id("com.github.johnrengelman.shadow") version "4.0.1"
}

application {
    mainClassName = "com.succcubbus.haliterelay.HaliteRelayKt"
}

dependencies {
    compile(kotlin("stdlib"))
}

repositories {
    jcenter()
}
