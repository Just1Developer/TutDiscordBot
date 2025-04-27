plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "net.justonedev"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("net.dv8tion:JDA:5.4.0") { // replace $version with the latest version
        // Optionally disable audio natives to reduce jar size by excluding `opus-java` and `tink`
        exclude(module="opus-java") // required for encoding audio into opus, not needed if audio is already provided in opus encoding
        exclude(module="tink") // required for encrypting and decrypting audio
    }
}

// configure the Shadow jar
tasks.shadowJar {
    archiveClassifier.set("")    // drop the “-all” suffix if you like
    manifest {
        attributes(
            "Main-Class" to "net.justonedev.braten.Main"
        )
    }
}

tasks.test {
    useJUnitPlatform()
}