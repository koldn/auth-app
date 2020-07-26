import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.72"
}

group = "org.authapp"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.72")
    implementation("io.ktor:ktor-server-netty:1.3.2")
    implementation("io.jsonwebtoken:jjwt:0.9.1")

    //Database
    implementation("org.postgresql:postgresql:42.2.14")
    implementation("com.zaxxer:HikariCP:3.4.5")

    implementation("org.jetbrains.exposed", "exposed-core", "0.24.1")
    implementation("org.jetbrains.exposed", "exposed-dao", "0.24.1")
    implementation("org.jetbrains.exposed", "exposed-jdbc", "0.24.1")

    implementation("org.kodein.di:kodein-di:7.0.0")
    implementation("org.kodein.di:kodein-di-framework-ktor-server-jvm:7.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
    testImplementation("io.ktor:ktor-server-tests:1.3.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

sourceSets {
    main {
        java.srcDirs("src/main/kotlin")
    }
    test {
        java.srcDirs("src/test/kotlin")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}
