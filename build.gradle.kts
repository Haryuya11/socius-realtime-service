val kotlinVersion: String by project
val logbackVersion: String by project
val rabbitmqVersion: String by project
val coroutinesVersion: String by project
val authVersion: String by project
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    alias(libs.plugins.spotless)
    alias(libs.plugins.dokka)
    alias(libs.plugins.shadow)
}

group = "com.uit"
version = "0.0.1"

application {
    mainClass.set("com.uit.ApplicationKt")
}

tasks {
    shadowJar {
        archiveBaseName.set("socius-realtime-service")
        archiveClassifier.set("")
        archiveVersion.set("")
        manifest {
            attributes["Main-Class"] = "com.uit.ApplicationKt"
        }
    }
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**")

        ktlint(libs.versions.ktlint.get())
            .setEditorConfigPath("$rootDir/.editorconfig")

        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
    }

    kotlinGradle {
        target("*.gradle.kts")
        ktlint(libs.versions.ktlint.get())
        trimTrailingWhitespace()
        endWithNewline()
    }
}

dependencies {
    implementation(libs.bundles.ktor.server)
    implementation(libs.rabbitmq.client)
    implementation(libs.logback.classic)
    implementation(libs.nimbus.jose.jwt)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.auth0.jwt)

    // Azure
    implementation(libs.azure.security.keyvault)
    implementation(libs.azure.identity)

    // Test
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}
