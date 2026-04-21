# aw-image ProGuard Rules
# 此文件用于库自身的 release 构建混淆规则
# Consumer-facing rules（供使用者混淆时使用）位于 consumer-rules.pro

# ===========================================================
# 保留 Kotlin 元数据和注解
# ===========================================================

-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes Exceptions
-keep class kotlin.Metadata { *; }
-keep class kotlin.coroutines.jvm.internal.BaseContinuationImpl { *; }

# ===========================================================
# 保留协程相关类（预加载功能依赖）
# ===========================================================

-keepclassmembers class ** {
    @kotlin.coroutines.jvm.internal.DebugMetadata *;
}

# ===========================================================
# 保留枚举
# ===========================================================

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ===========================================================
# 保留 Serializable
# ===========================================================

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ===========================================================
# 保留 Parcelable CREATOR
# ===========================================================

-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ===========================================================
# 保留网络回调类（ImageNetworkMonitor 使用 ConnectivityManager.NetworkCallback）
# ===========================================================

-keepclassmembers class com.answufeng.image.ImageNetworkMonitor$ensureRegistered$1 {
    *;
}

# ===========================================================
# Coil 规则（Coil 自带 consumer rules，此处仅保留必要的库自身构建规则）
# ===========================================================

-dontwarn okio.**
-dontwarn org.codehaus.mojo.**
