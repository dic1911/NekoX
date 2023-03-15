import java.io.ByteArrayInputStream
import java.util.Base64
import java.util.Properties
import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.failure

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

fun setupSigningConfig() = Properties().apply {
    val base64: String? = System.getenv("LOCAL_PROPERTIES")
    if (!base64.isNullOrBlank()) {
        load(ByteArrayInputStream(Base64.getDecoder().decode(base64)))
    } else if (project.rootProject.file("local.properties").exists()) {
        load(project.rootProject.file("local.properties").inputStream())
    }
    if (getProperty("KEYSTORE_PASS")?.isBlank() == true) {
        setProperty("KEYSTORE_PASS", System.getenv("KEYSTORE_PASS"))
        setProperty("ALIAS_NAME", System.getenv("ALIAS_NAME"))
        setProperty("ALIAS_PASS", System.getenv("ALIAS_PASS"))
    }
}



android {
    namespace = "nekox.messenger.sing"
    compileSdk = 33

    defaultConfig {
        applicationId = "nekox.messenger.sing"
        minSdk = 21
        targetSdk = 33
        versionCode = 100
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        val releaseSigningKey = setupSigningConfig()
        if (!releaseSigningKey.getProperty("KEYSTORE_PASS").isNullOrBlank()){
            create("release") {
                storeFile = file("../TMessagesProj/release.keystore")
                storePassword = releaseSigningKey.getProperty("KEYSTORE_PASS")
                keyAlias = releaseSigningKey.getProperty("ALIAS_NAME")
                keyPassword = releaseSigningKey.getProperty("ALIAS_PASS")
            }
        }
    }

    buildTypes {
        release {
//            sourceSets["main"].jniLibs.srcDirs.plus(File(project.path, "libs"))
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = if(signingConfigs.names.contains("release")) signingConfigs.getByName("release") else signingConfigs.getByName("debug")
        }
    }
    splits {
        abi {
            isEnable = true
            isUniversalApk = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.10")
//    implementation(fileTree("libs"))
}

val singboxCommit = "ef73c6f2a9e5b40028d9bdc0c7e7023c32010fb1" // Release 1.1.6

val nativeBuild = task("native-build-sing-box") {
    if (Os.isFamily(Os.FAMILY_WINDOWS)) {
        println("Build in Windows is not supported")
    } else {
        exec {
            workingDir(projectDir)
            executable("/bin/bash")
            args("build-sing-box", singboxCommit, android.defaultConfig.minSdkVersion)
            environment("ANDROID_HOME", android.sdkDirectory)
            environment("ANDROID_NDK_HOME", android.ndkDirectory)
        }
    }
}

tasks.whenTaskAdded {
    if (name.contains("javaPreCompile")) {
        dependsOn(nativeBuild)
    }
}
