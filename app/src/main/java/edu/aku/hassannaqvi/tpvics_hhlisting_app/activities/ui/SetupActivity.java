package edu.aku.hassannaqvi.tpvics_hhlisting_app.activities.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.validatorcrawler.aliazaz.Clear;
import com.validatorcrawler.aliazaz.Validator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import edu.aku.hassannaqvi.tpvics_hhlisting_app.R;
import edu.aku.hassannaqvi.tpvics_hhlisting_app.activities.home.LoginActivity;
import edu.aku.hassannaqvi.tpvics_hhlisting_app.activities.home.MainActivity;
import edu.aku.hassannaqvi.tpvics_hhlisting_app.contracts.ListingContract;
import edu.aku.hassannaqvi.tpvics_hhlisting_app.core.DatabaseHelper;
import edu.aku.hassannaqvi.tpvics_hhlisting_app.core.MainApp;
import edu.aku.hassannaqvi.tpvics_hhlisting_app.databinding.ActivitySetupBinding;

import static edu.aku.hassannaqvi.tpvics_hhlisting_app.core.MainApp.lc;

public class SetupActivity extends Activity {
    private static final String TAG = "Setup Activity";
    private ActivitySetupBinding bi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bi = DataBindingUtil.setContentView(this, R.layout.activity_setup);
        bi.setCallback(this);
        this.setTitle("Structure Information");

        bi.hh02.setText(MainApp.clusterCode);
        bi.hh02.setEnabled(false);

        if (MainApp.hh02txt == null) {
            MainApp.hh03txt = 1;
        } else {
            MainApp.hh03txt++;
            bi.hh02.setText(MainApp.clusterCode);
            bi.hh02.setEnabled(false);
        }
        MainApp.hh07txt = "1";
        String StructureNumber = MainApp.tabCheck + "-" + String.format("%04d", MainApp.hh03txt);
        bi.hh03.setTextColor(Color.RED);
        bi.hh03.setText(StructureNumber);
        bi.hh07.setText(new StringBuilder(getString(R.string.hh07)).append(":").append(MainApp.hh07txt));

        bi.hh04.setOnCheckedChangeListener((group, checkedId) -> {

            if (bi.hh04a.isChecked()) {
                //Moved to Add next Family button: MainApp.hh07txt = String.valueOf((char) MainApp.hh07txt.charAt(0) + 1);
                MainApp.hh07txt = "1";
            } else {
                MainApp.hh07txt = "";
            }

            if (bi.hh04h.isChecked() || bi.hh04i.isChecked()) {
                Clear.clearAllFields(bi.fldGrpHH12);
                bi.fldGrpHH12.setVisibility(View.GONE);
                bi.btnNextStructure.setVisibility(View.GONE);
                bi.btnChangePSU.setVisibility(View.VISIBLE);
                if (bi.hh04h.isChecked()) {
                    bi.btnChangePSU.setText(R.string.logout);
                } else {
                    bi.btnChangePSU.setText(R.string.change_enumeration_block);
                }
            } else {
                bi.fldGrpHH12.setVisibility(View.VISIBLE);
                bi.btnChangePSU.setVisibility(View.GONE);
            }
        });

        bi.hh14.setOnCheckedChangeListener((group, checkedId) -> {

            MainApp.hh07txt = "1";

            bi.hh07.setText(new StringBuilder(getString(R.string.hh07)).append(":").append(MainApp.hh07txt));
            if (bi.hh14a.isChecked()) {
                bi.fldGrpHH04.setVisibility(View.VISIBLE);
                bi.btnNextStructure.setVisibility(View.GONE);
            } else {
                Clear.clearAllFields(bi.fldGrpHH04);
                bi.fldGrpHH04.setVisibility(View.GONE);
                bi.hh05.setChecked(false);
                bi.hh06.setText(null);
                bi.btnNextStructure.setVisibility(View.VISIBLE);
            }
        });

