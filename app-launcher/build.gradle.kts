plugins { id("com.android.application") }

android {
    namespace = "org.minimalos.launcher"
    compileSdk = 35
    buildToolsVersion = "35.0.0"

    defaultConfig {
        applicationId = "org.minimalos.launcher"
        minSdk = 21
        targetSdk = 35
        versionCode = 4
        versionName = "0.4.0"
        testInstrumentationRunner = "android.test.InstrumentationTestRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = false
        disable += "OldTargetApi"
    }
}

dependencies {
    implementation(project(":core-device"))
    implementation(project(":core-profiles"))
    implementation(project(":core-journal"))
    implementation(project(":core-debloat"))
    implementation(project(":core-recovery"))
    implementation(project(":core-replication"))
    testImplementation("junit:junit:4.13.2")
}
