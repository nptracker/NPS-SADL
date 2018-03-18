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

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.net.Uri;
/**
 * An HTML-based More Info screen.
 *
 * @author info@nptracker.co.za (Eduard Stander)
 */
public final class MoreInfo extends Activity {

  private static String BASE_URL = "https://lic.nptracker.co.za/MoreInfo.html?";

  private WebView webView;

  @Override
  protected void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    setContentView(R.layout.webview);

    webView = (WebView) findViewById(R.id.webView1);
    WebSettings webSettings = webView.getSettings();
    webSettings.setJavaScriptEnabled(true);
    webSettings.setDomStorageEnabled(true);
    webSettings.setLoadWithOverviewMode(true);
    webSettings.setUseWideViewPort(true);
    webSettings.setBuiltInZoomControls(true);
    webSettings.setDisplayZoomControls(false);
    webSettings.setSupportZoom(true);
    webSettings.setDefaultTextEncodingName("utf-8");

    webView.getSettings().setJavaScriptEnabled(true);
    webView.getSettings().setAppCacheEnabled(false);
    webView.getSettings().setDomStorageEnabled(true);
    webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
    if (icicle == null) {
      String scantype = Uri.encode(getIntent().getExtras().getString("ScanType"));
      String scan = Uri.encode(getIntent().getExtras().getString("scan"));
      String type = getIntent().getExtras().getString("type");
      String token = Uri.encode(getIntent().getExtras().getString("token"));
      String device = Uri.encode(getIntent().getExtras().getString("device"));
      String count = Uri.encode(getIntent().getExtras().getString("count"));
      if (type.equals("Licence Disk")) {BASE_URL = "https://lic.nptracker.co.za/MoreInfo.html?";}
      if (type.equals("Drivers Licence")) {BASE_URL = "https://lic.nptracker.co.za/DriversLicence.html?";}
      if (type.equals("ID")) {BASE_URL = "https://lic.nptracker.co.za/IDCard.html?";}
      type = Uri.encode(type);
      webView.loadUrl(BASE_URL + "token=" + token + "&device=" + device + "&scan=" + scan + "&type=" + type + "&scantype=" + scantype + "&count=" + count + "&version=0203");
    } else {
      webView.restoreState(icicle);
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
      webView.goBack();
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override
  protected void onSaveInstanceState(Bundle icicle) {
    super.onSaveInstanceState(icicle);
    webView.saveState(icicle);
  }
}
