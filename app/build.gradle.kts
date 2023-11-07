plugins {
    id("com.android.application")

    id("com.google.gms.google-services")
}

android {
    namespace = "com.programmerxd.wod"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.programmerxd.wod"
        minSdk = 28
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-database:20.3.0")
    implementation("com.google.firebase:firebase-auth:22.2.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation(platform("com.google.firebase:firebase-bom:32.5.0"))
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.28")

    //calls mesibo
//    implementation("com.mesibo.api:calls:project.MesiboVersion_Calls")
//    implementation("com.mesibo.api:calls:2.2.5")

//    implementation("io.agora.rtc:agora-rtc-sdk:3.6.2") // Use the latest version available
//    implementation("io.agora.rtc:full-sdk:3.6.2") // Use the latest version available
//    implementation("io.agora.rtc:agora-rtc-sdk:3.6.2")
//    implementation("io.agora.rtc:agora-rtc-RtcTokenBuilder:3.6.1")


//    implementation("io.agora.rtc:voice-sdk:4.0.0.4") // Use the latest version available


//    implementation("io.agora.rtc:full-sdk:4.2.3") // Use the latest version available
    implementation("io.agora.rtc:voice-sdk:4.0.0.4") // Use the latest version available
    implementation(group = "commons-codec", name = "commons-codec", version = "1.15")

    implementation("com.google.android.gms:play-services-wearable:17.0.0")
    
}