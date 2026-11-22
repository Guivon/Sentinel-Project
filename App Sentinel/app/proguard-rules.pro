-keepattributes Signature,InnerClasses,EnclosingMethod,RuntimeVisibleAnnotations,AnnotationDefault,*Annotation*
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-keepclassmembers class * {
    @com.google.api.client.util.Key <fields>;
}
-keepclassmembers class * {
    @com.google.api.client.util.Value <fields>;
}
-keep class com.google.api.services.gmail.** { *; }
-keep class com.google.api.client.** { *; }
-dontwarn com.google.api.client.**
-dontwarn com.google.common.**
-dontwarn javax.naming.**
-dontwarn org.apache.http.**
-dontwarn org.ietf.jgss.**
-dontwarn sun.misc.**
-dontwarn com.google.errorprone.annotations.**

-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class io.github.jan.supabase.**$$serializer { *; }
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
}
-dontwarn kotlinx.serialization.**

-dontwarn io.ktor.**
-dontwarn org.slf4j.**
-dontwarn java.lang.management.**
-dontwarn reactor.blockhound.**

-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep class androidx.credentials.** { *; }
-dontwarn androidx.credentials.**
