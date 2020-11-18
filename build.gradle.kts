import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.http.*
import java.net.*

buildscript {
    dependencies {
        classpath("com.squareup.moshi:moshi:1.11.0")
    }
}

plugins {
    kotlin("jvm") version "1.4.10"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}
group = "org.tribot"
version = "1.0-SNAPSHOT"

val tbVersion = getTribotVersion()

allprojects {
    repositories {
        jcenter()
        maven {
            setUrl("https://gitlab.com/api/v4/projects/20741387/packages/maven")
        }
    }
}

dependencies {
    implementation("org.tribot:tribot-client:${tbVersion}")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "org.tribot.TRiBot"
        }
        finalizedBy(shadowJar)
    }
    shadowJar {
        archiveBaseName.set("Tribot")
        archiveClassifier.set("")
        archiveVersion.set(tbVersion)
    }
}

fun getTribotVersion(): String? {
    val client = HttpClient.newHttpClient()
    val req = HttpRequest.newBuilder()
        .uri(URI.create("https://gitlab.com/api/v4/projects/20741387/packages?sort=desc&order_by=version&package_name=tribot-client"))
        .build()
    val response = client.send(req, HttpResponse.BodyHandlers.ofString())
        .body()

    com.squareup.moshi.Moshi.Builder().build()
        .adapter<List<Map<String, Any>>>(List::class.java)
        .fromJson(response)
        ?.forEach {
            if (it["version"] != "12.0.1-rc1")
                return it["version"] as? String?
        } ?: println("Parse error")

    return null
}