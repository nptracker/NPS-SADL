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
import android.view.View;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

    public class TokenActivity extends Activity implements View.OnClickListener {
        // use a compound button so either checkbox or switch widgets work.
        private Activity activity;
        private String UserName="";
        private String PhoneNumber="";
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_token);
            findViewById(R.id.btnGuardLogin).setOnClickListener(this);
        }

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            String tmp="";
            EditText text = (EditText)findViewById(R.id.editUser1);
            String UserName = text.getText().toString();
            text = (EditText)findViewById(R.id.editPassword1);
            String Password = text.getText().toString();
            if (v.getId() == R.id.btnGuardLogin) {
                try {
                    tmp = "https://lic.nptracker.co.za/cgi-bin/loginToken.cgi" + "?username=" + URLEncoder.encode(UserName, "UTF-8") + "&password=" + URLEncoder.encode(Password, "UTF-8");
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
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }

        private void processJson(JSONObject object) {
            String name = "ERROR: In saving scan to the CLOUD check internet connection.";
            String token = "";
            try {
                UserName = object.getJSONObject("level").getString("username");
                token = object.getJSONObject("level").getString("token");
            } catch (JSONException e) {
                Toast.makeText(getBaseContext(),"ERROR: Reply from server incorrect please contact support at info@npscloud.co.za",Toast.LENGTH_LONG).show();
            }
            if (UserName.isEmpty()){Toast.makeText(getBaseContext(),"USER/PASSWORD NOT registered, please create user in your NPSCLOUD account [NPS-ACCESS module]",Toast.LENGTH_LONG).show();}
            else {
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString("NPS-API", token).apply();
                Toast.makeText(getBaseContext(),"Access token updated.",Toast.LENGTH_LONG).show();
                Toast.makeText(getBaseContext(),token,Toast.LENGTH_LONG).show();
            }
        }

    }