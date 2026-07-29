plugins {
    id("sollecitom.kotlin-library-conventions")
}

dependencies {
    api(projects.domain)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.swissknife.test.utils)
}
