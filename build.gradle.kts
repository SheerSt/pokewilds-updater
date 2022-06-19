group = "com.pkmngen.updater"

plugins {
    java

    id("com.github.johnrengelman.shadow") version "7.1.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("commons-io:commons-io:2.11.0")
}