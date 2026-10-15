plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "ir.ariakish.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "ir.ariakish.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 100
        versionName = "1.0.0"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildTypes {
        debug {
            buildConfigField("String", "STORE_API", "\"https://ariakish-89.ir/wp-json/wc/store/v1/\"")
            buildConfigField("String", "BRIDGE_API", "\"https://ariakish-89.ir/wp-json/ariakish/v1/\"")
            buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"\"")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("String", "STORE_API", "\"https://ariakish-89.ir/wp-json/wc/store/v1/\"")
            buildConfigField("String", "BRIDGE_API", "\"https://ariakish-89.ir/wp-json/ariakish/v1/\"")
            buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"\"")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("androidx.browser:browser:1.8.0")

    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
