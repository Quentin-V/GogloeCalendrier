package quinta.uqac.gogloecalendrier;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;

import static quinta.uqac.gogloecalendrier.LoginActivity.SHARED_PREFS;
import static quinta.uqac.gogloecalendrier.LoginActivity.TEMP_TOKEN;

public class GogloeActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		if(sharedPreferences.getString(TEMP_TOKEN, null) != null) {
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.remove(TEMP_TOKEN);
			editor.apply();
		}
	}
}
