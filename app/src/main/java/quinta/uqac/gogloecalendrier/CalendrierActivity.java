package quinta.uqac.gogloecalendrier;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static quinta.uqac.gogloecalendrier.LoginActivity.*;

public class CalendrierActivity extends GogloeActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calendrier);

		RetrieveEvents re = new RetrieveEvents();
		re.execute("https://gogloecalendrier.alwaysdata.net/retrieveEvents.php");
	}

	public void gotoAddEvent(View view) {
		Intent mainIntent = new Intent(CalendrierActivity.this, AddEventActivity.class);
		CalendrierActivity.this.startActivity(mainIntent);
		CalendrierActivity.this.finish();
	}



	@SuppressLint("StaticFieldLeak")
	private class RetrieveEvents extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {

			try {
				URL urlPhp = new URL(params[0]);
				HttpURLConnection urlCon = (HttpURLConnection) urlPhp.openConnection();

				SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
				String token = sharedPreferences.getString(TOKEN, null);
				if(token == null) token = sharedPreferences.getString(TEMP_TOKEN, null);
				if(token == null) {
					Toast.makeText(CalendrierActivity.this, "Erreur, pas de token stocké", Toast.LENGTH_SHORT).show();
					return null;
				}


				String data = URLEncoder.encode("token", "UTF-8") + "=" + URLEncoder.encode(token, "UTF-8");

				urlCon.setDoInput(true);
				urlCon.setDoOutput(true);

				urlCon.setRequestMethod("POST");

				OutputStreamWriter wr = new OutputStreamWriter(urlCon.getOutputStream());
				wr.write(data);
				wr.flush();

				InputStream is = urlCon.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));

				urlCon.connect();

				String retour = br.readLine();

				wr.close();
				is.close();
				br.close();
				urlCon.disconnect();

				return retour;

			}catch (UnknownHostException uhe) {
				return "[{name: 'Test', start_date: '2020-01-01', end_date: '2020-01-02', description: 'Le test',color: '#FF0000'},{name: 'Test2', start_date: '2020-01-02', end_date: '2020-01-03', description: 'Le test 2',color: '#444444'}]";
				//return "Erreur Internet";
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
				return "Erreur Inconnue";
			}

		}

		@Override
		protected void onPostExecute(String result) {

			if (result == null) {
				return;
			}

			if (result.startsWith("Err:")) { // If there was an error
				Toast.makeText(CalendrierActivity.this, "Une erreur est survenue", Toast.LENGTH_SHORT).show();
			} else { // Pas d'erreur
				Log.i("Quinta", result);
				LinearLayout listEvents = findViewById(R.id.listEvts);
				try {
					JSONArray json = new JSONArray(result);
					for (int i = 0; i < json.length(); ++i) {
						JSONObject obj = json.getJSONObject(i);
						Log.i("Quinta", "" + json);
						TextView tv = new TextView(CalendrierActivity.this);
						tv.setText(obj.getString("name") + " : " + obj.getString("start_date") + " --> " + obj.getString("end_date") + " | " + obj.getString("description"));
						//int color = Color.parseColor(obj.getString("color"));
						//if (color < -16777216/2) tv.setTextColor(-1); // Sets the color of the text to white if the background is too dark
						//tv.setBackgroundColor(color);
						listEvents.addView(tv);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
