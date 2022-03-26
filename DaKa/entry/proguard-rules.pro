-dontwarn
# 代码混淆压缩比，在0~7之间
-optimizationpasses 5
# 混合时不使用大小写混合，混合后的类名为小写
-dontusemixedcaseclassnames
# 在读取依赖的库文件时，不要略过那些非public类成员
-dontskipnonpubliclibraryclassmembers
# 指定不去忽略非公共库的类
-dontskipnonpubliclibraryclasses
# 不做预校验，preverify是proguard的四个步骤之一，去掉这一步能够加快混淆速度。
-dontpreverify
-verbose
# google推荐算法
#-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-dontoptimize#注意在上传时配置，本地无需配置
# 保留注解、内部类、泛型、匿名类
-keepattributes *Annotation*,Exceptions,InnerClasses,Signature,EnclosingMethod
# 重命名抛出异常时的文件名称
-renamesourcefileattribute SourceFile
# 抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable
-dontwarn javax.annotation.**
# 保留本地native方法不被混淆
-keepclasseswithmembernames,allowshrinking class * {
native <methods>;
}
# 保留枚举类不被混淆
-keepclassmembers enum * {
public static **[] values();
public static ** valueOf(java.lang.String);
}
# 保留自定义类
-keep class com.xcl.location.MyApplication
-keep class com.xcl.location.Net.**{*;}

# 忽略数据库有关
-keep class com.litesuits.orm.**
-keepclassmembers class com.litesuits.orm.**{*;}
-keep enum com.litesuits.orm.**
-keepclassmembers enum com.litesuits.orm.**{*;}
-keep interface com.litesuits.orm.**
-keepclassmembers interface com.litesuits.orm.**{*;}
-keep class com.xcl.study.DataANet.**{*;}
# 忽略继承
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
# 保留HMS相关接入[AnalyticsKit SDK和依赖SDK的混淆配置]
-ignorewarnings
-repackageclasses
-keep class com.huawei.agconnect.**{*;}
-keep class com.huawei.hms.analytics.**{*;}
-keep class com.huawei.hms.push.**{*;}
-keep class com.huawei.hms.**{*;}
# 保留fastjson
#-dontwarn com.alibaba.fastjson.**
#-keep class com.alibaba.fastjson.**{*; }
# 保留okhttp
-keep class com.squareup.okhttp.** { *;}
-dontwarn com.squareup.okhttp.**
-dontwarn org.apache.http.**
-keep class okio.**{*;}
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
# HMS接口服务
-keepattributes Exceptions
-keep interface com.huawei.hms.analytics.type.HAEventType{*;}
-keep interface com.huawei.hms.analytics.type.HAParamType{*;}
-keep class com.huawei.hms.analytics.HiAnalyticsInstance{*;}
-keep class com.huawei.hms.analytics.HiAnalytics{*;}
-keep class com.huawei.hianalytics.**{*;}
-keep class com.huawei.updatesdk.**{*;}
-keep class com.huawei.harmony.**{*;}
-keep class com.huawei.mylibrary.**{*;}
# 保留HarmonyOS应用/服务入口类
-keep public class * extends *.aafwk.ability.Ability
-keep public class * extends *.ace.ability.AceAbility
-keep public class * extends *.aafwk.ability.AbilitySlice
-keep public class * extends *.aafwk.ability.AbilityPackage
-dontwarn java.lang.invoke.**
-dontwarn javax.naming.**
# 保留HarmonyOS定位服务
-keep public class com.huawei.hms.location.harmony.* {*;}
-keep public class com.huawei.hms.location.harmony.base.* {*;}
-keep class com.huawei.hmf.tasks.* {*;}
#网络有关
#okgo
-dontwarn com.lzy.okgo.**
-keep class com.lzy.okgo.**{*;}

#okrx
-dontwarn com.lzy.okrx.**
-keep class com.lzy.okrx.**{*;}

#okrx2
-dontwarn com.lzy.okrx2.**
-keep class com.lzy.okrx2.**{*;}

#okserver
-dontwarn com.lzy.okserver.**
-keep class com.lzy.okserver.**{*;}

-keepattributes Signature, InnerClasses, EnclosingMethod, Exceptions
# 蒹葭
-dontwarn com.net.jianjia.**
-keep class com.net.jianjia.** { *; }
-keep class com.net.jianjia.gson.** { *; }
-keep class com.net.jianjia.conventer.** { *; }
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @com.net.jianjia.http.* <methods>;
}

# OkHttp3
-dontwarn okhttp3.logging.**
-keep class okhttp3.internal.**{*;}
-dontwarn okio.**

# gson
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# 保留配置文件
-printmapping mapping.txt