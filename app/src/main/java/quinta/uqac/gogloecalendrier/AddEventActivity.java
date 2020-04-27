package quinta.uqac.gogloecalendrier;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;

import static quinta.uqac.gogloecalendrier.LoginActivity.SHARED_PREFS;
import static quinta.uqac.gogloecalendrier.LoginActivity.TEMP_TOKEN;
import static quinta.uqac.gogloecalendrier.LoginActivity.TOKEN;

public class AddEventActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_event);
	}

	public void addEvent(View view) {
		EditText etName = findViewById(R.id.etName);
		EditText etDesc = findViewById(R.id.etDescription);
		EditText etColor = findViewById(R.id.etColor);

		String name = etName.getText().toString();
		String desc = etDesc.getText().toString();
		String color = etColor.getText().toString();

		DatePicker dp1 = findViewById(R.id.datePicker);
		DatePicker dp2 = findViewById(R.id.datePicker2);
		String date1 = dp1.getYear() + "-" + dp1.getMonth() + "-" + dp1.getDayOfMonth();
		String date2 = dp2.getYear() + "-" + dp2.getMonth() + "-" + dp2.getDayOfMonth();

		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		String token = sharedPreferences.getString(TOKEN, null);
		if(token == null) token = sharedPreferences.getString(TEMP_TOKEN, null);
		if(token == null) {
			Toast.makeText(AddEventActivity.this, "Erreur, pas de token stocké", Toast.LENGTH_SHORT).show();
			return;
		}


		AjouterEvt ajouterEvt = new AjouterEvt();
		ajouterEvt.execute("https://gogloecalendrier.alwaysdata.net/addEvent.php", token, name, desc, color, date1, date2);
	}

	private static class AjouterEvt extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {

			try {
				URL urlPhp = new URL(params[0]);
				HttpURLConnection urlCon = (HttpURLConnection) urlPhp.openConnection();

				String data = URLEncoder.encode("token", "UTF-8") + "=" + URLEncoder.encode(params[1], "UTF-8");
				data += "&" + URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(params[2], "UTF-8");
				data += "&" + URLEncoder.encode("description", "UTF-8") + "=" + URLEncoder.encode(params[3], "UTF-8");
				data += "&" + URLEncoder.encode("color", "UTF-8") + "=" + URLEncoder.encode(params[4], "UTF-8");
				data += "&" + URLEncoder.encode("startDate", "UTF-8") + "=" + URLEncoder.encode(params[5], "UTF-8");
				data += "&" + URLEncoder.encode("endDate", "UTF-8") + "=" + URLEncoder.encode(params[6], "UTF-8");

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
