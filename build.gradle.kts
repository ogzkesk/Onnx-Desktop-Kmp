plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}

tasks.register("runWithAdmin") {
    doLast {
        val command = "powershell -Command \"Start-Process cmd -ArgumentList '/c gradlew run' -Verb runAs\""
        val process = Runtime.getRuntime().exec(command)
        process.waitFor()
    }
}