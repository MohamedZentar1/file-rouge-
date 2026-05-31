package edu.polytech.filrouge_tp5;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import edu.polytech.filrouge_tp5.view.Screen5Fragment;

/**
 * This application has two activities:
 *
 *     MainActivity, which contains a frame-by-frame animation.
 *     ControlActivity, which contains two fragments:
 *         MenuFragment (static fragment)
 *         Screen1Fragment (dynamic fragment)
 *
 * @author F. Rallo - march 2025
 */
public class MainActivity extends AppCompatActivity {
    private final String TAG = "frallo "+getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Splash/login entry: go to the SignalRoute map dashboard.
        findViewById(R.id.goDefault).setOnClickListener(clic -> {
            Intent intent = new Intent(getApplicationContext(), ControlActivity.class);
            intent.putExtra(getString(R.string.index), Screen5Fragment.FRAGMENT_ID);
            startActivity(intent);
        });

        // The option button opens Screen5Fragment, where OSM + MVC are demonstrated.
        findViewById(R.id.option).setOnClickListener(clic -> {
            Intent intent = new Intent(getApplicationContext(), ControlActivity.class);
            intent.putExtra(getString(R.string.index), Screen5Fragment.FRAGMENT_ID);
            startActivity(intent);
        });
    }

}
