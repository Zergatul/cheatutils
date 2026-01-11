plugins {
    `kotlin-dsl`
}

repositories { mavenCentral() }

gradlePlugin {
    plugins {
        create("cheatutils") {
            id = "cheatutils"
            implementationClass = "com.zergatul.cheatutils.CheatUtilsGradlePlugin"
        }
    }
}