plugins {
    id("com.android.application") version "8.12.2" apply false
    id("com.android.library") version "8.12.2" apply false
    kotlin("android") version "2.1.20" apply false
    kotlin("kapt") version "2.1.20" apply false
    id("com.google.dagger.hilt.android") version "2.57.1" apply false
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}
