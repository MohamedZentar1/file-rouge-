package edu.polytech.filrouge_tp5.view;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputLayout;

import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.Locale;

import edu.polytech.filrouge_tp5.MapConfig;
import edu.polytech.filrouge_tp5.Notifiable;
import edu.polytech.filrouge_tp5.R;
import edu.polytech.filrouge_tp5.factory.AccidentFactory;
import edu.polytech.filrouge_tp5.factory.HighwayFactory;
import edu.polytech.filrouge_tp5.factory.UrbanFactory;
import edu.polytech.filrouge_tp5.model.Issue;

/**
 * ScreenxFragment dedicated to the Abstract Factory and voice-command exercise.
 */
public class Screen3Fragment extends Fragment {
    public static final int FRAGMENT_ID = 2;

    private static final double DEFAULT_LATITUDE = 43.6156;
    private static final double DEFAULT_LONGITUDE = 7.0718;

    private final String TAG = "frallo " + getClass().getSimpleName();
    private Notifiable notifiable;
    private EditText currentTargetEditText;

    private MapView reportMap;
    private Marker reportMarker;
    private TextView gpsHint;
    private double selectedLatitude = DEFAULT_LATITUDE;
    private double selectedLongitude = DEFAULT_LONGITUDE;

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
        MapConfig.configure(requireContext());
        View view = inflater.inflate(R.layout.fragment_screen3, container, false);

        EditText issueTitle = view.findViewById(R.id.title);
        EditText issueDescription = view.findViewById(R.id.et_description);
        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.context);
        View btnSubmit = view.findViewById(R.id.submitIssue);

        TextInputLayout titleInput = view.findViewById(R.id.issueTitle);
        TextInputLayout descriptionInput = view.findViewById(R.id.issueDescription);

        gpsHint = view.findViewById(R.id.gpsHint);
        reportMap = view.findViewById(R.id.reportMap);
        setupReportMap();

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
            newIssue.setLocation(selectedLatitude, selectedLongitude);
            notifiable.onDataChange(FRAGMENT_ID, newIssue, Action.ISSUE_CREATED.ordinal(), newIssue.getSafetyProtocol());
            Toast.makeText(requireContext(), "Signalement cree", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Nouvel incident cree : " + newIssue);
        });

        return view;
    }

    private void setupReportMap() {
        if (reportMap == null) {
            return;
        }
        MapConfig.applyTileSource(reportMap);
        reportMap.setMultiTouchControls(true);
        reportMap.setBuiltInZoomControls(false);
        reportMap.getController().setZoom(15.0);

        GeoPoint start = new GeoPoint(selectedLatitude, selectedLongitude);
        reportMap.getController().setCenter(start);

        reportMarker = new Marker(reportMap);
        reportMarker.setPosition(start);
        reportMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        reportMarker.setTitle("Position de l'incident");
        reportMarker.setDraggable(true);
        reportMarker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                setSelectedPoint(marker.getPosition(), false);
            }

            @Override
            public void onMarkerDragStart(Marker marker) {
            }
        });
        reportMap.getOverlays().add(reportMarker);

        MapEventsOverlay events = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                setSelectedPoint(p, true);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        });
        reportMap.getOverlays().add(events);

        reportMap.setOnTouchListener((v, e) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        updateGpsHint();
        reportMap.invalidate();
    }

    private void setSelectedPoint(GeoPoint point, boolean recenter) {
        selectedLatitude = point.getLatitude();
        selectedLongitude = point.getLongitude();
        if (reportMarker != null) {
            reportMarker.setPosition(point);
        }
        if (recenter && reportMap != null) {
            reportMap.getController().animateTo(point);
        }
        updateGpsHint();
        if (reportMap != null) {
            reportMap.invalidate();
        }
    }

    private void updateGpsHint() {
        if (gpsHint != null) {
            gpsHint.setText(String.format(Locale.FRANCE,
                    "Position : %.4f, %.4f | Horodatage auto",
                    selectedLatitude, selectedLongitude));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (reportMap != null) {
            reportMap.onResume();
        }
    }

    @Override
    public void onPause() {
        if (reportMap != null) {
            reportMap.onPause();
        }
        super.onPause();
    }
}
