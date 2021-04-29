package edu.aku.hassannaqvi.tpvics_hhlisting_app.contracts

import android.database.Cursor
import org.apache.commons.lang3.StringUtils
import org.json.JSONException
import org.json.JSONObject

class Districts {

    var dist_id: String = StringUtils.EMPTY
    var district: String = StringUtils.EMPTY
    var province: String = StringUtils.EMPTY
    var uc_name: String = StringUtils.EMPTY
    var uc_id: String = StringUtils.EMPTY

    @Throws(JSONException::class)
    fun sync(jsonObject: JSONObject): Districts {
        dist_id = jsonObject.getString(DistrictTable.COLUMN_DIST_ID)
        district = jsonObject.getString(DistrictTable.COLUMN_DIST_NAME)
        province = jsonObject.getString(DistrictTable.COLUMN_PROVINCE_NAME)
        uc_name = jsonObject.getString(DistrictTable.COLUMN_UC_NAME)
        uc_id = jsonObject.getString(DistrictTable.COLUMN_UC_ID)
        return this
    }

    fun hydrate(cursor: Cursor): Districts {
        dist_id = cursor.getString(cursor.getColumnIndex(DistrictTable.COLUMN_DIST_ID))
        district = cursor.getString(cursor.getColumnIndex(DistrictTable.COLUMN_DIST_NAME))
        province = cursor.getString(cursor.getColumnIndex(DistrictTable.COLUMN_PROVINCE_NAME))
        uc_name = cursor.getString(cursor.getColumnIndex(DistrictTable.COLUMN_UC_NAME))
        uc_id = cursor.getString(cursor.getColumnIndex(DistrictTable.COLUMN_UC_ID))
        return this
    }

    object DistrictTable {
        const val TABLE_NAME = "districts"
        const val COLUMN_ID = "_id"
        const val COLUMN_DIST_ID = "dist_id"
        const val COLUMN_DIST_NAME = "district"
        const val COLUMN_PROVINCE_NAME = "province"
        const val COLUMN_UC_NAME = "uc_name"
        const val COLUMN_UC_ID = "uc_id"
    }

}