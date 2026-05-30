package edu.polytech.filrouge_tp5;

import android.content.Intent;
import android.os.Bundle;

import edu.polytech.filrouge_tp5.R;
import androidx.appcompat.app.AppCompatActivity;

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

        // Splash/login entry: go to the SignalRoute map screen.
        findViewById(R.id.goDefault).setOnClickListener(clic -> {
            Intent intent = new Intent(getApplicationContext(), ControlActivity.class);
            intent.putExtra(getString(R.string.index), Screen4Fragment.FRAGMENT_ID);
            startActivity(intent);
        });

        // The TP5 option button opens fragment #3, where factory + voice command are demonstrated.
        findViewById(R.id.option).setOnClickListener(clic -> {
            Intent intent = new Intent(getApplicationContext(), ControlActivity.class);
            intent.putExtra(getString(R.string.index), Screen3Fragment.FRAGMENT_ID);
            startActivity(intent);
        });
    }

}
