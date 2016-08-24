// Copyright 2011 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.hughes.android.dictionary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.hughes.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public final class HtmlDisplayActivity extends ActionBarActivity {

    static final String LOG = "QuickDic";

    static final String HTML_RES = "html_res";
    static final String HTML = "html";
    static final String TEXT_TO_HIGHLIGHT = "textToHighlight";
    static final String SHOW_OK_BUTTON = "showOKButton";

    public static Intent getHelpLaunchIntent(Context c) {
        final Intent intent = new Intent(c, HtmlDisplayActivity.class);
        intent.putExtra(HTML_RES, R.raw.help);
        return intent;
    }

    public static Intent getWhatsNewLaunchIntent(Context c) {
        final Intent intent = new Intent(c, HtmlDisplayActivity.class);
        intent.putExtra(HTML_RES, R.raw.whats_new);
        return intent;
    }

    public static Intent getHtmlIntent(Context c, final String html, final String textToHighlight,
            final boolean showOkButton) {
        final Intent intent = new Intent(c, HtmlDisplayActivity.class);
        intent.putExtra(HTML, html);
        intent.putExtra(TEXT_TO_HIGHLIGHT, textToHighlight);
        intent.putExtra(SHOW_OK_BUTTON, showOkButton);
        return intent;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        setTheme(((DictionaryApplication) getApplication()).getSelectedTheme().themeId);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.html_display_activity);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        final int htmlRes = getIntent().getIntExtra(HTML_RES, -1);
        String html;
        if (htmlRes != -1) {
            InputStream res = getResources().openRawResource(htmlRes);
            html = StringUtil.readToString(res);
            try {
                res.close();
            } catch (IOException e) {
            }
        } else {
            html = getIntent().getStringExtra(HTML);
        }
        final MyWebView webView = (MyWebView) findViewById(R.id.webView);
        try {
            // No way to get pure UTF-8 data into WebView
            html = Base64.encodeToString(html.getBytes("UTF-8"), Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Missing UTF-8 support?!", e);
        }
        // Use loadURL to allow specifying a charset
        webView.loadUrl("data:text/html;charset=utf-8;base64," + html);
        webView.activity = this;

        final String textToHighlight = getIntent().getStringExtra(TEXT_TO_HIGHLIGHT);
        if (textToHighlight != null && !"".equals(textToHighlight)) {
            Log.d(LOG, "NOT Highlighting text: " + textToHighlight);
            // This isn't working:
            // webView.findAll(textToHighlight);
            // webView.showFindDialog(textToHighlight, false);
        }

        final Button okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (!getIntent().getBooleanExtra(SHOW_OK_BUTTON, true)) {
            okButton.setVisibility(Button.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        final MyWebView webView = (MyWebView)findViewById(R.id.webView);
        if (webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Explicitly handle the up button press so
        // we return to the dictionary.
        if (item.getItemId() == android.R.id.home)
        {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
