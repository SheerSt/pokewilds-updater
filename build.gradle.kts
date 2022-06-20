version = "1.0.0"
group = "com.pkmngen.updater"

plugins {
    java
    application

    id("com.github.johnrengelman.shadow") version "7.1.1"
}

application {
    mainClass.set("com.pkmngen.updater.UpdaterApplication")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("commons-io:commons-io:2.11.0")
}