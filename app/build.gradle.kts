import java.util.Properties

plugins {
    // AGP 9 compiles Kotlin itself (built-in Kotlin), so the standalone
    // `org.jetbrains.kotlin.android` plugin must NOT be applied here.
    alias(libs.plugins.android.application)
    // The Compose compiler plugin still has to be applied explicitly.
    alias(libs.plugins.kotlin.compose)
}

/**
 * Release signing material.
 *
 * Read from `keystore.properties` locally (git-ignored) or from environment variables on CI, both
 * backed by the same keys. Without it the release APK is signed with the runner's debug key, which
 * keeps CI artifacts installable instead of emitting an unsigned APK.
 */
val signingProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun signingValue(key: String): String? = signingProperties.getProperty(key) ?: System.getenv(key)

val keystorePath = signingValue("KEYSTORE_PATH")
val keystorePassword = signingValue("KEYSTORE_PASSWORD")
val releaseKeyAlias = signingValue("KEY_ALIAS")
val releaseKeyPassword = signingValue("KEY_PASSWORD")
val hasReleaseSigning =
    keystorePath != null && keystorePassword != null && releaseKeyAlias != null && releaseKeyPassword != null

android {
    namespace = "top.youzix.nekonote"
    compileSdk = 37

    defaultConfig {
        applicationId = "top.youzix.nekonote"
        minSdk = 33
        targetSdk = 35
        versionCode = 9
        versionName = "0.1.8"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(keystorePath!!)
                storePassword = keystorePassword!!
                keyAlias = releaseKeyAlias!!
                keyPassword = releaseKeyPassword!!
                enableV1Signing = false
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }

    buildTypes {
        release {
            // R8 is what keeps the APK small: material-icons-extended alone carries several
            // thousand ImageVector definitions that nothing here references.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig =
                if (hasReleaseSigning) signingConfigs.getByName("release")
                else signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// `kotlin.compilerOptions.jvmTarget` is not set on purpose: with built-in Kotlin it already
// defaults to `android.compileOptions.targetCompatibility` (Java 21).

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.foundation)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.material.icons)

    // The second UI engine is written against Material Design.
    implementation(libs.compose.material3)

    implementation(libs.miuix.ui)
    implementation(libs.miuix.preference)
    // The liquid-glass floating bottom bar is built on miuix-blur, which requires minSdk 33.
    implementation(libs.miuix.blur)
    // SukiSU's navigation stack: NavDisplay owns the second-level pages and the platform
    // predictive-back gesture, so there is no hand-written back animation here.
    implementation(libs.miuix.nav)
    // The miuix search icon set and the back-event bridge, both used by SukiSU's search bar.
    implementation(libs.miuix.icons)
    implementation(libs.androidx.navigationevent.compose)

    implementation(libs.androidx.activity.compose)

    debugImplementation("androidx.compose.ui:ui-tooling")
}
