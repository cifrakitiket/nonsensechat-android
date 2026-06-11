# Supabase-kt / Ktor use kotlinx.serialization reflection-free; keep model + serializer classes.
-keepattributes *Annotation*, InnerClasses
-keep,includedescriptorclasses class com.nonsense.chat.**$$serializer { *; }
-keepclassmembers class com.nonsense.chat.** {
    *** Companion;
}
-keepclasseswithmembers class com.nonsense.chat.** {
    kotlinx.serialization.KSerializer serializer(...);
}
# Ktor
-dontwarn org.slf4j.**
-dontwarn io.ktor.**
