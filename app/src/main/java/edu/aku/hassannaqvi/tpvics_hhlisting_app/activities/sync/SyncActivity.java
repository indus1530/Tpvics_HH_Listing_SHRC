package edu.aku.hassannaqvi.tpvics_hhlisting_app.activities.sync;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.aku.hassannaqvi.tpvics_hhlisting_app.CONSTANTS;
import edu.aku.hassannaqvi.tpvics_hhlisting_app.R;
import edu.aku.hassannaqvi.tpvics_hhlisting_app.adapters.SyncListAdapter;
import edu.aku.hassannaqvi.tpvics_hhlisting_app.contracts.DistrictContract;
import edu.aku.hassannaqvi.tpvics_hhlisting_app.contracts.EnumBlockContract;
import edu.aku.hassannaqvi.tpvics_hhlisting_app.contracts.ListingContract;
import edu.aku.hassannaqvi.tpvics_hhlisting_app.contracts.UsersContract;
import edu.aku.hassannaqvi.tpvics_hhlisting_app.contracts.VersionAppContract;
import edu.aku.hassannaqvi.tpvics_hhlisting_app.core.DatabaseHelper;
import edu.aku.hassannaqvi.tpvics_hhlisting_app.core.MainApp;
import edu.aku.hassannaqvi.tpvics_hhlisting_app.databinding.ActivitySyncBinding;
import edu.aku.hassannaqvi.tpvics_hhlisting_app.otherClasses.SyncModel;
import edu.aku.hassannaqvi.tpvics_hhlisting_app.workers.DataDownWorkerALL;
import edu.aku.hassannaqvi.tpvics_hhlisting_app.workers.DataUpWorkerALL;
import timber.log.Timber;

import static edu.aku.hassannaqvi.tpvics_hhlisting_app.repository.UtilsKt.dbBackup;
import static edu.aku.hassannaqvi.tpvics_hhlisting_app.utils.AndroidUtilityKt.isNetworkConnected;

public class SyncActivity extends AppCompatActivity {
    private static final String TAG = "SyncActivity";
    private DatabaseHelper db;
    private SyncListAdapter syncListAdapter;
    private ActivitySyncBinding bi;
    private List<SyncModel> uploadTables;
    private List<SyncModel> downloadTables;
    private String distCode;
    private int totalFiles;
    private long tStart;
    private String progress;
    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bi = DataBindingUtil.setContentView(this, R.layout.activity_sync);
        bi.setCallback(this);
        db = new DatabaseHelper(this);
        uploadTables = new ArrayList<>();
        downloadTables = new ArrayList<>();
        MainApp.uploadData = new ArrayList<>();
        bi.noDataItem.setVisibility(View.VISIBLE);

