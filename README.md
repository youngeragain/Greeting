# Greeting
Use Kotlin's extension method to help handle Android permissions
<br>
<img src="https://i.loli.net/2021/07/31/G6p8UiFcZKwrgBe.png" width="303" height="480"/>
<br>
Usage:
Call GreetingYou.putHolder(any:Any)  before using
```kotlin
GreetingYou.putHolder(this) 
```
and then use extension methods or other methods, as shown below:

```kotlin
1:
  mapOf(
    android.Manifest.permission.WRITE_SETTINGS to "We need to modify the brightness",
    android.Manifest.permission.MANAGE_EXTERNAL_STORAGE to "Some files need to be placed in external storage space but cannot be placed in a specific location",
    android.Manifest.permission.SYSTEM_ALERT_WINDOW to "Provide you with a picture-in-picture effect",
    android.Manifest.permission.WRITE_EXTERNAL_STORAGE to "I just want this permission!",
    android.Manifest.permission.READ_EXTERNAL_STORAGE to "I just want this permission!",
    android.Manifest.permission.RECORD_AUDIO to "I just want this permission!"
  ).greeting(
                granted = {permissions->
                    //your logic
                },
                denied = {permissions->
                    //your logic
                }
            )
 2:      
  (android.Manifest.permission.WRITE_SETTINGS to "We need to modify the brightness").greeting(
                granted = {permission->
                    //your logic
                },
                denied = {permission->
                    //your logic
                }
            )
 3:more in GreetingYou.kt file
```
