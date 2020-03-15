package quinta.uqac.gogloecalendrier;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
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
import java.util.ArrayList;

public class SignUpActivity extends AppCompatActivity {

	private EditText etMail;
	private EditText etUsername;
	private EditText etPassword;
	private EditText etConfPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_up);

		etMail         = findViewById(R.id.etMail);
		etUsername     = findViewById(R.id.etUsername);
		etPassword     = findViewById(R.id.etPassword);
		etConfPassword = findViewById(R.id.etConfPassword);
	}

	@Override
	public void onBackPressed() {
		Intent mainIntent = new Intent(SignUpActivity.this, LoginActivity.class);
		SignUpActivity.this.startActivity(mainIntent);
		SignUpActivity.this.finish();
	}


	final static int PERMISSIONS_REQUEST_INTERNET = 1234;
	public void signup(View view) {

		if ( ContextCompat.checkSelfPermission( this, Manifest.permission.INTERNET ) != PackageManager.PERMISSION_GRANTED ) {
			Toast.makeText(this, "Permission d'internet non accessible", Toast.LENGTH_SHORT).show();
			ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.INTERNET  },
					PERMISSIONS_REQUEST_INTERNET);
		}else {
			ArrayList<String> errors = checkFields();
			if(errors.isEmpty()) {
				String mail     = etMail.getText().toString();
				String username = etUsername.getText().toString();
				String password = etPassword.getText().toString();
				new Inscription().execute("https://gogloecalendrier.alwaysdata.net/signUp.php", mail, username, password);
			}else {

				Drawable warnIco = getDrawable(R.drawable.triangle_gogloe);
				assert warnIco != null;
				warnIco.setBounds(0, 0, warnIco.getIntrinsicWidth(), warnIco.getIntrinsicHeight());

				for(String s : errors) {
					switch (s) {
						case "InvalidMailFormat":
							etMail.setError(getResources().getString(R.string.invalid_mail_format), warnIco);
							break;
						case "UsernameTooLong":
							etUsername.setError(getResources().getString(R.string.username_too_long), warnIco);
							break;
						case "PasswordTooLong":
							etPassword.setError(getResources().getString(R.string.password_too_long), warnIco);
							break;
						case "NotCorrespondingPasswords":
							etConfPassword.setError(getResources().getString(R.string.passwords_not_matching), warnIco);
							break;
					}
				}
			}
		}		
	}

	private ArrayList<String> checkFields() {
		ArrayList<String> ret = new ArrayList<>();

		String mail         = etMail.getText().toString();
		String username     = etUsername.getText().toString();
		String password     = etPassword.getText().toString();
		String confPassword = etConfPassword.getText().toString();

		if(!mail.matches("^([a-zA-Z0-9_\\-.]+)@([a-zA-Z0-9_\\-.]+)\\.([a-zA-Z]{2,5})$") || mail.length() > 100)
			ret.add("InvalidMailFormat");
		if(username.length() > 36)
			ret.add("UsernameTooLong");
		if(password.length() > 50)
			ret.add("PasswordTooLong");
		if(!password.equals(confPassword))
			ret.add("NotCorrespondingPasswords");

		return ret;
	}


	@SuppressLint("StaticFieldLeak")
	private class Inscription extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {

			try {

				URL urlPhp = new URL(params[0]);
				HttpURLConnection urlCon = (HttpURLConnection) urlPhp.openConnection();

				String data = URLEncoder.encode("mail", "UTF-8") + "=" + URLEncoder.encode(params[1], "UTF-8");
				data += "&" + URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(params[2], "UTF-8");
				data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(params[3], "UTF-8");

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

			}catch (UnknownHostException uhException) {
				return "Erreur Internet";
			}
			catch (IOException e) {
				e.printStackTrace();
				return "Erreur Inconnue";
			}

		}


		@Override
		protected void onPostExecute(String result) {

			if(result.startsWith("Err:")) { // If there was an error

				Drawable warnIco = getDrawable(R.drawable.triangle_gogloe);
				assert warnIco != null;
				warnIco.setBounds(0, 0, warnIco.getIntrinsicWidth(), warnIco.getIntrinsicHeight());

				Intent mainIntent = new Intent(SignUpActivity.this, LoginActivity.class);

				String error = result.split(":")[1];
				switch (error) {
					case "InvalidEmailFormat": // Shouldn't be possible
						Toast.makeText(SignUpActivity.this, "Adresse courriel invalide", Toast.LENGTH_SHORT).show();
						SignUpActivity.this.etMail.setError("Adresse courriel invalide", warnIco);
						break;
					case "UsernameTooLong": // Shouldn't be possible
						Toast.makeText(SignUpActivity.this, "Nom d'utilisateur trop long", Toast.LENGTH_SHORT).show();
						SignUpActivity.this.etUsername.setError("Nom d'utilisateur trop long(max 36 caractères)", warnIco);
						break;
					case "PasswordTooLong": // Shouldn't be possible
						Toast.makeText(SignUpActivity.this, "Mot de passe trop long", Toast.LENGTH_SHORT).show();
						SignUpActivity.this.etPassword.setError("Mot de passe trop long (max 50 caractères)", warnIco);
						break;
					case "MailAlrUsed":
						Toast.makeText(SignUpActivity.this, "Adresse courriel déjà utilisée", Toast.LENGTH_SHORT).show();
						SignUpActivity.this.etUsername.setError("Un compte avec cette adresse existe déjà", warnIco);
						break;
					case "UsernameAlrUsed":
						Toast.makeText(SignUpActivity.this, "Nom d'utilisateur déjà utilisé", Toast.LENGTH_SHORT).show();
						SignUpActivity.this.etUsername.setError("Un compte avec ce nom d'utilisateur existe déjà", warnIco);
						break;
					case "MailFail":
						Toast.makeText(SignUpActivity.this, "Erreur dans l'envoi du mail de confirmation, votre compte a été vérifié", Toast.LENGTH_LONG).show();
						SignUpActivity.this.startActivity(mainIntent);
						SignUpActivity.this.finish();
						break;
					case "Confed":
						Toast.makeText(SignUpActivity.this, "Erreur serveur, votre compte a été vérifié", Toast.LENGTH_LONG).show();
						SignUpActivity.this.startActivity(mainIntent);
						SignUpActivity.this.finish();
						break;
					case "ImpQuery":
						Toast.makeText(SignUpActivity.this, "Erreur serveur, veuillez réessayer ultérieuremet", Toast.LENGTH_SHORT).show();
						break;
					default:
						Toast.makeText(SignUpActivity.this, "Erreur inconnue, veuillez réessayer", Toast.LENGTH_SHORT).show();
				}
			}else { // Pas d'erreur
				if(result.equals("Confsend")) {
					Toast.makeText(SignUpActivity.this, "Un mail de confirmation vous a été envoyé.", Toast.LENGTH_LONG).show();
					Intent mainIntent = new Intent(SignUpActivity.this, LoginActivity.class);
					SignUpActivity.this.startActivity(mainIntent);
					SignUpActivity.this.finish();
				}else {
					Toast.makeText(SignUpActivity.this, "Erreur inconnue", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}
}
