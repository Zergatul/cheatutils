plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

java.toolchain.languageVersion = JavaLanguageVersion.of(17)

gradlePlugin {
    plugins {
        create("cheatutils") {
            id = "cheatutils"
            implementationClass = "com.zergatul.cheatutils.CheatUtilsGradlePlugin"
        }
    }
}