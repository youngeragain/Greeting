package xcj.app.greeting

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import xcj.app.greeting.components.Greeting.greeting
import com.example.greeting.R
import java.util.*


class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<AppCompatTextView>(R.id.btn)?.setOnClickListener {
            val stringBuilder = StringBuilder()
            mapOf(
                android.Manifest.permission.WRITE_SETTINGS to "需要修改亮度以保持内容清晰",
                android.Manifest.permission.MANAGE_EXTERNAL_STORAGE to "有些文件需要放到外部存储空间但是又不能放到特定的位置",
                android.Manifest.permission.SYSTEM_ALERT_WINDOW to "提供类似画中画的效果",
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE to "I just want this permission!",
                android.Manifest.permission.READ_EXTERNAL_STORAGE to "I just want this permission!",
                android.Manifest.permission.RECORD_AUDIO to "I just want this permission!",
            ).greeting(
                this,
                granted = {permissions->
                    if(permissions.size==6){
                        Toast.makeText(this, "all the permissions are granted!", Toast.LENGTH_SHORT).show()
                    }
                    stringBuilder.append("granted permissions:\n")
                    permissions.forEach {permission->
                        stringBuilder.append(permission).append("\n")
                    }

                },
                denied = {permissions->
                    stringBuilder.append("denied permissions:\n")
                    permissions.forEach {permission->
                        stringBuilder.append(permission).append("\n")
                    }
                    findViewById<AppCompatTextView>(R.id.btn)?.text = stringBuilder.toString()
                }
            )
        }
    }

}