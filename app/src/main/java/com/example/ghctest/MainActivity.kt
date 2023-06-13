package com.example.ghctest

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.database.getStringOrNull
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var getContent: ActivityResultLauncher<Intent>
    val permissionList = arrayListOf(

        "android.permission.health.READ_ACTIVE_CALORIES_BURNED",
        "android.permission.health.READ_DISTANCE",
        "android.permission.health.READ_ELEVATION_GAINED",
        "android.permission.health.READ_EXERCISE",
        "android.permission.health.READ_FLOORS_CLIMBED",
        "android.permission.health.READ_STEPS",
        "android.permission.health.READ_TOTAL_CALORIES_BURNED",
        "android.permission.health.READ_VO2_MAX",
        "android.permission.health.READ_WHEELCHAIR_PUSHES",
        "android.permission.health.READ_POWER",
        "android.permission.health.READ_SPEED",
        "android.permission.health.READ_BASAL_METABOLIC_RATE",
        "android.permission.health.READ_BODY_FAT",
        "android.permission.health.READ_BODY_WATER_MASS",
        "android.permission.health.READ_BONE_MASS",
        "android.permission.health.READ_HEIGHT",
        "android.permission.health.READ_WEIGHT",
        "android.permission.health.READ_BASAL_BODY_TEMPERATURE",
        "android.permission.health.READ_BLOOD_GLUCOSE",
        "android.permission.health.READ_BLOOD_PRESSURE",
        "android.permission.health.READ_BODY_TEMPERATURE",
        "android.permission.health.READ_HEART_RATE",
        "android.permission.health.READ_HEART_RATE_VARIABILITY",
        "android.permission.health.READ_OXYGEN_SATURATION",
        "android.permission.health.READ_RESPIRATORY_RATE",
        "android.permission.health.READ_RESTING_HEART_RATE"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val PROVIDER_NAME = "com.example.healthconnect.codelab"

        // defining content URI
        val PERMISSION_URL = "content://$PROVIDER_NAME/permission"
        val DATA_URL = "content://$PROVIDER_NAME/data"
        val PERMISSION_URI = Uri.parse(PERMISSION_URL)
        val DATA_URI = Uri.parse(DATA_URL)
        getContent = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                activityResult.data?.getStringArrayExtra("grantedPermissions")?.forEach { permission ->
                    System.out.println("granted permission: $permission")
                }
                System.out.println("Permission granted")
                checkPermissionAndGetData(PERMISSION_URI, DATA_URI)
            } else {
                System.out.println("Permission not granted")
            }
        }
        findViewById<Button>(R.id.fetchghcbtn)
            .setOnClickListener {
                checkPermissionAndGetData(PERMISSION_URI, DATA_URI)
            }
    }
    fun getCalculatedDate(date: String, dateFormat: String, days: Int): String {
        val cal = Calendar.getInstance()
        val s = SimpleDateFormat(dateFormat)
        if (date.isNotEmpty()) {
            cal.time = s.parse(date)
        }
        cal.add(Calendar.DAY_OF_YEAR, days)
        return s.format(Date(cal.timeInMillis))
    }


    @SuppressLint("Range", "Recycle")
    fun checkPermissionAndGetData(permissionUri: Uri, dataUri: Uri) {
        val cursor = contentResolver.query(permissionUri, null, null, null, null);
        val listOfPermissions = arrayListOf<String>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val permission = cursor.getString(cursor.getColumnIndex("Permission"))
                listOfPermissions.add(permission)
            } while (cursor.moveToNext())
            cursor.close()
        }
        if (listOfPermissions.size > 0) {
            val dataCursor = contentResolver.query(dataUri, null, getCalculatedDate("","yyyy-MM-dd HH:mm:ss",-7),null, null)
            if (dataCursor != null) {
                for (column in dataCursor.columnNames) {
                        dataCursor.moveToFirst()
                        while (!dataCursor.isAfterLast) {
                            val value = dataCursor.getStringOrNull(dataCursor.getColumnIndexOrThrow(column))
                            if (value != null) println("$column : $value")
                            dataCursor.moveToNext()
                        }
                }
                dataCursor.close()
            }
        } else {
            val jdaIntent = packageManager.getLaunchIntentForPackage("com.example.healthconnect.codelab")
            jdaIntent?.flags = 0;
            jdaIntent?.putExtra("permissions", permissionList.toTypedArray())
            getContent.launch(jdaIntent)
        }
    }
}