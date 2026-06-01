package edu.polytech.filrouge_tp5.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Locale;

import edu.polytech.filrouge_tp5.Notifiable;
import edu.polytech.filrouge_tp5.R;
import edu.polytech.filrouge_tp5.factory.AccidentFactory;
import edu.polytech.filrouge_tp5.factory.HighwayFactory;
import edu.polytech.filrouge_tp5.factory.UrbanFactory;
import edu.polytech.filrouge_tp5.model.Issue;

public class Screen6Fragment extends Fragment {
    public final static int FRAGMENT_ID = 5;
    private final String TAG = "frallo " + getClass().getSimpleName();
    private Notifiable notifiable;
    private TextView voiceStatus;

    public Screen6Fragment() {
        Log.d(TAG, "screenFragment type 6 created"); // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        notifiable.onFragmentDisplayed(FRAGMENT_ID);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (requireActivity() instanceof Notifiable) {
            notifiable = (Notifiable) requireActivity();
        } else {
            throw new AssertionError("Classe " + requireActivity().getClass().getName() + " ne met pas en oeuvre Notifiable.");
        }
    }

    private final ActivityResultLauncher<Intent> voiceLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    ArrayList<String> matches = result.getData()
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty()) {
                        createIssueFromVoice(matches.get(0));
                    } else {
                        promptManualEntry();
                    }
                } else {
                    promptManualEntry();
                }
            }
    );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_screen6, container, false);
        voiceStatus = view.findViewById(R.id.voiceStatus);
        view.findViewById(R.id.micButton).setOnClickListener(v -> startVoiceRecognition());
        return view;
    }

    private void startVoiceRecognition() {
        if (voiceStatus != null) {
            voiceStatus.setText("A l'ecoute...");
        }
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Decrivez l'incident a signaler...");

        try {
            voiceLauncher.launch(intent);
        } catch (Exception e) {
            Log.e(TAG, "Reconnaissance vocale non supportee sur cet appareil.", e);
            promptManualEntry();
        }
    }

    private void promptManualEntry() {
        if (!isAdded()) {
            return;
        }
        if (voiceStatus != null) {
            voiceStatus.setText("Touchez le micro pour parler");
        }
        final EditText input = new EditText(requireContext());
        input.setHint("Ex : Accident sur l'autoroute A8");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        new AlertDialog.Builder(requireContext())
                .setTitle("Decrire l'incident")
                .setMessage("Micro indisponible : saisissez le signalement.")
                .setView(input)
                .setPositiveButton("Creer", (dialog, which) -> {
                    String text = input.getText().toString().trim();
                    if (!text.isEmpty()) {
                        createIssueFromVoice(text);
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void createIssueFromVoice(String spoken) {
        String title = spoken.trim();
        if (title.isEmpty()) {
            return;
        }
        AccidentFactory factory = mentionsHighway(title)
                ? new HighwayFactory()
                : new UrbanFactory();
        Issue newIssue = factory.createIssue(title, "Signalement vocal : " + title);
        notifiable.onDataChange(FRAGMENT_ID, newIssue, 0, newIssue.getSafetyProtocol());
        Toast.makeText(requireContext(), "Signalement vocal cree", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Incident vocal cree : " + newIssue);
    }

    private boolean mentionsHighway(String text) {
        String lower = text.toLowerCase(Locale.FRANCE);
        return lower.contains("autoroute") || lower.matches(".*\\ba\\d+\\b.*");
    }
}
