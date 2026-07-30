plugins {
    id("sollecitom.kotlin-library-conventions")
}

dependencies {
    implementation(projects.domain)
    implementation(projects.kotlinModel)
    implementation(projects.kotlinRules)
    implementation(projects.kotlinAnalysis)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.swissknife.test.utils)
}

// The vendored grader artefact: a single runnable fat jar the task harness invokes offline.
val fatJar by tasks.registering(Jar::class) {
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest { attributes["Main-Class"] = "sollecitom.quality.scorer.app.MainKt" }
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({ configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) } })
}
