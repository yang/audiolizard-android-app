package com.audiolizard.audiolizard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


public class EnqueueActivity extends ActionBarActivity {

    public String TAG = "aoeu";

    class Response {
        public String response;
        public Exception error;
    }

    class RequestTask extends AsyncTask<String, String, Response> {

        @Override
        protected Response doInBackground(String... uri) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            Response resp = new Response();
            try {
                // Apache libs are deprecated, but we continue to force using legacy lib.
                // These commented lines show what new code should look like, according to https://stackoverflow.com/questions/32153318/httpclient-wont-import-in-android-studio
//                URL urlObj = new URL(uri[0]);
//                HttpURLConnection urlConnection = (HttpURLConnection) urlObj.openConnection();
//                InputStream is = urlConnection.getInputStream();

                response = httpclient.execute(new HttpGet(uri[0]));
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    resp.response = out.toString();
                    out.close();
                } else {
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (IOException e) {
                Log.e(TAG, "exception", e);
                resp.error = e;
            }
            return resp;
        }

        @Override
        protected void onPostExecute(Response result) {
            super.onPostExecute(result);
            if (result.error != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EnqueueActivity.this);
                builder.setMessage(result.error.toString())
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                // Create the AlertDialog object and return it
                builder.create().show();
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(this)
//                        .setSmallIcon(android.R.drawable.ic_menu_upload)
//                        .setContentTitle("Error uploading")
//                        .setContentText("Failed!");
//
//        // Sets an ID for the notification
//        int mNotificationId = 001;
//        // Gets an instance of the NotificationManager service
//        NotificationManager mNotifyMgr =
//                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        // Builds the notification and issues it.
//        mNotifyMgr.notify(mNotificationId, mBuilder.build());

        Intent intent = getIntent();
        if (savedInstanceState == null && intent != null && intent.getAction().equals(Intent.ACTION_SEND)) {
            String url = intent.getStringExtra(Intent.EXTRA_TEXT);
            List<NameValuePair> params = new LinkedList<>();
            params.add(new BasicNameValuePair("url", url));
            String paramString = URLEncodedUtils.format(params, "utf-8");
            String finalUrl = "https://yz.mit.edu/audiolizard/api/v1/enqueue?" + paramString;


            new RequestTask().execute(finalUrl);
        } else {
            finish();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_enqueue, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
