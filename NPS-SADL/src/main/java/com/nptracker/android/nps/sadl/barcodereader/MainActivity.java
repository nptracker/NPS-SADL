/*
 * Copyright (C) 2018 Number Plate Systems (Pty) Ltd
 *
 *      Contact: info@nptracker.co.za
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nptracker.android.nps.sadl.barcodereader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONException;

/**
 * Main activity demonstrating how to pass extra parameters to an activity that
 * reads barcodes.
 */
public class MainActivity extends Activity implements View.OnClickListener {

    // use a compound button so either checkbox or switch widgets work.
    private CompoundButton autoFocus;
    private CompoundButton useFlash;
    private TextView statusMessage;
    private TextView barcodeValue;

    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusMessage = (TextView)findViewById(R.id.status_message);
        barcodeValue = (TextView)findViewById(R.id.barcode_value);

        autoFocus = (CompoundButton) findViewById(R.id.auto_focus);
        useFlash = (CompoundButton) findViewById(R.id.use_flash);
        findViewById(R.id.read_barcode).setOnClickListener(this);
        findViewById(R.id.btnSettings).setOnClickListener(this);
        findViewById(R.id.btnSource).setOnClickListener(this);
        findViewById(R.id.btnHome).setOnClickListener(this);
        findViewById(R.id.btnLIC).setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnSettings) {
            Intent intent = new Intent(this, MyPreferencesActivity.class);
            intent.putExtra("type", "Settings");
            startActivity(intent);
        }

        if (v.getId() == R.id.btnHome) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse("https://www.nptracker.co.za"));
            startActivity(intent);
        }

        if (v.getId() == R.id.btnLIC) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.nptracker.license.client.android&hl=en"));
            startActivity(intent);
        }

        if (v.getId() == R.id.btnSource) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse("https://github.com/nptracker/nps-sadl"));
            startActivity(intent);
        }

        if (v.getId() == R.id.read_barcode) {
            String token = PreferenceManager.getDefaultSharedPreferences(this).getString("NPS-API", "defaultValue");;
            Integer myNum;
            try {
                myNum = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("count", "0"));
            } catch(NumberFormatException nfe) {
                myNum = 0;
            }
            String count = String.valueOf(myNum + 1);

            if ((myNum > 5)&&(token.isEmpty() || token.contains("defaultValue"))) {
                Intent intent = new Intent(this, TokenActivity.class);
                startActivity(intent);
            }
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("count", count).apply();
            // launch barcode activity.
            Intent intent = new Intent(this, BarcodeCaptureActivity.class);
            intent.putExtra(BarcodeCaptureActivity.AutoFocus, autoFocus.isChecked());
            intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash.isChecked());
            startActivityForResult(intent, RC_BARCODE_CAPTURE);
        }

    }

    /**
     * Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional
     * data from it.  The <var>resultCode</var> will be
     * {@link #RESULT_CANCELED} if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     * <p/>
     * <p>You will receive this call immediately before onResume() when your
     * activity is re-starting.
     * <p/>
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     * @see #startActivityForResult
     * @see #createPendingResult
     * @see #setResult(int)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    String count = PreferenceManager.getDefaultSharedPreferences(this).getString("count", "0");
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    byte[] bytes = barcode.rawValue.getBytes();
                    StringBuilder sb = new StringBuilder(bytes.length * 2);
                    sb.append("<" + barcode.rawValue.toString().length() + "> ");
                    for(byte b: bytes)
                        sb.append(String.format("%02x ", b));
                    String type="";
                    String scan="";
                    String script="";
                    String tmp="";
//                    String cmd="";
//                    String base64="";
                    String tbytes = ""; // char codes
                    String tch="";
//                    Integer code=0;
                    Integer i=0;
                    String device = PreferenceManager.getDefaultSharedPreferences(this).getString("scannerID", "defaultValue");
                    String token = PreferenceManager.getDefaultSharedPreferences(this).getString("NPS-API", "defaultValue");
                    if (barcode.rawValue.toString().contains("RSA")) {
                        type="ID";
                        scan=barcode.rawValue.toString();
                    }
                    if (barcode.rawValue.toString().length() == 720) {  //SA Drivers licence is 720 bytes
                        type = "Drivers Licence";
                        for (i = 0; i < barcode.rawValue.toString().length(); ++i) {
                            tch = Integer.toHexString(barcode.rawValue.charAt(i)).length()==1
                                    ?"0"+Integer.toHexString(barcode.rawValue.charAt(i))
                                    :Integer.toHexString(barcode.rawValue.charAt(i));
                            tbytes = tbytes.concat(tch);
                        }
                        scan=tbytes;
                    }
                    if (barcode.rawValue.toString().contains("MVL")) {
                        type="Licence Disk";
                        scan=barcode.rawValue.toString();
                    }
                    if (barcode.rawValue.toString().contains("NPSCLOUD")) {
                        type = "Access Token";
                        tmp = barcode.rawValue.toString();
                        String[] parts = tmp.split("%");
                        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("NPS-API", parts[2]).apply();
                        Toast.makeText(getBaseContext(),"Access token updated.", Toast.LENGTH_LONG).show();
                    }
                    if (token.contains("defaultValue") || token.isEmpty()) {
                        Toast.makeText(getBaseContext(),"Saving and advance search DISABLED please register device at http://www.npscloud.co.za for FREE to enable all functions.",Toast.LENGTH_LONG).show();
                    }
//                    if(script != null && !script.isEmpty()){
                        if (type.equals("Licence Disk")){
                            int leng = scan.length();
                            try {
                                tmp = "http://lic.nptracker.co.za/API/" + script + "?tkn=" + URLEncoder.encode(token, "UTF-8") + "&cmd=lic" + "&str=" + URLEncoder.encode(scan, "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                Toast.makeText(getBaseContext(),"ERROR: Invalid character(s) in [scanner identifier] OR [Scanner access token] correct in Setting menu to continue.",Toast.LENGTH_LONG).show();
                            }
                            Toast.makeText(getBaseContext(),"Connecting to http://www.npscloud.co.za.",Toast.LENGTH_SHORT).show();
                            new DownloadWebpageTask(new AsyncResult() {
                                @Override
                                public void onResult(JSONObject object) {
                                    processJson(object);
                                }
                            }).execute(tmp);
                        }
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                        intent.setClassName(this, MoreInfo.class.getName());
                        intent.putExtra("scan", scan.toString());
                        intent.putExtra("type", type);
                        intent.putExtra("token", token);
                        intent.putExtra("device", device);
                        intent.putExtra("count", count);
//                        intent.putExtra("scantype", getIntent().getExtras().getString("type"));
                        startActivity(intent);
                        Log.d(TAG, "Barcode read: " + barcode.displayValue);
//                    }
                }else {
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            }
        }else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

/*

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    statusMessage.setText(R.string.barcode_success);
                    barcodeValue.setText(barcode.displayValue);
                    Log.d(TAG, "Barcode read: " + barcode.displayValue);
                } else {
                    statusMessage.setText(R.string.barcode_failure);
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                statusMessage.setText(String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

*/
private void processJson(JSONObject object) {
    String name = "ERROR: In saving scan to the CLOUD check internet connection.";
    String store = "";
    try {
        name = object.getJSONObject("level").getString("desc");
        store = object.getJSONObject("level").getString("result");
    } catch (JSONException e) {
        Toast.makeText(getBaseContext(),"ERROR: Reply from server incorrect please contact support at info@npscloud.co.za",Toast.LENGTH_LONG).show();
    }
    Toast.makeText(getBaseContext(),name.toString(),Toast.LENGTH_LONG).show();
}

}