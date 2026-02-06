# Add project specific ProGuard rules here
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Preserve SQLite database classes
-keep class android.database.sqlite.** { *; }

# Preserve Kotlin metadata and reflection
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# Preserve AndroidX classes
-keep class androidx.** { *; }
-dontwarn androidx.**

# Preserve Material Design classes
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# Preserve RecyclerView classes
-keep class androidx.recyclerview.** { *; }
-dontwarn androidx.recyclerview.**

# Preserve ConstraintLayout classes
-keep class androidx.constraintlayout.** { *; }
-dontwarn androidx.constraintlayout.**

# Preserve all application classes
-keep class moji.deliverytracker.** { *; }

# Preserve Order class and its properties
-keep class moji.deliverytracker.Order { *; }

# Preserve DatabaseHelper and its methods
-keep class moji.deliverytracker.DatabaseHelper { *; }

# Preserve all Activity classes
-keep class * extends androidx.appcompat.app.AppCompatActivity { *; }

# Preserve logging
-keep class android.util.Log { *; }

# Preserve ContentValues
-keep class android.content.ContentValues { *; }

# Preserve SimpleDateFormat and related classes
-keep class java.text.SimpleDateFormat { *; }
-keep class java.util.Locale { *; }
-keep class java.util.Date { *; }

# Preserve Intent and Bundle classes
-keep class android.content.Intent { *; }
-keep class android.os.Bundle { *; }

# Preserve Toast
-keep class android.widget.Toast { *; }

# Preserve ArrayAdapter and AutoCompleteTextView
-keep class android.widget.ArrayAdapter { *; }
-keep class android.widget.AutoCompleteTextView { *; }

# Preserve EditText and Button
-keep class android.widget.EditText { *; }
-keep class android.widget.Button { *; }

# Preserve TextView
-keep class android.widget.TextView { *; }

# Preserve RecyclerView and LinearLayoutManager
-keep class androidx.recyclerview.widget.RecyclerView { *; }
-keep class androidx.recyclerview.widget.LinearLayoutManager { *; }

# Preserve ViewHolder pattern
-keep class * extends androidx.recyclerview.widget.RecyclerView$ViewHolder { *; }

# Preserve Adapter pattern
-keep class * extends androidx.recyclerview.widget.RecyclerView$Adapter { *; }