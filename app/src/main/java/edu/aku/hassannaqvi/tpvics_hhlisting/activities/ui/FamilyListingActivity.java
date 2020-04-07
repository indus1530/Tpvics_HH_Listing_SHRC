package edu.aku.hassannaqvi.tpvics_hhlisting.activities.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.validatorcrawler.aliazaz.Clear;
import com.validatorcrawler.aliazaz.Validator;

import edu.aku.hassannaqvi.tpvics_hhlisting.R;
import edu.aku.hassannaqvi.tpvics_hhlisting.core.DatabaseHelper;
import edu.aku.hassannaqvi.tpvics_hhlisting.core.MainApp;
import edu.aku.hassannaqvi.tpvics_hhlisting.databinding.ActivityFamilyListingBinding;

public class FamilyListingActivity extends AppCompatActivity {

    public static String TAG = "FamilyListingActivity";
    static Boolean familyFlag = false;
    ActivityFamilyListingBinding bi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        bi = ActivityFamilyListingBinding.inflate(getLayoutInflater());
        bi = DataBindingUtil.setContentView(this, R.layout.activity_family_listing);
        bi.setCallback(this);
        this.setTitle("Family Information");
        bi.txtTeamNoWithFam.setText(MainApp.tabCheck + "-" + String.format("%04d", MainApp.hh03txt) + "-" + String.format("%03d", Integer.valueOf(MainApp.hh07txt)));

        setupButtons();

        bi.hh17.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                bi.btnAddNewHousehold.setVisibility(View.VISIBLE);
                bi.btnAddHousehold.setVisibility(View.GONE);
                if (MainApp.hh07txt.equals("1")) {
                    MainApp.hh07txt = "1";
                }
            } else {
                bi.btnAddNewHousehold.setVisibility(View.GONE);
                setupButtons();
                if (MainApp.fTotal == 0) {
                    if (MainApp.hh07txt.equals("1")) {
                        MainApp.hh07txt = "1";
                    }
                }
            }
            bi.txtTeamNoWithFam.setText(MainApp.tabCheck + "-S" + String.format("%04d", MainApp.hh03txt) + "-H" + String.format("%03d", Integer.valueOf(MainApp.hh07txt)));
        });

        bi.deleteHH.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b)
                Clear.clearAllFields(bi.fldGrpSecB01, false);
            else
                Clear.clearAllFields(bi.fldGrpSecB01, true);
        });

    }

    public void setupButtons() {
        if (MainApp.fCount < MainApp.fTotal) {
            bi.btnAddFamily.setVisibility(View.VISIBLE);
            bi.btnAddHousehold.setVisibility(View.GONE);
            bi.hh17.setVisibility(View.GONE);
        } else {
            bi.btnAddFamily.setVisibility(View.GONE);
            bi.btnAddHousehold.setVisibility(View.VISIBLE);
            bi.hh17.setVisibility(View.VISIBLE);
            bi.deleteHH.setVisibility(View.VISIBLE);
        }
    }

    private void SaveDraft() {

        MainApp.lc.setHh07(MainApp.hh07txt);
        MainApp.lc.setHh08a1("1");
        MainApp.lc.setHh08(bi.hh08.getText().toString());
        MainApp.lc.setHh09(bi.hh09.getText().toString());
        MainApp.lc.setHh10(bi.hh10a.isChecked() ? "1" : bi.hh10b.isChecked() ? "2" : "0");
        MainApp.lc.setHh11(bi.hh11.getText().toString().isEmpty() ? "0" : bi.hh11.getText().toString());
        MainApp.lc.setHh14(bi.hh16.getText().toString());
        MainApp.lc.setHh15(bi.deleteHH.isChecked() ? "1" : "0");
        MainApp.lc.setIsNewHH(bi.hh17.isChecked() ? "1" : "2");

        Log.d(TAG, "SaveDraft: Structure " + MainApp.lc.getHh03());

    }

    private boolean formValidation() {
        return Validator.emptyCheckingContainer(this, bi.activityHouseholdListing);
    }

    private boolean UpdateDB() {
        DatabaseHelper db = new DatabaseHelper(this);
        long updcount = db.addForm(MainApp.lc);
        MainApp.lc.setID(String.valueOf(updcount));
        if (updcount != 0) {
            MainApp.lc.setUID((MainApp.lc.getDeviceID() + MainApp.lc.getID()));
            db.updateListingUID();
        } else {
            Toast.makeText(this, "Updating Database... ERROR!", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    public void onBtnAddNewHouseHoldClick() {

        if (formValidation()) {

            SaveDraft();
            if (UpdateDB()) {
                if (familyFlag)
                    MainApp.hh07txt = String.valueOf(Integer.parseInt(MainApp.hh07txt) + 1);
                else {

                    MainApp.hh07txt = String.valueOf(Integer.parseInt(MainApp.hh07txt) + 1);

                    familyFlag = true;
                }
                MainApp.lc.setHh07(MainApp.hh07txt);
                MainApp.fCount++;

                finish();
                Intent fA = new Intent(this, FamilyListingActivity.class);
                startActivity(fA);
            }
        }

    }

    public void onBtnAddFamilyClick() {
        if (formValidation()) {

            SaveDraft();
            if (UpdateDB()) {
                MainApp.hh07txt = String.valueOf(Integer.parseInt(MainApp.hh07txt) + 1);
                MainApp.lc.setHh07(MainApp.hh07txt);
                MainApp.fCount++;

                finish();
                Intent fA = new Intent(this, FamilyListingActivity.class);
                startActivity(fA);
            }

        }

    }

    public void onBtnAddHouseholdClick() {
        if (formValidation()) {

            SaveDraft();
            if (UpdateDB()) {
                MainApp.fCount = 0;
                MainApp.fTotal = 0;
                MainApp.cCount = 0;
                MainApp.cTotal = 0;
                familyFlag = false;
                finish();
                Intent fA = new Intent(this, SetupActivity.class);
                startActivity(fA);
            }
        }

    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(), "Back Button NOT Allowed!", Toast.LENGTH_SHORT).show();

    }
}
