# Keep Room entities/daos referenced by reflection
-keep class com.ascendy.app.data.** { *; }

# zxing-android-embedded reflectively instantiates the capture activity and
# decode classes; zxing core uses enum/class lookups for barcode formats.
-keep class com.journeyapps.barcodescanner.** { *; }
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# Keep line numbers for readable release crash traces (no file names leaked)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