        db = new DatabaseHelper(this);
        dbBackup(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_OK);
        finish();
    }


    @SuppressLint("NonConstantResourceId")
    public void ProcessStart(View view) {

        if (!isNetworkConnected(this))
            return;

        switch (view.getId()) {

            case R.id.btnUpload:
                bi.dataLayout.setVisibility(View.VISIBLE);
                bi.photoLayout.setVisibility(View.GONE);
                bi.mTextViewS.setVisibility(View.GONE);
                bi.pBar.setVisibility(View.GONE);
                uploadTables.clear();
                MainApp.uploadData.clear();

                // Forms
                uploadTables.add(new SyncModel(ListingContract.ListingEntry.TABLE_NAME.toLowerCase()));
                MainApp.uploadData.add(db.getUnsyncedListing());

                setAdapter(uploadTables);
                uploadData();
                break;
            case R.id.btnSync:
                MainApp.downloadData = new String[0];
                bi.dataLayout.setVisibility(View.VISIBLE);
                bi.photoLayout.setVisibility(View.GONE);
                bi.mTextViewS.setVisibility(View.GONE);
                bi.pBar.setVisibility(View.GONE);
                downloadTables.clear();
                boolean sync_flag = getIntent().getBooleanExtra(CONSTANTS.SYNC_LOGIN, false);
                if (sync_flag) {
                    distCode = getIntent().getStringExtra(CONSTANTS.SYNC_DISTRICTID_LOGIN);
                    downloadTables.add(new SyncModel(EnumBlockContract.EnumBlockTable.TABLE_NAME.toLowerCase()));
                } else {
                    // Set tables to DOWNLOAD
                    downloadTables.add(new SyncModel(UsersContract.UsersTable.TABLE_NAME.toLowerCase()));
                    downloadTables.add(new SyncModel(VersionAppContract.VersionAppTable.TABLE_NAME));
                    downloadTables.add(new SyncModel(DistrictContract.DistrictTable.TABLE_NAME.toLowerCase()));
                }
                MainApp.downloadData = new String[downloadTables.size()];
                setAdapter(downloadTables);
                beginDownload();
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }
    }

    private void beginDownload() {
        List<OneTimeWorkRequest> workRequests = new ArrayList<>();
        for (int i = 0; i < downloadTables.size(); i++) {
            Data.Builder data = new Data.Builder()
                    .putString("table", downloadTables.get(i).gettableName())
                    .putInt("position", i)
                    //.putString("columns", "_id, sysdate")
                    // .putString("where", where)
                    ;
            if (downloadTables.get(i).gettableName().equals(EnumBlockContract.EnumBlockTable.TABLE_NAME)) {
                data.putString("where", EnumBlockContract.EnumBlockTable.COLUMN_DIST_ID + "='" + distCode + "'");
            }
            workRequests.add(new OneTimeWorkRequest.Builder(DataDownWorkerALL.class)
                    .addTag(String.valueOf(i))
                    .setInputData(data.build()).build());
        }

        // FOR SIMULTANEOUS WORKREQUESTS (ALL TABLES DOWNLOAD AT THE SAME TIME)
        WorkManager wm = WorkManager.getInstance();
        WorkContinuation wc = wm.beginWith(workRequests);
        wc.enqueue();

        wc.getWorkInfosLiveData().observe(this, workInfos -> {

//            SharedStorage.INSTANCE.setLastDataDownload(this, new SimpleDateFormat("dd-MM-yy", Locale.ENGLISH).format(new Date()));

            Timber.tag(TAG).d("workInfos: %s", workInfos.size());
            for (WorkInfo workInfo : workInfos) {
                Timber.tag(TAG).d("workInfo: getState %s", workInfo.getState());
                Timber.tag(TAG).d("workInfo: data %s", workInfo.getOutputData().getString("data"));
                Timber.tag(TAG).d("workInfo: error %s", workInfo.getOutputData().getString("error"));
                Timber.tag(TAG).d("workInfo: position %s", workInfo.getOutputData().getInt("position", 0));
            }

            for (WorkInfo workInfo : workInfos) {
                int position = workInfo.getOutputData().getInt("position", 0);
                String tableName = downloadTables.get(position).gettableName();

                if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {

                    String result = MainApp.downloadData[position];
                    //Do something with the JSON string
                    if (result != null) {
                        if (result.length() > 0) {
                            DatabaseHelper db = new DatabaseHelper(SyncActivity.this);
                            try {
                                JSONArray jsonArray = new JSONArray();
                                int insertCount = 0;
                                switch (tableName) {
                                    case UsersContract.UsersTable.TABLE_NAME:
                                        jsonArray = new JSONArray(result);
                                        insertCount = db.syncUser(jsonArray);
                                        break;
                                    case VersionAppContract.VersionAppTable.TABLE_NAME:
                                        insertCount = db.syncVersionApp(new JSONObject(result));
                                        if (insertCount == 1) jsonArray.put("1");
                                        break;
                                    case EnumBlockContract.EnumBlockTable.TABLE_NAME:
                                        jsonArray = new JSONArray(result);
                                        insertCount = db.syncEnumBlocks(jsonArray);
                                        break;
                                    case DistrictContract.DistrictTable.TABLE_NAME:
                                        jsonArray = new JSONArray(result);
                                        insertCount = db.syncDistrict(jsonArray);
                                        break;
                                }

                                downloadTables.get(position).setmessage("Received: " + jsonArray.length() + ", Saved: " + insertCount);
                                downloadTables.get(position).setstatus(insertCount == 0 ? "Unsuccessful" : "Successful");
                                downloadTables.get(position).setstatusID(insertCount == 0 ? 1 : 3);
                                syncListAdapter.updatesyncList(downloadTables);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                downloadTables.get(position).setstatus("Process Failed");
                                downloadTables.get(position).setstatusID(1);
                                downloadTables.get(position).setmessage(result);
                                syncListAdapter.updatesyncList(downloadTables);
                            }
                        } else {
                            downloadTables.get(position).setmessage("Received: " + result.length() + "");
                            downloadTables.get(position).setstatus("Successful");
                            downloadTables.get(position).setstatusID(3);
                            syncListAdapter.updatesyncList(downloadTables);
                        }
                    } else {
                        downloadTables.get(position).setstatus("Process Failed");
                        downloadTables.get(position).setstatusID(1);
                        downloadTables.get(position).setmessage("Server not found!");
                        syncListAdapter.updatesyncList(downloadTables);
                    }
                }
                if (workInfo.getState() == WorkInfo.State.FAILED) {
                    String message = workInfo.getOutputData().getString("error");
                    downloadTables.get(position).setstatus("Process Failed");
                    downloadTables.get(position).setstatusID(1);
                    downloadTables.get(position).setmessage(message);
                    syncListAdapter.updatesyncList(downloadTables);

                }
            }
        });
    }

    private void setAdapter(List<SyncModel> tables) {
        syncListAdapter = new SyncListAdapter(tables);
        RecyclerView.LayoutManager mLayoutManager2 = new LinearLayoutManager(getApplicationContext());
        bi.rvUploadList.setLayoutManager(mLayoutManager2);
        bi.rvUploadList.setItemAnimator(new DefaultItemAnimator());
        bi.rvUploadList.setAdapter(syncListAdapter);
        syncListAdapter.notifyDataSetChanged();
        if (syncListAdapter.getItemCount() > 0) {
            bi.noDataItem.setVisibility(View.GONE);
        } else {
            bi.noDataItem.setVisibility(View.VISIBLE);
        }
    }

    private void uploadData() {
        List<OneTimeWorkRequest> workRequests = new ArrayList<>();
        for (int i = 0; i < uploadTables.size(); i++) {
            Data data = new Data.Builder()
                    .putString("table", uploadTables.get(i).gettableName())
                    .putInt("position", i)
                    //    .putString("data", uploadData.get(i).toString())
                    //.putString("columns", "_id, sysdate")
                    // .putString("where", where)
                    .build();
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(DataUpWorkerALL.class)
                    .addTag(String.valueOf(i))
                    .setInputData(data).build();
            workRequests.add(workRequest);

        }

        // FOR SIMULTANEOUS WORKREQUESTS (ALL TABLES DOWNLOAD AT THE SAME TIME)
        WorkManager wm = WorkManager.getInstance();
        WorkContinuation wc = wm.beginWith(workRequests);
        wc.enqueue();

        // FOR WORKREQUESTS CHAIN (ONE TABLE DOWNLOADS AT A TIME)
        wc.getWorkInfosLiveData().observe(this, workInfos -> {
//            SharedStorage.INSTANCE.setLastDataUpload(this, new SimpleDateFormat("dd-MM-yy", Locale.ENGLISH).format(new Date()));
            Timber.tag(TAG).d("workInfos: %s", workInfos.size());
            for (WorkInfo workInfo : workInfos) {
                Timber.tag(TAG).d("workInfo: getState %s", workInfo.getState());
                Timber.tag(TAG).d("workInfo: data %s", workInfo.getTags());
                Timber.tag(TAG).d("workInfo: data %s", workInfo.getOutputData().getString("message"));
                Timber.tag(TAG).d("workInfo: error %s", workInfo.getOutputData().getString("error"));
                Timber.tag(TAG).d("workInfo: position %s", workInfo.getOutputData().getInt("position", 0));
            }
            for (WorkInfo workInfo : workInfos) {
                int position = workInfo.getOutputData().getInt("position", 0);
                String tableName = uploadTables.get(position).gettableName();

                if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {

                    String result = workInfo.getOutputData().getString("message");

                    int sSynced = 0;
                    int sDuplicate = 0;
                    StringBuilder sSyncedError = new StringBuilder();
                    JSONArray json;

                    if (result != null) {
                        if (result.length() > 0) {
                            try {
                                Timber.tag(TAG).d("onPostExecute: %s", result);
                                json = new JSONArray(result);

                                Method method = null;
                                for (Method method1 : db.getClass().getDeclaredMethods()) {
                                    Timber.tag(TAG).d("onChanged Methods: %s", method1.getName());
                                    Timber.tag(TAG).d("onChanged Names: updateSynced%s", tableName);
                                    Timber.tag(TAG).d("onChanged Compare: %s", method1.getName().equals("updateSynced" + tableName));
                                    if (method1.getName().equals("updateSynced" + tableName)) {
                                        method = method1;
                                        Toast.makeText(SyncActivity.this, "updateSynced not found: updateSynced" + tableName, Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                }
                                if (method != null) {
                                    for (int i = 0; i < json.length(); i++) {
                                        JSONObject jsonObject = new JSONObject(json.getString(i));
                                        if (jsonObject.getString("status").equals("1") && jsonObject.getString("error").equals("0")) {
                                            method.invoke(db, jsonObject.getString("id"));
                                            sSynced++;
                                        } else if (jsonObject.getString("status").equals("2") && jsonObject.getString("error").equals("0")) {
                                            method.invoke(db, jsonObject.getString("id"));
                                            sDuplicate++;
                                        } else {
                                            sSyncedError.append("\nError: ").append(jsonObject.getString("message"));
                                        }
                                    }
                                    Toast.makeText(SyncActivity.this, tableName + " synced: " + sSynced + "\r\n\r\n Errors: " + sSyncedError, Toast.LENGTH_SHORT).show();

                                    if (sSyncedError.toString().equals("")) {
                                        uploadTables.get(position).setmessage(tableName + " synced: " + sSynced + "\r\n\r\n Duplicates: " + sDuplicate + "\r\n\r\n Errors: " + sSyncedError);
                                        uploadTables.get(position).setstatus("Completed");
                                        uploadTables.get(position).setstatusID(3);
                                        syncListAdapter.updatesyncList(uploadTables);
                                    } else {
                                        uploadTables.get(position).setmessage(tableName + " synced: " + sSynced + "\r\n\r\n Duplicates: " + sDuplicate + "\r\n\r\n Errors: " + sSyncedError);
                                        uploadTables.get(position).setstatus("Process Failed");
                                        uploadTables.get(position).setstatusID(1);
                                        syncListAdapter.updatesyncList(uploadTables);
                                    }
                                } else {
                                    uploadTables.get(position).setmessage("Method not found: updateSynced" + tableName);
                                    uploadTables.get(position).setstatus("Process Failed");
                                    uploadTables.get(position).setstatusID(1);
                                    syncListAdapter.updatesyncList(uploadTables);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(SyncActivity.this, "Sync Result:  " + result, Toast.LENGTH_SHORT).show();

                                if (result.equals("No new records to sync.")) {
                                    uploadTables.get(position).setmessage(result /*+ " Open Forms" + String.format("%02d", unclosedForms.size())*/);
                                    uploadTables.get(position).setstatus("Not processed");
                                    uploadTables.get(position).setstatusID(4);
                                    syncListAdapter.updatesyncList(uploadTables);
                                } else {
                                    uploadTables.get(position).setmessage(result);
                                    uploadTables.get(position).setstatus("Process Failed");
                                    uploadTables.get(position).setstatusID(1);
                                    syncListAdapter.updatesyncList(uploadTables);
                                }
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                                uploadTables.get(position).setstatus("Process Failed");
                                uploadTables.get(position).setstatusID(1);
                                uploadTables.get(position).setmessage(e.getMessage());
                                syncListAdapter.updatesyncList(uploadTables);
                            }
                        } else {
                            uploadTables.get(position).setmessage("Received: " + result.length() + "");
                            uploadTables.get(position).setstatus("Successful");
                            uploadTables.get(position).setstatusID(3);
                            syncListAdapter.updatesyncList(uploadTables);
                        }
                    } else {
                        uploadTables.get(position).setstatus("Process Failed");
                        uploadTables.get(position).setstatusID(1);
                        uploadTables.get(position).setmessage("Server not found!");
                        syncListAdapter.updatesyncList(uploadTables);
                    }
                }
                //mTextView1.append("\n" + workInfo.getState().name());
                if (workInfo.getState() == WorkInfo.State.FAILED) {
                    String message = workInfo.getOutputData().getString("error");
                    uploadTables.get(position).setstatus("Process Failed");
                    uploadTables.get(position).setstatusID(1);
                    uploadTables.get(position).setmessage(message);
                    syncListAdapter.updatesyncList(uploadTables);

                }
            }
        });

    }

    private void sortBySize(File[] files) {
        Arrays.sort(files, (t, t1) -> (int) (t.length() - t1.length()));
    }
}
