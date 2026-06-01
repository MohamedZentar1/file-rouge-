package edu.polytech.filrouge_tp5.view;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.polytech.filrouge_tp5.Notifiable;
import edu.polytech.filrouge_tp5.R;
import edu.polytech.filrouge_tp5.controller.IssueController;
import edu.polytech.filrouge_tp5.model.Issue;
import edu.polytech.filrouge_tp5.model.IssueManager;
import edu.polytech.filrouge_tp5.model.IssueObserver;

/**
 * View of the MVC exercise: the same incidents are shown as map markers and
 * as a list filtered by the current map viewport.
 */
public class Screen5Fragment extends Fragment implements IssueObserver {
    public static final int FRAGMENT_ID = 4;
    private static final String ARG_ISSUES = "issues";
    private static final double DEFAULT_LATITUDE = 43.6156;
    private static final double DEFAULT_LONGITUDE = 7.0718;
    private static final double DEFAULT_ZOOM = 15.5;

    private final String tag = "frallo " + getClass().getSimpleName();
    private final List<Issue> visibleIssues = new ArrayList<>();

    private Notifiable notifiable;
    private MapView mapView;
    private ListView listView;
    private IssueManager model;
    private IssueController controller;
    private MapIssueAdapter adapter;
    private boolean mapListenerRegistered;

    public Screen5Fragment() {
        Log.d(tag, "screenFragment type 5 created");
    }

    public static Screen5Fragment newInstance(ArrayList<Issue> issues) {
        Screen5Fragment fragment = new Screen5Fragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_ISSUES, issues);
        fragment.setArguments(args);
        return fragment;
    }

    public void setMvc(IssueManager model, IssueController controller) {
        this.model = model;
        this.controller = controller;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ensureMvcReady();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        edu.polytech.filrouge_tp5.MapConfig.configure(requireContext());
        View view = inflater.inflate(R.layout.fragment_screen5, container, false);
        listView = view.findViewById(R.id.listIssues);
        mapView = view.findViewById(R.id.mapView);

        adapter = new MapIssueAdapter(requireContext(), visibleIssues);
        listView.setAdapter(adapter);

        configureMap();
        renderMarkers();
        mapView.post(this::updateListView);

        model.addObserver(this);

        return view;
    }

    private void ensureMvcReady() {
        if (model != null && controller != null) {
            return;
        }

        ArrayList<Issue> initialIssues = new ArrayList<>();
        Bundle arguments = getArguments();
        if (arguments != null) {
            arguments.setClassLoader(Issue.class.getClassLoader());
            ArrayList<Issue> argumentIssues = arguments.getParcelableArrayList(ARG_ISSUES);
            if (argumentIssues != null) {
                initialIssues = argumentIssues;
            }
        }

        model = IssueManager.getInstance();
        if (model.getIssues().isEmpty() && !initialIssues.isEmpty()) {
            model.replaceIssues(initialIssues);
        }
        controller = new IssueController(model);
    }

    private void configureMap() {
        if (mapView == null) {
            return;
        }

        edu.polytech.filrouge_tp5.MapConfig.applyTileSource(mapView);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.setMinZoomLevel(3.0);
        mapView.setMaxZoomLevel(20.0);
        mapView.getController().setZoom(DEFAULT_ZOOM);
        mapView.getController().setCenter(getInitialCenter());

        if (!mapListenerRegistered) {
            mapView.addMapListener(new MapListener() {
                @Override
                public boolean onScroll(ScrollEvent event) {
                    updateListView();
                    return true;
                }

                @Override
                public boolean onZoom(ZoomEvent event) {
                    updateListView();
                    return true;
                }
            });
            mapListenerRegistered = true;
        }
    }

    private GeoPoint getInitialCenter() {
        if (model != null && !model.getIssues().isEmpty()) {
            Issue issue = model.getIssues().get(0);
            return new GeoPoint(issue.getLatitude(), issue.getLongitude());
        }
        return new GeoPoint(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
    }

    private void renderMarkers() {
        if (mapView == null || model == null || controller == null) {
            return;
        }

        mapView.getOverlays().clear();

        for (Issue issue : model.getIssues()) {
            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(issue.getLatitude(), issue.getLongitude()));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(issue.getTitle());
            marker.setSnippet(String.format(Locale.FRANCE, "%s - %s",
                    issue.getDescription(), issue.getLocation()));
            marker.setDraggable(true);
            controller.controlMarker(issue, marker);
            mapView.getOverlays().add(marker);
        }

        mapView.invalidate();
    }

    private void updateListView() {
        if (adapter == null || model == null) {
            return;
        }

        visibleIssues.clear();
        for (Issue issue : model.getIssues()) {
            if (isVisibleOnMap(issue)) {
                visibleIssues.add(issue);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private boolean isVisibleOnMap(Issue issue) {
        if (mapView == null) {
            return true;
        }

        try {
            BoundingBox bounds = mapView.getBoundingBox();
            return bounds == null || bounds.contains(issue.getLatitude(), issue.getLongitude());
        } catch (IllegalArgumentException ignored) {
            return true;
        }
    }

    @Override
    public void onStatusChanged(Issue issue) {
        renderMarkers();
        updateListView();
    }

    @Override
    public void onPriorityChanged(Issue issue) {
        updateListView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        if (mapView != null) {
            mapView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        if (model != null) {
            model.removeObserver(this);
        }
        mapView = null;
        adapter = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (model != null) {
            model.removeObserver(this);
        }
        super.onDestroy();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (requireActivity() instanceof Notifiable) {
            notifiable = (Notifiable) requireActivity();
        } else {
            throw new AssertionError("Classe " + requireActivity().getClass().getName() + " ne met pas en oeuvre Notifiable.");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (notifiable != null) {
            notifiable.onFragmentDisplayed(FRAGMENT_ID);
        }
    }

    /**
     * Renders each visible incident as a styled card row (priority icon, title,
     * status badge and coordinates) instead of a plain text line.
     */
    private static class MapIssueAdapter extends ArrayAdapter<Issue> {
        private final LayoutInflater inflater;

        MapIssueAdapter(Context context, List<Issue> items) {
            super(context, 0, items);
            this.inflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View row = convertView;
            if (row == null) {
                row = inflater.inflate(R.layout.item_map_issue, parent, false);
            }

            Issue issue = getItem(position);
            ImageView priority = row.findViewById(R.id.mapIssuePriority);
            TextView title = row.findViewById(R.id.mapIssueTitle);
            TextView status = row.findViewById(R.id.mapIssueStatus);
            TextView coords = row.findViewById(R.id.mapIssueCoords);

            if (issue == null) {
                return row;
            }

            title.setText(issue.getTitle());
            status.setText(issue.getStatusLabel());
            coords.setText(String.format(Locale.FRANCE, "%.4f, %.4f",
                    issue.getLatitude(), issue.getLongitude()));

            switch (issue.getPriority()) {
                case LOW:
                    priority.setImageResource(R.drawable.ic_warning_low);
                    break;
                case MEDIUM:
                    priority.setImageResource(R.drawable.ic_warning_medium);
                    break;
                case HIGH:
                case CRITICAL:
                default:
                    priority.setImageResource(R.drawable.ic_warning_critical);
                    break;
            }
            return row;
        }
    }
}
