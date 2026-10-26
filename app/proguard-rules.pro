# Pleos SDK는 AIDL과 리플렉션을 쓰므로 통째로 보존한다.
-keep class ai.pleos.** { *; }
-dontwarn ai.pleos.**

# kotlinx.serialization DTO
-keepattributes *Annotation*, InnerClasses
-keep,includedescriptorclasses class com.firstroad.mate.data.network.**$$serializer { *; }
-keepclassmembers class com.firstroad.mate.data.network.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
