package edu.polytech.filrouge_tp5.view;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import edu.polytech.filrouge_tp5.Notifiable;
import edu.polytech.filrouge_tp5.R;
import edu.polytech.filrouge_tp5.model.Issue;
import edu.polytech.filrouge_tp5.model.IssueManager;
import edu.polytech.filrouge_tp5.model.IssueObserver;

public class Screen1Fragment extends Fragment implements IssueObserver {
    public static final int FRAGMENT_ID = 0;

    private static final String[] STEP_LABELS = {
            "Signale", "Confirme", "Sur place", "Degagement", "Resolu"
    };

    private final String TAG = "frallo " + getClass().getSimpleName();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.FRANCE);

    private Notifiable notifiable;
    private IssueManager model;
    private Issue issue;

    private TextView detailMeta;
    private LinearLayout statusStepper;
    private Button btnStatusPrev;
    private Button btnStatusNext;
    private MapView detailMap;
    private TextView detailCoords;
    private Marker detailMarker;

    public Screen1Fragment() {
        Log.d(TAG, "screenFragment type 1 created");
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = IssueManager.getInstance();
        Issue argumentIssue = readIssueArgument();
        if (argumentIssue != null) {
            Issue stored = model.findIssueById(argumentIssue.getId());
            issue = stored != null ? stored : argumentIssue;
        }
        if (issue != null) {
            model.addObserver(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        edu.polytech.filrouge_tp5.MapConfig.configure(requireContext());
        View view = inflater.inflate(R.layout.fragment_screen1, container, false);

        TextView screenTitle = view.findViewById(R.id.labelScreen1Fragment);
        TextView detailTitle = view.findViewById(R.id.detailTitle);
        TextView content = view.findViewById(R.id.topic);
        TextView protocol = view.findViewById(R.id.detailProtocol);
        TextView photoHint = view.findViewById(R.id.detailPhotoHint);
        ImageView picture = view.findViewById(R.id.picture);

        detailMeta = view.findViewById(R.id.detailMeta);
        statusStepper = view.findViewById(R.id.statusStepper);
        btnStatusPrev = view.findViewById(R.id.btnStatusPrev);
        btnStatusNext = view.findViewById(R.id.btnStatusNext);
        detailMap = view.findViewById(R.id.detailMap);
        detailCoords = view.findViewById(R.id.detailCoords);

        TextView statusSectionTitle = view.findViewById(R.id.statusSectionTitle);
        TextView mapSectionTitle = view.findViewById(R.id.mapSectionTitle);

        screenTitle.setText(getString(R.string.Screen1Fragment_label));

        if (issue != null) {
            screenTitle.setText("Statut de l'incident");
            detailTitle.setText(issue.getTitle());
            content.setText(issue.getDescription());
            protocol.setText("Protocole : " + issue.getSafetyProtocol());
            photoHint.setText(issue.getPhotoPath() == null
                    ? "Photo de l'incident"
                    : "Photo ajoutee a l'incident");
            picture.setImageResource(issue.getPriority() == Issue.Priority.CRITICAL
                    ? R.drawable.ic_warning_critical
                    : R.drawable.ic_alert);

            safe("frise", this::buildStepper);
            safe("carte", this::configureMap);
            safe("statut", this::refreshStatusUi);

            btnStatusPrev.setOnClickListener(v -> shiftStatus(-1));
            btnStatusNext.setOnClickListener(v -> shiftStatus(+1));

            // Integrate CameraFragment
            final Bundle savedState = savedInstanceState;
            safe("camera", () -> {
                if (savedState == null) {
                    getChildFragmentManager().beginTransaction()
                            .replace(R.id.camera_fragment_container, new CameraFragment())
                            .commit();
                }
            });

            // Send existing photo path if any
            if (issue.getPhotoPath() != null) {
                Bundle result = new Bundle();
                result.putString(CameraFragment.BUNDLE_PATH, issue.getPhotoPath());
                getChildFragmentManager().setFragmentResult(CameraFragment.REQUEST_KEY, result);
            }
        } else {
            detailMeta.setText("Selectionnez un incident dans la liste.");
            statusSectionTitle.setVisibility(View.GONE);
            statusStepper.setVisibility(View.GONE);
            btnStatusPrev.setVisibility(View.GONE);
            btnStatusNext.setVisibility(View.GONE);
            mapSectionTitle.setVisibility(View.GONE);
            detailMap.setVisibility(View.GONE);
            detailCoords.setText("Position disponible apres selection d'un incident.");
            protocol.setText("Protocole de securite disponible apres selection.");
            photoHint.setText("Photo disponible apres selection d'un incident.");
        }

        view.findViewById(R.id.button).setOnClickListener(clic -> notifiable.onClick(FRAGMENT_ID));
        return view;
    }

    private void safe(String label, Runnable action) {
        try {
            action.run();
        } catch (Throwable t) {
            Log.e(TAG, "Echec section detail: " + label, t);
            if (isAdded()) {
                android.widget.Toast.makeText(requireContext(),
                        "Erreur (" + label + ") : " + t.getClass().getSimpleName()
                                + " - " + t.getMessage(),
                        android.widget.Toast.LENGTH_LONG).show();
            }
        }
    }

    private void buildStepper() {
        statusStepper.removeAllViews();
        Context context = requireContext();
        Issue.Status[] all = Issue.Status.values();
        for (int i = 0; i < all.length; i++) {
            final Issue.Status target = all[i];

            LinearLayout step = new LinearLayout(context);
            step.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams stepParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            if (i < all.length - 1) {
                stepParams.rightMargin = dp(4);
            }
            step.setLayoutParams(stepParams);

            View bar = new View(context);
            bar.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(8)));
            bar.setBackgroundResource(R.drawable.bg_step_inactive);

            TextView label = new TextView(context);
            label.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            label.setText(STEP_LABELS[i]);
            label.setGravity(Gravity.CENTER);
            label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
            label.setPadding(0, dp(4), 0, 0);

            step.addView(bar);
            step.addView(label);
            step.setOnClickListener(v -> applyStatus(target));

            statusStepper.addView(step);
        }
    }

    private void refreshStatusUi() {
        if (issue == null) {
            return;
        }
        Context context = requireContext();
        int activeColor = ContextCompat.getColor(context, R.color.primary);
        int idleColor = ContextCompat.getColor(context, R.color.text_subtle);
        int currentIndex = issue.getStatus().ordinal();

        for (int i = 0; i < statusStepper.getChildCount(); i++) {
            View step = statusStepper.getChildAt(i);
            if (!(step instanceof LinearLayout)) {
                continue;
            }
            LinearLayout stepLayout = (LinearLayout) step;
            View bar = stepLayout.getChildAt(0);
            TextView label = (TextView) stepLayout.getChildAt(1);

            boolean reached = i <= currentIndex;
            bar.setBackgroundResource(reached
                    ? R.drawable.bg_step_active
                    : R.drawable.bg_step_inactive);
            label.setTextColor(reached ? activeColor : idleColor);
            label.setTypeface(null, i == currentIndex
                    ? android.graphics.Typeface.BOLD
                    : android.graphics.Typeface.NORMAL);
        }

        detailMeta.setText(String.format(Locale.FRANCE, "%s   %s  -  %s",
                issue.getPriority().name(),
                issue.getStatusLabel(),
                timeFormat.format(new Date(issue.getTimestamp()))));

        btnStatusPrev.setEnabled(currentIndex > 0);
        btnStatusPrev.setAlpha(currentIndex > 0 ? 1f : 0.4f);
        boolean hasNext = currentIndex < Issue.Status.values().length - 1;
        btnStatusNext.setEnabled(hasNext);
        btnStatusNext.setAlpha(hasNext ? 1f : 0.4f);
        btnStatusNext.setText(hasNext ? "Faire avancer" : "Incident resolu");
    }

    private void shiftStatus(int delta) {
        if (issue == null) {
            return;
        }
        Issue.Status[] all = Issue.Status.values();
        int next = issue.getStatus().ordinal() + delta;
        if (next < 0 || next >= all.length) {
            return;
        }
        applyStatus(all[next]);
    }

    private void applyStatus(Issue.Status status) {
        if (issue == null || status == issue.getStatus()) {
            return;
        }
        model.setIssueStatus(issue, status);
        if (model.findIssueById(issue.getId()) == null) {
            refreshStatusUi();
        }
    }

    private void configureMap() {
        if (detailMap == null || issue == null) {
            return;
        }
        edu.polytech.filrouge_tp5.MapConfig.applyTileSource(detailMap);
        detailMap.setMultiTouchControls(true);
        detailMap.setBuiltInZoomControls(false);
        detailMap.getController().setZoom(16.5);

        detailMap.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        GeoPoint point = new GeoPoint(issue.getLatitude(), issue.getLongitude());
        detailMap.getController().setCenter(point);

        detailMarker = new Marker(detailMap);
        detailMarker.setPosition(point);
        detailMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        detailMarker.setTitle(issue.getTitle());
        detailMarker.setSnippet(issue.getLocation());
        detailMarker.setDraggable(true);
        detailMarker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                model.setIssueLocation(issue,
                        marker.getPosition().getLatitude(),
                        marker.getPosition().getLongitude());
                updateCoords();
            }

            @Override
            public void onMarkerDragStart(Marker marker) {
                marker.closeInfoWindow();
            }
        });
        detailMap.getOverlays().add(detailMarker);
        detailMap.invalidate();

        updateCoords();
    }

    private void updateCoords() {
        if (detailCoords == null || issue == null) {
            return;
        }
        detailCoords.setText(String.format(Locale.FRANCE, "%s  -  %.4f, %.4f",
                issue.getLocation(), issue.getLatitude(), issue.getLongitude()));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onStatusChanged(Issue changed) {
        refreshIfSameIssue(changed);
    }

    @Override
    public void onPriorityChanged(Issue changed) {
        refreshIfSameIssue(changed);
    }

    private void refreshIfSameIssue(Issue changed) {
        if (isAdded() && issue != null && changed != null
                && issue.getId().equals(changed.getId())) {
            requireActivity().runOnUiThread(() -> {
                if (isAdded()) {
                    refreshStatusUi();
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (detailMap != null) {
            detailMap.onResume();
        }
    }

    @Override
    public void onPause() {
        if (detailMap != null) {
            detailMap.onPause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (model != null) {
            model.removeObserver(this);
        }
        super.onDestroy();
    }

    private Issue readIssueArgument() {
        if (getArguments() == null) {
            return null;
        }
        getArguments().setClassLoader(Issue.class.getClassLoader());
        return getArguments().getParcelable(getString(R.string.issue));
    }
}
