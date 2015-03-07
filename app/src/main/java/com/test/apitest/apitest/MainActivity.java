package com.test.apitest.apitest;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends ActionBarActivity {

    public final static String apiSensisURL = "http://api.sensis.com.au/ob-20110511/test/search?key=";
    public final static String apiSensisKey = "";

    private EditText strQuery, strLocation;
    private HandleJSON obj;

    public final static String EXTRA_MESSAGE = "com.test.apitest.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set the location field to the current location
        determineLocation();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_profile) {
            Log.i("info", "Profile clicked");
            return true;
        }
        if (id == R.id.category_arts) {
            Log.i("info", "Arts clicked");
            return true;
        }
        if (id == R.id.category_events) {
            Log.i("info", "Events clicked");
            return true;
        }
        if (id == R.id.category_food) {
            Log.i("info", "Category clicked");
            return true;
        }
        if (id == R.id.category_sports) {
            Log.i("info", "Sports clicked");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void verifyEmail(View view) {
        // This is the method that is called when the submit button is clicked

        //set the location field to the current location
        determineLocation();
/*
        EditText emailEditText = (EditText) findViewById(R.id.email_address);
        String email = emailEditText.getText().toString();

        strQuery = (EditText)findViewById(R.id.query);
        strLocation = (EditText) findViewById(R.id.location);

        if( email != null && !email.isEmpty()) {
            String urlString = apiURL + "LicenseInfo.RegisteredUser.UserID=" + strikeIronUserName + "&LicenseInfo.RegisteredUser.Password=" + strikeIronPassword + "&VerifyEmail.Email=" + email + "&VerifyEmail.Timeout=30";
            String urlSensis = apiSensisURL + "" + apiSensisKey + "&query=Pizza&location=Dallas";

            obj = new HandleJSON(urlSensis);
            obj.fetchJSON();

            //while(obj.parsingComplete);
            //strQuery.setText(obj.getName());

            //new CallSensisAPI().execute(urlSensis);
            //new CallAPI().execute(urlString);
        }
*/
    }

    private void determineLocation() {
        PhoneLocation phoneLoc = new PhoneLocation(this); //Here the context is passing

        // get the location
        Location location = phoneLoc.getLocation();

        // build location string for display
        String latLongString = phoneLoc.updateWithNewLocation(location);

        // display the location string
        TextView myLocationText = (TextView)findViewById(R.id.location);
        myLocationText.setText(latLongString);
    }

    private class CallAPI extends AsyncTask<String, String, String> {

        protected String doInBackground(String... params) {
            String urlString=params[0];
            String resultToDisplay;
            emailVerificationResult result = null;
            InputStream in = null;
            // HTTP Get
            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                in = new BufferedInputStream(urlConnection.getInputStream());
            } catch (Exception e ) {
                System.out.println(e.getMessage());
                return e.getMessage();
            }

            // Parse XML
            XmlPullParserFactory pullParserFactory;
            try {
                pullParserFactory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = pullParserFactory.newPullParser();

                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(in, null);
                result = parseXML(parser);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Simple logic to determine if the email is dangerous, invalid, or valid
            if (result != null ) {
                if( result.hygieneResult.equals("Spam Trap")) {
                    resultToDisplay = "Dangerous email, please correct";
                }
                else if( Integer.parseInt(result.statusNbr) >= 300) {
                    resultToDisplay = "Invalid email, please re-enter";
                }
                else {
                    resultToDisplay = "Thank you for your submission";
                }
            }
            else {
                resultToDisplay = "Exception Occured";
            }
            return resultToDisplay;
        }

        protected void onPostExecute(String result) {
            Intent intent = new Intent(getApplicationContext(), ResultActivity.class);

            intent.putExtra(EXTRA_MESSAGE, result);
            startActivity(intent);
        }
    } // end CallAPI

    private class emailVerificationResult {
        public String statusNbr;
        public String hygieneResult;
    }

    private emailVerificationResult parseXML( XmlPullParser parser ) throws XmlPullParserException, IOException {

        int eventType = parser.getEventType();
        emailVerificationResult result = new emailVerificationResult();

        while( eventType!= XmlPullParser.END_DOCUMENT) {
            String name = null;

            switch(eventType)
            {
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    if( name.equals("Error")) {
                        System.out.println("Web API Error!");
                    }
                    else if ( name.equals("StatusNbr")) {
                        result.statusNbr = parser.nextText();
                    }
                    else if (name.equals("HygieneResult")) {
                        result.hygieneResult = parser.nextText();
                    }
                    break;
                case XmlPullParser.END_TAG:
                    break;
            } // end switch

            eventType = parser.next();
        } // end while

        return result;
    }

    /*
    private class CallSensisAPI extends AsyncTask<String, String, String> {

        protected JSONObject doInBackground(String... params) {
            String urlSensis=params[0];
            String resultToDisplay;
            emailVerificationResult result = null;
            InputStream in = null;
            // HTTP Get
            try {
                URL url = new URL(urlSensis);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                in = new BufferedInputStream(urlConnection.getInputStream());
                return new JSONObject(getReponseText(in));
            } catch (Exception e ) {
                System.out.println(e.getMessage());
                return null;
            }
            return null;
        }

        private String getReponseText(InputStream inStream) {
            return new Scanner(inStream).useDelimiter("\\A").next();
        }
        protected void onPostExecute(String result) {
            Intent intent = new Intent(getApplicationContext(), ResultActivity.class);

            intent.putExtra(EXTRA_MESSAGE, result);
            startActivity(intent);
        }
    } // end CallAPI
    */
}
