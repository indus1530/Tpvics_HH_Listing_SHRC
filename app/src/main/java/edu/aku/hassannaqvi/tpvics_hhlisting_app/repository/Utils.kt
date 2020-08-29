package edu.aku.hassannaqvi.tpvics_hhlisting_app.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import edu.aku.hassannaqvi.tpvics_hhlisting_app.core.DatabaseHelper
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

fun dbBackup(context: Context) {
    val sharedPref = context.getSharedPreferences("listingHHTpvics", Context.MODE_PRIVATE)
    val editor = sharedPref.edit()
    if (sharedPref.getBoolean("flag", false)) {
        val dt: String = sharedPref.getString("dt", SimpleDateFormat("dd-MM-yy").format(Date())).toString()
        if (dt != SimpleDateFormat("dd-MM-yy").format(Date())) {
            editor.putString("dt", SimpleDateFormat("dd-MM-yy").format(Date()))
            editor.apply()
        }
        var folder = File(Environment.getExternalStorageDirectory().toString() + File.separator + DatabaseHelper.PROJECT_NAME)
        var success = true
        if (!folder.exists()) {
            success = folder.mkdirs()
        }
        if (success) {
            val directoryName = folder.path + File.separator + sharedPref.getString("dt", "")
            folder = File(directoryName)
            if (!folder.exists()) {
                success = folder.mkdirs()
            }
            if (success) {
                try {
                    val dbFile = File(context.getDatabasePath(DatabaseHelper.DATABASE_NAME).path)
                    val fis = FileInputStream(dbFile)
                    val outFileName: String = directoryName + File.separator + DatabaseHelper.DB_NAME
                    // Open the empty db as the output stream
                    val output: OutputStream = FileOutputStream(outFileName)

                    // Transfer bytes from the inputfile to the outputfile
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (fis.read(buffer).also { length = it } > 0) {
                        output.write(buffer, 0, length)
                    }
                    // Close the streams
                    output.flush()
                    output.close()
                    fis.close()
                } catch (e: IOException) {
                    e.message?.let { Log.e("dbBackup:", it) }
                }
            }
        } else {
            Toast.makeText(context, "Not create folder", Toast.LENGTH_SHORT).show()
        }
    }
}

private fun checkPermission(context: Context): IntArray {
    return intArrayOf(ContextCompat.checkSelfPermission(context,
            Manifest.permission.READ_CONTACTS), ContextCompat.checkSelfPermission(context,
            Manifest.permission.GET_ACCOUNTS), ContextCompat.checkSelfPermission(context,
            Manifest.permission.READ_PHONE_STATE), ContextCompat.checkSelfPermission(context,
            Manifest.permission.ACCESS_FINE_LOCATION), ContextCompat.checkSelfPermission(context,
            Manifest.permission.ACCESS_COARSE_LOCATION), ContextCompat.checkSelfPermission(context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE), ContextCompat.checkSelfPermission(context,
            Manifest.permission.CAMERA))
}

fun getPermissionsList(context: Context): List<String> {
    val permissions = arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA)
    val listPermissionsNeeded: MutableList<String> = ArrayList()
    for (i in checkPermission(context).indices) {
        if (checkPermission(context)[i] != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(permissions[i])
        }
    }
    return listPermissionsNeeded
}