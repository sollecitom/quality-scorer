plugins {
    id("sollecitom.kotlin-library-conventions")
}

dependencies {
    api(projects.kotlinModel)
    implementation(libs.kotlin.compiler.embeddable)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.swissknife.test.utils)
}
