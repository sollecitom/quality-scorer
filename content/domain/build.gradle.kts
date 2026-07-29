plugins {
    id("sollecitom.kotlin-library-conventions")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.swissknife.test.utils)
}
