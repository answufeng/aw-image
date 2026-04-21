# aw-image ProGuard Rules
# 此文件用于库自身的 release 构建混淆规则
# Consumer-facing rules（供使用者混淆时使用）位于 consumer-rules.pro

# ===========================================================
# 保留公共 API 和 Coil 相关类
# ===========================================================

# 保留所有公共类
-keep class com.answufeng.image.** { *; }

# 保留 Coil 相关类
-keep class coil.** { *; }

# 保留 Transformation 相关类
-keep class coil.size.** { *; }
-keep class coil.request.** { *; }
-keep class coil.transform.** { *; }

# 保留 Kotlin 反射和元数据
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes Exceptions
-keep class kotlin.Metadata { *; }

# ===========================================================
# 保留枚举和 sealed class
# ===========================================================

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ===========================================================
# 保留 Android Graphics 相关类
# ===========================================================

-keep class android.graphics.** { *; }
