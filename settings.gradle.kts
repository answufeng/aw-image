pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()

        // 可选：国内加速镜像（默认关闭，避免 CI/JitPack 因 502 失败）
        val useAliyunMirror = providers.gradleProperty("useAliyunMirror").orNull == "true"
        if (useAliyunMirror) {
            maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
            maven { url = uri("https://maven.aliyun.com/repository/google") }
            maven { url = uri("https://maven.aliyun.com/repository/central") }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // 可选：国内加速镜像（默认关闭，避免 CI/JitPack 因 502 失败）
        val useAliyunMirror = providers.gradleProperty("useAliyunMirror").orNull == "true"
        if (useAliyunMirror) {
            maven { url = uri("https://maven.aliyun.com/repository/google") }
            maven { url = uri("https://maven.aliyun.com/repository/central") }
        }
    }
}

rootProject.name = "aw-image"

include(":aw-image")
include(":demo")
