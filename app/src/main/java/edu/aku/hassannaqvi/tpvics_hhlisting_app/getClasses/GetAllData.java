package edu.aku.hassannaqvi.tpvics_hhlisting_app.getClasses;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import edu.aku.hassannaqvi.tpvics_hhlisting_app.adapters.SyncListAdapter;
import edu.aku.hassannaqvi.tpvics_hhlisting_app.contracts.VersionAppContract;
import edu.aku.hassannaqvi.tpvics_hhlisting_app.core.DatabaseHelper;
import edu.aku.hassannaqvi.tpvics_hhlisting_app.core.MainApp;
import edu.aku.hassannaqvi.tpvics_hhlisting_app.otherClasses.SyncModel;
import edu.aku.hassannaqvi.tpvics_hhlisting_app.utils.Keys;
import edu.aku.hassannaqvi.tpvics_hhlisting_app.utils.ServerSecurity;

/**
 * Created by ali.azaz on 7/14/2017.
 */

public class GetAllData extends AsyncTask<String, String, String> {

    private HttpURLConnection urlConnection;
    private SyncListAdapter adapter;
    private List<SyncModel> list;
    private int position;
    private String TAG = "";
    private Context mContext;
    private ProgressDialog pd;
    private String syncClass;


    public GetAllData(Context context, String syncClass, SyncListAdapter adapter, List<SyncModel> list) {
        mContext = context;
        this.syncClass = syncClass;
        this.adapter = adapter;
        this.list = list;
        TAG = "Get" + syncClass;
        switch (syncClass) {
            case "User":
                position = 0;
                break;
            case "VersionApp":
                position = 1;
                break;
            case "District":
                position = 2;
                break;
            case "EnumBlock":
                position = 0;
                break;
        }
        list.get(position).settableName(syncClass);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pd = new ProgressDialog(mContext);
        pd.setTitle("Syncing " + syncClass);
        pd.setMessage("Getting connected to server...");
        // pd.show();
        list.get(position).setstatus("Getting connected to server...");
        list.get(position).setstatusID(2);
        list.get(position).setmessage("");
        adapter.updatesyncList(list);

    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        switch (values[0]) {
            case "User":
                position = 0;
                break;
            case "VersionApp":
                position = 1;
                break;
            case "District":
                position = 2;
                break;
            case "EnumBlock":
                position = 0;
                break;
        }
        list.get(position).setstatus("Syncing");
        list.get(position).setstatusID(2);
        list.get(position).setmessage("");
        adapter.updatesyncList(list);
        pd.setMessage("Syncing");
        // pd.show();
    }

    @Override
    protected String doInBackground(String... args) {

        StringBuilder result = new StringBuilder();
        String table = "";

        URL url = null;
        try {
            switch (syncClass) {
                case "User":
                    url = new URL(MainApp._HOST_URL + MainApp._SERVER_GET_URL);
                    position = 0;
                    table = "users";
                    break;
                case "VersionApp":
                    url = new URL(MainApp._UPDATE_URL + VersionAppContract.VersionAppTable._URI);
                    position = 1;
                    break;
                case "District":
                    url = new URL(MainApp._HOST_URL + MainApp._SERVER_GET_URL);
                    position = 2;
                    table = "users";
                    break;
                case "EnumBlock":
                    url = new URL(MainApp._HOST_URL + MainApp._SERVER_GET_URL);
                    position = 0;
                    break;
            }

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(100000 /* milliseconds */);
            urlConnection.setConnectTimeout(150000 /* milliseconds */);

            switch (syncClass) {
                case "District":
                case "User":
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoOutput(true);
                    urlConnection.setDoInput(true);
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("charset", "utf-8");
                    urlConnection.setUseCaches(false);

                    // Starts the query
                    urlConnection.connect();
                    DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                    JSONObject json = new JSONObject();
                    try {
                        json.put("user", "test1234");
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                    Log.d(TAG, "downloadUrl: " + json.toString());
                    wr.writeBytes(ServerSecurity.INSTANCE.encrypt(String.valueOf(json), Keys.INSTANCE.apiKey()));
                    wr.flush();
                    wr.close();
                    break;


                case "EnumBlock":
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoOutput(true);
                    urlConnection.setDoInput(true);
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("charset", "utf-8");
                    urlConnection.setUseCaches(false);

                    // Starts the query
                    urlConnection.connect();
                    DataOutputStream wr2 = new DataOutputStream(urlConnection.getOutputStream());
                    JSONObject json2 = new JSONObject();
                    try {
                        json2.put("user", "test1234");
                        json2.put("dist_id", MainApp.UC_ID);
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                    Log.d(TAG, "downloadUrl: " + json2.toString());
                    wr2.writeBytes(ServerSecurity.INSTANCE.encrypt(String.valueOf(json2), Keys.INSTANCE.apiKey()));
                    wr2.flush();
                    wr2.close();
                    break;
            }


            Log.d(TAG, "doInBackground: " + urlConnection.getResponseCode());
            publishProgress(syncClass);
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                publishProgress("In Progress");

                String line;
                while ((line = reader.readLine()) != null) {
                    Log.i(TAG, syncClass + " In: " + line);
                    result.append(line);
                }
            }
        } catch (IOException e) {
            return null;
        } finally {
            urlConnection.disconnect();
        }
        return ServerSecurity.INSTANCE.decrypt(result.toString(), Keys.INSTANCE.apiKey());
    }

    @Override
    protected void onPostExecute(String result) {


        //Do something with the JSON string
        if (result != null) {
            if (result.length() > 0) {
                DatabaseHelper db = new DatabaseHelper(mContext);
                try {
                    JSONArray jsonArray = new JSONArray();
                    Log.d(TAG, "onPostExecute: " + syncClass);
                    int insertCount = 0;
                    switch (syncClass) {
                        case "User":
                            jsonArray = new JSONArray(result);
                            insertCount = db.syncUser(jsonArray);
                            position = 0;
                            break;
                        case "VersionApp":
                            insertCount = db.syncVersionApp(new JSONObject(result));
                            if (insertCount == 1) jsonArray.put("1");
                            position = 1;
                            break;
                        case "District":
                            jsonArray = new JSONArray(result);
                            insertCount = db.syncDistrict(jsonArray);
                            position = 2;
                            break;
                        case "EnumBlock":
                            jsonArray = new JSONArray(result);
                            insertCount = db.syncEnumBlocks(jsonArray);
                            position = 0;
                            break;

                    }

                    pd.setMessage("Received: " + jsonArray.length());
                    list.get(position).setmessage("Received: " + jsonArray.length() + ", Saved: " + insertCount);
                    list.get(position).setstatus(insertCount == 0 ? "Unsuccessful" : "Successful");
                    list.get(position).setstatusID(insertCount == 0 ? 2 : 3);
                    adapter.updatesyncList(list);
                    // pd.show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                pd.setMessage("Received: " + result.length() + "");
                list.get(position).setmessage("Received: " + result.length() + "");
                list.get(position).setstatus("Processed");
                list.get(position).setstatusID(4);
                adapter.updatesyncList(list);
                // pd.show();
            }
        } else {
            pd.setTitle("Connection Error");
            pd.setMessage("Server not found!");
            list.get(position).setstatus("Failed");
            list.get(position).setstatusID(1);
            list.get(position).setmessage("Server not found!");
            adapter.updatesyncList(list);
            // pd.show();
        }


    }

}
