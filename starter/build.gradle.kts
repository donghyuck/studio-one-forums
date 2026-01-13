plugins {
    id("java-library")
    id("maven-publish") 
    id("io.spring.dependency-management") 
}
group = rootProject.findProperty("buildStarterGroup") as String
version = rootProject.version
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of((rootProject.findProperty("javaVersion") as String).toInt()))
    }
    withSourcesJar()
    withJavadocJar()
}

dependencies { 
    compileOnly("studio.one.api:studio-platform-autoconfigure:${rootProject.findProperty("studioOneVersion")}")
    compileOnly("studio.one.starter:studio-platform-starter:${rootProject.findProperty("studioOneVersion")}")
    api(project(":"))
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    compileOnly("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    // lombok
    val lombokVersion: String = rootProject.findProperty("lombokVersion") as String? ?: "1.18.30"
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
	testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${rootProject.findProperty("springBootVersion")}")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = rootProject.findProperty("buildStarterName") as String
        }
    }
    repositories {
        maven {
            val allowInsecure = (rootProject.findProperty("nexus.allowInsecure") as String).toBoolean()
            isAllowInsecureProtocol = allowInsecure
            val isSnapshot = version.toString().endsWith("SNAPSHOT")
            url = uri(
                if (isSnapshot) (rootProject.findProperty("nexus.snapshotsUrl") as String)
                else (rootProject.findProperty("nexus.releasesUrl") as String)
            )
            credentials {
                username = (rootProject.findProperty("nexus.username") as String?) ?: System.getenv("NEXUS_USERNAME")
                password = (rootProject.findProperty("nexus.password") as String?) ?: System.getenv("NEXUS_PASSWORD")
            }
        }
    }
}
