package sk.tuke.ms.sedentti.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import sk.tuke.ms.sedentti.R;

public class PersonalityTestActivity extends AppCompatActivity {
    private static final String TAG = "PersonalityTestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personality_test);

        Log.d(TAG, "Personality test started");

        // TODO remove
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
