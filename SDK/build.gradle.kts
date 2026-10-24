plugins { alias(libs.plugins.android.library) }

android {
    namespace = "com.kit.jedyapp.sdk"
    compileSdk { version = release(37) }
    defaultConfig { minSdk = 28 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