        bi.hh05.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                MainApp.hh07txt = "1";
                bi.hh07.setText(String.format("%s: %s", getString(R.string.hh07), MainApp.hh07txt));
                bi.hh06.setVisibility(View.VISIBLE);
                bi.hh06.requestFocus();

            } else {
                MainApp.hh07txt = "1";
                bi.hh07.setText(String.format("%s: %s", getString(R.string.hh07), MainApp.hh07txt));
                bi.hh06.setVisibility(View.GONE);
                bi.hh06.setText(null);
            }
        });


    }

    private void SaveDraft() {

        lc = new ListingContract();
        SharedPreferences sharedPref = getSharedPreferences("tagName", MODE_PRIVATE);
        lc.setTagId(sharedPref.getString("tagName", null));
        lc.setAppVer(MainApp.versionName + "." + MainApp.versionCode);
        lc.setHhDT(new SimpleDateFormat("dd-MM-yy HH:mm:ss").format(new Date().getTime()));
        lc.setEnumCode(MainApp.enumCode);
        lc.setClusterCode(MainApp.clusterCode);
        lc.setEnumStr(MainApp.enumStr);
        lc.setHh01(String.valueOf(MainApp.hh01txt));
        lc.setHh02(MainApp.hh02txt);
        lc.setHh03(String.valueOf(MainApp.hh03txt));
        lc.setHh04(bi.hh04a.isChecked() ? "1" :
                bi.hh04b.isChecked() ? "2" :
                        bi.hh04c.isChecked() ? "3" :
                                bi.hh04d.isChecked() ? "4" :
                                        bi.hh04e.isChecked() ? "5" :
                                                bi.hh04f.isChecked() ? "6" :
                                                        bi.hh04h.isChecked() ? "8" :
                                                                bi.hh04i.isChecked() ? "9" :
                                                                        bi.hh0496.isChecked() ? "96" :
                                                                                "0");
        lc.setUsername(MainApp.userEmail);
        lc.setHh05(bi.hh05.isChecked() ? "1" : "2");
        lc.setHh06(Objects.requireNonNull(bi.hh06.getText()).toString());
        lc.setHh07(MainApp.hh07txt);
        lc.setHh09a1(bi.hh04a.isChecked() ? "1" : "2");
        lc.setDeviceID(Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));
        lc.setIsRandom(MainApp.tabCheck);
        lc.setHh08a1(bi.hh14a.isChecked() ? "1" : bi.hh14b.isChecked() ? "2" : "0");
        setGPS();
        MainApp.fTotal = bi.hh06.getText().toString().isEmpty() ? 0 : Integer.parseInt(bi.hh06.getText().toString());
        Log.d(TAG, "SaveDraft: " + lc.getHh03());
    }

    public void setGPS() {
        SharedPreferences GPSPref = getSharedPreferences("GPSCoordinates", Context.MODE_PRIVATE);
//        String date = DateFormat.format("dd-MM-yyyy HH:mm", Long.parseLong(GPSPref.getString("Time", "0"))).toString();
        try {
            String lat = GPSPref.getString("Latitude", "0");
            String lang = GPSPref.getString("Longitude", "0");
            String acc = GPSPref.getString("Accuracy", "0");
            String dt = GPSPref.getString("Time", "0");
            if (lat.equals("0") && lang.equals("0")) {
                Toast.makeText(this, "Could not obtained GPS points", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "GPS set", Toast.LENGTH_SHORT).show();
            }
            String date = DateFormat.format("dd-MM-yyyy HH:mm", Long.parseLong(GPSPref.getString("Time", "0"))).toString();
            lc.setGPSLat(GPSPref.getString("Latitude", "0"));
            lc.setGPSLng(GPSPref.getString("Longitude", "0"));
            lc.setGPSAcc(GPSPref.getString("Accuracy", "0"));
            lc.setGPSAlt(GPSPref.getString("Altitude", "0"));
//            MainApp.fc.setGpsTime(GPSPref.getString(date, "0")); // Timestamp is converted to date above
            lc.setGPSTime(date); // Timestamp is converted to date above
            Toast.makeText(this, "GPS set", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "setGPS: " + e.getMessage());
        }

    }

    private boolean formValidation() {
        return Validator.emptyCheckingContainer(this, bi.fldGrpSecA01);
    }

    private boolean updateDB() {
        DatabaseHelper db = new DatabaseHelper(this);
        Log.d(TAG, "UpdateDB: Structure" + lc.getHh03());

        long updcount = db.addForm(lc);

        lc.setID(String.valueOf(updcount));

        if (updcount != 0) {


            lc.setUID(
                    (lc.getDeviceID() + lc.getID()));

            db.updateListingUID();

        } else {
            Toast.makeText(this, "Updating Database... ERROR!", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    public void onBtnAddHHClick() {

        if (MainApp.hh02txt == null) {
            MainApp.hh02txt = bi.hh02.getText().toString();
        }
        if (formValidation()) {
            SaveDraft();
            MainApp.fCount++;
            finish();
            Intent fA = new Intent(this, FamilyListingActivity.class);
            startActivity(fA);
        }

    }

    public void onBtnChangePSUClick() {

        finish();

        Intent fA;
        if (bi.hh04h.isChecked()) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            SaveDraft();

            if (updateDB()) {
                MainApp.hh02txt = null;

                fA = new Intent(this, MainActivity.class);
                startActivity(fA);
            }
        }

    }

    public void onBtnNextStructureClick() {
        if (MainApp.hh02txt == null) {
            MainApp.hh02txt = bi.hh02.getText().toString();
        }
        if (formValidation()) {

            SaveDraft();
            if (updateDB()) {
                MainApp.fCount = 0;
                MainApp.fTotal = 0;
                MainApp.cCount = 0;
                MainApp.cTotal = 0;
                finish();
                Intent fA = new Intent(this, SetupActivity.class);
                startActivity(fA);

            }
        }
    }
}


