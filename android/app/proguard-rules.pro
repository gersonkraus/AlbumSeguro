# Add project specific ProGuard rules here.

# Preservar assinaturas de tipo genérico (obrigatório para Gson + Retrofit)
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Gson — preservar TypeToken e subclasses (evita Class cannot be cast to ParameterizedType)
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken
-keep class com.google.gson.** { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Modelos de dados usados pelo Gson
-keep class com.familiaaco.data.models.** { *; }

# Retrofit — preservar interfaces e métodos anotados
-keep,allowobfuscation,allowshrinking interface * {
    @retrofit2.http.* <methods>;
}
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Kotlin coroutines + Retrofit suspend functions
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-keep class kotlin.coroutines.** { *; }
-keep class kotlinx.coroutines.** { *; }
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations

# ApiService — manter interface e métodos (Retrofit usa reflexão)
-keep interface com.familiaaco.network.ApiService { *; }

# Media3 — preservar classes de UI para evitar ClassCastException na inflação de layout
-keep class androidx.media3.** { *; }
-keep interface androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Desativar todas as otimizações R8 (resolve ClassCastException em Gson + Retrofit)
# O shrinking (remoção de código não usado) continua ativo
-dontoptimize
