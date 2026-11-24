plugins {
    `kotlin-dsl`
}

repositories { mavenCentral() }

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

gradlePlugin {
    plugins {
        create("cheatutils") {
            id = "cheatutils"
            implementationClass = "com.zergatul.cheatutils.CheatUtilsGradlePlugin"
        }
    }
}