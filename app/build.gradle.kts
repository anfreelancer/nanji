plugins {
  id("com.android.application")
  kotlin("android")
}

android {
  val sdkVersion = 30
  compileSdkVersion(sdkVersion)

  buildFeatures {
    viewBinding = true
  }

  defaultConfig {
    applicationId = "com.luteapp.nanji"
    minSdkVersion(16)
    targetSdkVersion(sdkVersion)
    versionCode = 39
    versionName = "1.4.0"
    resConfigs("en", "ja", "ru")
    vectorDrawables.useSupportLibrary = true
  }

  buildTypes {
    getByName("release") {
      isShrinkResources = true
      isMinifyEnabled = true
      proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
    }
  }
}

dependencies {
  implementation(libs.kotlinStdlib)
  implementation(libs.kprefs)
  implementation(libs.anyadapter)
  implementation(libs.colorpicker)
  implementation(libs.androidxCoreKtx)
  implementation(libs.androidxAppCompat)
  implementation(libs.androidxRecyclerView)
  implementation(libs.androidxConstraintLayout)
  implementation("com.android.billingclient:billing:4.0.0")
  implementation("androidx.cardview:cardview:1.0.0")

  testImplementation(libs.kotlinTestJunit)
}
