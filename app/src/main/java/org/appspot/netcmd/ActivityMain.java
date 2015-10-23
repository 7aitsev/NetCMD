package org.appspot.netcmd;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ActivityMain extends Activity {
  private EditText mEditUrl;
  private Button mBtn_enter;
  private Context mContext;

  private boolean mBtn_state = true;
  private final String BTN_STATE_INFO = "btn";

  private class RestRequest extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... params) {
      StringBuilder builder = new StringBuilder();
      try {
        log("Making a GET request");
        URL url = new URL(params[0]);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Content-Type", "text/html");
        connection.setUseCaches(false);

        InputStream inputStream = new BufferedInputStream(connection.getInputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while((line = reader.readLine()) != null) {
          builder.append(line);
        }
        connection.disconnect();
      } catch(Exception e) {
        builder.append(e.getMessage());
        log("Error: " + e.getMessage());
      }
      return builder.toString();
    }

    @Override
    protected void onPostExecute(String s) {
      super.onPostExecute(s);
      log("Response: " + s);
      mBtn_state = true;
      mBtn_enter.setEnabled(mBtn_state);
      Toast.makeText(mContext, mContext.getString(R.string.response) + s, Toast.LENGTH_LONG).show();
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    log("Activity created");

    mContext = this.getApplicationContext();
    mEditUrl = (EditText) findViewById(R.id.url);
    mEditUrl.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        try {
          if(i == EditorInfo.IME_ACTION_DONE ||
            keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
              keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            log("User pressed Enter.");
            performRequest(makeUrl());
            if(getCurrentFocus() != null) {
              InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
              imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
              return true;
            }
          }
        } catch(Exception e) {
          log("Runtime error: " + e.toString());
        }
        return false;
      }
    });

    mBtn_enter = (Button) findViewById(R.id.btn_enter);
    mBtn_enter.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        log("Button pressed");
        performRequest(makeUrl());
      }
    });

    if(savedInstanceState != null) {
      mBtn_state = savedInstanceState.getBoolean(BTN_STATE_INFO);
      log("State restored");
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    log("Invoked onSaveInstanceState");
    outState.putBoolean(BTN_STATE_INFO, mBtn_state);
  }

  private String makeUrl() {
    String url = mEditUrl.getText().toString();
    Uri uri = Uri.parse(url);
    if(uri.getScheme() == null)
      url = getString(R.string.scheme) + url;
    log("Generated ULR: " + url);
    return url;
  }

  private void performRequest(String url) {
    mBtn_state = false;
    mBtn_enter.setEnabled(mBtn_state);
    if(! mBtn_state) {
      log("Starting new request");
      RestRequest request = new RestRequest();
      request.execute(url);
    }
  }

  private void log(String text) {
    final String TAG = "ActivityMain";
    Log.d(TAG, text);
  }
}