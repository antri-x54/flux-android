-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod


-keep class org.nikanikoo.flux.** { *; }

-keep class org.nikanikoo.flux.data.models.Post { *; }
-keep class org.nikanikoo.flux.data.models.Message { *; }
-keep class org.nikanikoo.flux.data.models.Friend { *; }
-keep class org.nikanikoo.flux.data.models.UserProfile { *; }
-keep class org.nikanikoo.flux.data.models.Group { *; }
-keep class org.nikanikoo.flux.data.models.Conversation { *; }
-keep class org.nikanikoo.flux.data.models.Notification { *; }
-keep class org.nikanikoo.flux.data.models.FriendRequest { *; }

-keep interface org.nikanikoo.flux.OpenVKApi$** { *; }
-keep interface org.nikanikoo.flux.**$*Callback { *; }
-keep interface org.nikanikoo.flux.**$*Listener { *; }

-keep class org.nikanikoo.flux.utils.** { *; }

-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

-dontwarn com.squareup.picasso.**
-keep class com.squareup.picasso.** { *; }

-keep class com.github.chrisbanes.photoview.** { *; }

-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

-keepclassmembers class * {
    @org.json.** *;
}

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepclassmembers class * {
    public <init>(...);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

-assumenosideeffects class java.lang.System {
    public static void out.println(...);
    public static void err.println(...);
}

-assumenosideeffects class org.nikanikoo.flux.utils.Logger {
    public static void d(...);
    public static void i(...);
    public static void w(...);
}

-dontwarn java.lang.invoke.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**