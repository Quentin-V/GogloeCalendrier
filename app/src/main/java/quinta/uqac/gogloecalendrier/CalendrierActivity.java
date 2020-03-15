package quinta.uqac.gogloecalendrier;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TimePicker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CalendrierActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calendrier);

		TimePicker tp1 = findViewById(R.id.timePicker);
		TimePicker tp2 = findViewById(R.id.timePicker2);

		tp1.setIs24HourView(true);
		tp2.setIs24HourView(true);
	}

	public void go(View v) {
		Date currentTime = Calendar.getInstance().getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CANADA_FRENCH);
		SharedPreferences sp = getSharedPreferences("GogloeCalendrier", MODE_PRIVATE);
		String token = sp.getString("Token", null);
		TimePicker tp1 = findViewById(R.id.timePicker);
		TimePicker tp2 = findViewById(R.id.timePicker2);
		tp1.setIs24HourView(true);
		tp2.setIs24HourView(true);
		String start = sdf.format(currentTime) + " " +  tp1.getHour() + ":" + tp1.getMinute() + ":00";
		String end = sdf.format(currentTime) + " " +  tp2.getHour() + ":" + tp2.getMinute() + ":00";

		if(token != null) {
			Log.i("Quinta", token);
			Log.i("Quinta", start);
			Log.i("Quinta", end);
			new AjouterEvt().execute("https://gogloecalendrier.alwaysdata.net/addevent.php", token, start, end);
		}else{
			Log.i("Quinta", "TOKEN = NULL");
		}
	}

	private static class AjouterEvt extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {

			try {
				URL urlPhp = new URL(params[0]);
				HttpURLConnection urlCon = (HttpURLConnection) urlPhp.openConnection();

				String data = URLEncoder.encode("token", "UTF-8") + "=" + URLEncoder.encode(params[1], "UTF-8");
				data += "&" + URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode("eventname", "UTF-8");
				data += "&" + URLEncoder.encode("description", "UTF-8") + "=" + URLEncoder.encode("eventdesc", "UTF-8");
				data += "&" + URLEncoder.encode("startDate", "UTF-8") + "=" + URLEncoder.encode(params[2], "UTF-8");
				data += "&" + URLEncoder.encode("endDate", "UTF-8") + "=" + URLEncoder.encode(params[2], "UTF-8");

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
				return "Erreur Internet";
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
				return "Erreur Inconnue";
			}

		}

		@Override
		protected void onPostExecute(String result) { // TODO Gestion des erreurs avec le retour de la request
			Log.i("Quinta", result);
		}
	}
}
