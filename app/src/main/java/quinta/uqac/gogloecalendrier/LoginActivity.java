package quinta.uqac.gogloecalendrier;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {

	private EditText etMailOrUsername;
	private EditText etPassword;
	private TextView tvErrors;
	private CheckBox cbRememberMe;

	private Button btnConnection;

	private ConstraintLayout loadingLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		etMailOrUsername = findViewById(R.id.etMailOrUsername);
		etPassword       = findViewById(R.id.etPassword);
		tvErrors         = findViewById(R.id.errors);
		cbRememberMe     = findViewById(R.id.cbRememberMe);
		loadingLayout    = findViewById(R.id.loadingLayout);
		btnConnection    = findViewById(R.id.btnConnection);

		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		String token = sharedPreferences.getString("Token", null);
		if(token != null) {
			btnConnection.setEnabled(false);
			Log.i("Quinta", "Connexion token");
			//afficherChargement();
			new TokenConnexion().execute("https://gogloecalendrier.alwaysdata.net/connection.php", token);
		}
	}

	public void goToSignUp(View view) {
		Intent mainIntent = new Intent(LoginActivity.this, SignUpActivity.class);
		LoginActivity.this.startActivity(mainIntent);
		LoginActivity.this.finish();
	}

	public void connexion(View view) {
		String mailOrUsername = etMailOrUsername.getText().toString();
		String password       = etPassword.getText().toString();
		ArrayList<String> errors = checkFields(mailOrUsername, password);
		if(errors.isEmpty()) {
			btnConnection.setEnabled(false);
			Connexion connexion = new Connexion();
			connexion.execute("https://gogloecalendrier.alwaysdata.net/connection.php", mailOrUsername, password);
		}else {

			Drawable warnIco = getDrawable(R.drawable.triangle_gogloe);
			assert warnIco != null;
			warnIco.setBounds(0, 0, warnIco.getIntrinsicWidth(), warnIco.getIntrinsicHeight());

			for(String s : errors) {
				switch (s) {
					case "InvalidMailOrUsername":
						etMailOrUsername.setError(getResources().getString(R.string.mailorusername_invalid), warnIco);
						break;
					case "PasswordTooLong":
						etPassword.setError(getResources().getString(R.string.password_invalid), warnIco);
						break;
				}
			}
		}
	}

	private ArrayList<String> checkFields(String mailOrUsername, String passsword) {
		ArrayList<String> ret = new ArrayList<>();

		if (mailOrUsername.length() > 100 || !mailOrUsername.matches("^([a-zA-Z0-9_\\-.]+)@([a-zA-Z0-9_\\-.]+)\\.([a-zA-Z]{2,5})$") && mailOrUsername.length() > 36) {
			ret.add("InvalidMailOrUsername");
		}else if(passsword.length() > 50) {
			ret.add("PasswordTooLong");
		}
		return ret;
	}

	private void afficherChargement() {
		loadingLayout.setVisibility(View.VISIBLE);
	}

	private void retirerChargement() {
		loadingLayout.setVisibility(View.INVISIBLE);
	}

	final static String SHARED_PREFS = "GogloeCalendrier";
	final static String TOKEN = "Token";
	final static String TEMP_TOKEN = "TempToken";

	@SuppressLint("StaticFieldLeak")
	private class Connexion extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {

			try {
				URL urlPhp = new URL(params[0]);
				HttpURLConnection urlCon = (HttpURLConnection) urlPhp.openConnection();

				String data = URLEncoder.encode("mailOrUsername", "UTF-8") + "=" + URLEncoder.encode(params[1], "UTF-8");
				data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(params[2], "UTF-8");

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
				return "Ouioui";
				//return "Err:CheckInternet";
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
				return "Erreur Inconnue";
			}

		}

		@Override
		protected void onPostExecute(String result) {

			//retirerChargement();

			if(result.startsWith("Err:")) { // If there was an error

				Drawable warnIco = getDrawable(R.drawable.triangle_gogloe);
				assert warnIco != null;
				warnIco.setBounds(0, 0, warnIco.getIntrinsicWidth(), warnIco.getIntrinsicHeight());

				String error = result.split(":")[1];
				switch (error) {
					case "MailOrUsernameTooLong": // Shouldn't be possible
						Toast.makeText(LoginActivity.this, "Identifiant invalide", Toast.LENGTH_SHORT).show();
						break;
					case "PasswordTooLong": // Shouldn't be possible
						Toast.makeText(LoginActivity.this, "Mot de passe invalide", Toast.LENGTH_SHORT).show();
						break;
					case "NotConfirmed":
						Toast.makeText(LoginActivity.this, "Compte non validé, veuillez confirmer le compte via le courriel qui vous a été envoyé", Toast.LENGTH_SHORT).show();
						LoginActivity.this.tvErrors.setText("Votre compte n'a pas été confirmé, vérifiez vos mails et confirmez votre compte.");
						break;
					case "IncorrectPass":
						Toast.makeText(LoginActivity.this, "Mot de passe incorrect", Toast.LENGTH_SHORT).show();
						LoginActivity.this.etPassword.setError("Mot de passe incorrect", warnIco);
						break;
					case "NotFound":
						Toast.makeText(LoginActivity.this, "Utilisateur introuvable", Toast.LENGTH_SHORT).show();
						LoginActivity.this.etMailOrUsername.setError("Identifiant introuvable", warnIco);
						break;
					case "CheckInternet":
						Toast.makeText(LoginActivity.this, "Impossible d'effectuer la requête, vérifiez votre connexion internet", Toast.LENGTH_LONG).show();
						break;
					default:
						Toast.makeText(LoginActivity.this, "Erreur inconnue", Toast.LENGTH_SHORT).show();
				}
			}else { // Pas d'erreur
				if(result.startsWith("Token:")) {
					String token = result.split(":")[1];
					SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
					SharedPreferences.Editor editor = sharedPreferences.edit();
					if(LoginActivity.this.cbRememberMe.isChecked()) {
						editor.putString(TOKEN, token);
					}else {
						editor.putString(TEMP_TOKEN, token);
					}
					editor.apply();
					String[] tokenParts = token.split("\\.");
					byte[] decoded = Base64.decode(tokenParts[1], Base64.URL_SAFE);
					String json = new String(decoded);
					try {
						JSONObject jsonObj = new JSONObject(json);
						System.out.println(jsonObj);
						String username = jsonObj.get("username").toString();
						Toast.makeText(LoginActivity.this, "Connecté en tant que : " + username, Toast.LENGTH_SHORT).show();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				Intent mainIntent = new Intent(LoginActivity.this, CalendrierActivity.class);
				LoginActivity.this.startActivity(mainIntent);
				LoginActivity.this.finish();
			}
			btnConnection.setEnabled(true);
		}
	}

	@SuppressLint("StaticFieldLeak")
	private class TokenConnexion extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {

			try {
				URL urlPhp = new URL(params[0]);
				HttpURLConnection urlCon = (HttpURLConnection) urlPhp.openConnection();

				String data = URLEncoder.encode("token", "UTF-8") + "=" + URLEncoder.encode(params[1], "UTF-8");

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
		protected void onPostExecute(String result) {

			//retirerChargement();

			Log.i("Quinta", "Result : " + result);

			if(result.startsWith("Err:")) { // If there was an error
				String error = result.split(":")[1];
				switch (error) {
					case "InvalidTokenSyntax|MissingUserUUID": // Shouldn't be possible
					case "InvalidTokenSyntax|MissingParameters":
					case "InvalidTokenSyntax|MissingCreationTime":
						Toast.makeText(LoginActivity.this, "Token de connexion invalide, veuillez vous connecter manuellement", Toast.LENGTH_SHORT).show();
						break;
					case "QueryResultInvalid":
						Toast.makeText(LoginActivity.this, "Erreur serveur", Toast.LENGTH_SHORT).show();
						break;
					case "TokenRevoked":
						Toast.makeText(LoginActivity.this, "Token révoqué", Toast.LENGTH_SHORT).show();
						LoginActivity.this.tvErrors.setText(getResources().getString(R.string.token_revoked));
						break;
					case "ExpiredOrInvalidSyntaxToken":
						Toast.makeText(LoginActivity.this, "Utilisateur introuvable", Toast.LENGTH_SHORT).show();
						LoginActivity.this.tvErrors.setText(getResources().getString(R.string.token_expired));
						break;
					case "InvalidSignature":
						Toast.makeText(LoginActivity.this, "Signature du token de connexion incorrecte", Toast.LENGTH_SHORT).show();
						break;
					default:
						Toast.makeText(LoginActivity.this, "Erreur inconnue", Toast.LENGTH_SHORT).show();
				}
			}else { // Pas d'erreur
				if(result.equals("Connected")) {
					Intent mainIntent = new Intent(LoginActivity.this, CalendrierActivity.class);
					LoginActivity.this.startActivity(mainIntent);
					LoginActivity.this.finish();
				}else {
					Toast.makeText(LoginActivity.this, "Erreur inconnue", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

}
