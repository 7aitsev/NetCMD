package org.appspot.netcmd;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

class RESTManager {
  /** This listener using for success requests. */
  public interface IExecuted {
    void onExecuted(JSONObject response);
  }

  /** Shall be invoked if something went wrong. */
  public interface IRequestFailed {
    void onFailed(String info);
  }

  /** A single task to be performed. */
  private class RESTTask implements Runnable {
    /* The unique data required to make this request. */
    private String uri;
    private IExecuted callbackOk;
    private IRequestFailed callbackFailed;

    RESTTask(String url, IExecuted ok, IRequestFailed failed) {
      this.uri = url;
      this.callbackOk = ok;
      this.callbackFailed = failed;
    }

    @Override
    public void run() {
      try {
        URL url = new URL(uri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setUseCaches(false);

        InputStream inputStream = new BufferedInputStream(connection.getInputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        StringBuilder builder = new StringBuilder();
        while((line = reader.readLine()) != null) {
          builder.append(line);
        }
        connection.disconnect();
        callbackOk.onExecuted(new JSONObject(builder.toString()));
      } catch(MalformedURLException e) {
        callbackFailed.onFailed("Malformed URL has occurred: " + e.getMessage());
      } catch(IOException e) {
        callbackFailed.onFailed("I/O operation failed or interrupt: " + e.getMessage());
      } catch(JSONException e) {
        callbackFailed.onFailed("An exception happened during JSON processing: " + e.getMessage());
      } catch(Exception e) {
        callbackFailed.onFailed("Unexpected error: " + e.getMessage());
      }
    }
  }

  public void makeRequest(String url, IExecuted ok, IRequestFailed failed) {
    RESTTask task = new RESTTask(url, ok, failed);
    task.run();
  }

  private void log(String text) {
    final String TAG = "____RESTManager";
    Log.d(TAG, text);
  }
}