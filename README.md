# Greeting
Use Kotlin's extension methods to help deal with Android runtime permissions and special permissions
<img src="https://i.loli.net/2021/07/31/G6p8UiFcZKwrgBe.png" width="303" height="480"/>
<br>
Usage:
<br>
Use extension methods or other methods, as shown below:
```kotlin
  mapOf(
    android.Manifest.permission.WRITE_SETTINGS to "We need to modify the brightness",
    android.Manifest.permission.MANAGE_EXTERNAL_STORAGE to "Some files need to be placed in external storage space but cannot be placed in a specific location",
    android.Manifest.permission.SYSTEM_ALERT_WINDOW to "Provide you with a picture-in-picture effect",
    android.Manifest.permission.WRITE_EXTERNAL_STORAGE to "Reason",
    android.Manifest.permission.READ_EXTERNAL_STORAGE to "Reason",
    android.Manifest.permission.RECORD_AUDIO to "Reason"
  ).greeting(context = any,
    granted = { permissions->
        
    },
    denied = { permissions->
        
    })
```
more in Greeting.kt file
