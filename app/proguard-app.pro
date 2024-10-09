# 忽略警告
#-ignorewarning

# 混淆保护自己项目的部分代码以及引用的第三方jar包
#-libraryjars libs/xxxxxxxxx.jar

#############################################
#
# 对于一些基本指令的添加
#
#############################################

# 代码混淆压缩比，在0~7之间，默认为5，一般不做修改
-optimizationpasses 5

# 混合时不使用大小写混合，混合后的类名为小写
-dontusemixedcaseclassnames

# 保留Annotation不混淆
-keepattributes *Annotation*,InnerClasses

# 避免混淆泛型
-keepattributes Signature

# 抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable

# 指定混淆是采用的算法，后面的参数是一个过滤器
# 这个过滤器是谷歌推荐的算法，一般不做更改
-optimizations !code/simplification/cast,!field/*,!class/merging/*

# 保留我们自定义控件（继承自View）不被混淆
-keep class com.tt.funfun.widget.** {*;}

# 保留Parcelable序列化类不被混淆
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# 保留Serializable序列化的类不被混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 对于带有回调函数的onXXEvent、**On*Listener的，不能被混淆
-keepclassmembers class * {
    void *(**On*Event);
    void *(**On*Listener);
}

# 保留R下面的资源
-keep class **.R$* {*;}

# --------------- 保持哪些类不被混淆 Start ---------------
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends androidx.fragment.app.DialogFragment
-keep public class * extends androidx.appcompat.app.AppCompatActivity

#委托方式反射初始化 viewbind
-keep public class * extends androidx.viewbinding.ViewBinding { *; }

# 不混淆这个包下的类
-keep class com.tt.dramatime.http.api.** {
    <fields>;
}
-keep class com.tt.dramatime.http.bean.** {
     <fields>;
 }
-keep class com.tt.dramatime.http.model.** {
    <fields>;
}
-keep class com.tt.dramatime.util.eventbus.** {
    <fields>;
}

# 不混淆被 Log 注解的方法信息
-keepclassmembernames class ** {
    @com.tt.dramatime.aop.Log <methods>;
}

#编译提示添加 -dontwarn javax.lang.model.element.Modifier
-dontwarn javax.**
#编译提示添加 -dontwarn java.lang.reflect.AnnotatedType
-dontwarn java.lang.reflect.**

