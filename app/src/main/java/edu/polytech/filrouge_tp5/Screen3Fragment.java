package edu.polytech.filrouge_tp5;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import edu.polytech.filrouge_tp5.R;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Locale;

import edu.polytech.filrouge_tp5.model.AccidentFactory;
import edu.polytech.filrouge_tp5.model.HighwayFactory;
import edu.polytech.filrouge_tp5.model.Issue;
import edu.polytech.filrouge_tp5.model.UrbanFactory;

/**
 * ScreenxFragment dedicated to the Abstract Factory and voice-command exercise.
 */
public class Screen3Fragment extends Fragment {
    public static final int FRAGMENT_ID = 2;

    private final String TAG = "frallo " + getClass().getSimpleName();
    private Notifiable notifiable;
    private EditText currentTargetEditText;

    public enum Action {
        ISSUE_CREATED
    }

    public Screen3Fragment() {
        Log.d(TAG, "screenFragment type 3 created");
    }

    /**
     * Called when this ScreenxFragment is visible. It lets the activity update
     * the active menu entry for the report/factory screen.
     */
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
                    ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty() && currentTargetEditText != null) {
                        currentTargetEditText.setText(matches.get(0));
                    }
                }
            }
    );

    private void startVoiceRecognition(EditText target) {
        currentTargetEditText = target;
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez pour remplir le champ...");

        try {
            voiceLauncher.launch(intent);
        } catch (Exception e) {
            Log.e(TAG, "Reconnaissance vocale non supportee sur cet appareil.", e);
            Toast.makeText(requireContext(), "Reconnaissance vocale non disponible", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_screen3, container, false);

        EditText issueTitle = view.findViewById(R.id.title);
        EditText issueDescription = view.findViewById(R.id.et_description);
        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.context);
        View btnSubmit = view.findViewById(R.id.submitIssue);

        TextInputLayout titleInput = view.findViewById(R.id.issueTitle);
        TextInputLayout descriptionInput = view.findViewById(R.id.issueDescription);

        titleInput.setEndIconOnClickListener(v -> startVoiceRecognition(issueTitle));
        descriptionInput.setEndIconOnClickListener(v -> startVoiceRecognition(issueDescription));

        btnSubmit.setOnClickListener(clic -> {
            String title = issueTitle.getText().toString().trim();
            String description = issueDescription.getText().toString().trim();

            if (title.isEmpty()) {
                titleInput.setError("Titre obligatoire");
                return;
            }
            titleInput.setError(null);

            if (description.isEmpty()) {
                descriptionInput.setError("Description obligatoire");
                return;
            }
            descriptionInput.setError(null);

            AccidentFactory factory = toggleGroup.getCheckedButtonId() == R.id.btn_highway
                    ? new HighwayFactory()
                    : new UrbanFactory();
            Issue newIssue = factory.createIssue(title, description);
            notifiable.onDataChange(FRAGMENT_ID, newIssue, Action.ISSUE_CREATED.ordinal(), newIssue.getSafetyProtocol());
            Toast.makeText(requireContext(), "Signalement cree", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Nouvel incident cree : " + newIssue);
        });

        return view;
    }
}
