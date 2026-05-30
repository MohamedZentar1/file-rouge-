package edu.polytech.filrouge_tp5;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import edu.polytech.filrouge_tp5.model.Issue;
import edu.polytech.filrouge_tp5.model.IssueCatalog;

public class Screen4Fragment extends Fragment implements OnMapReadyCallback {

    public final static int FRAGMENT_ID = 3;
    private final String TAG = "frallo " + getClass().getSimpleName();

    private Notifiable notifiable;
    private MapView mapView;

    public Screen4Fragment() {
        Log.d(TAG, "screenFragment type 4 created");
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
    public void onStart() {
        super.onStart();
        notifiable.onFragmentDisplayed(FRAGMENT_ID);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_screen4, container, false);
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        ArrayList<Issue> issues = IssueCatalog.createDefaultIssues();
        for (Issue issue : issues) {
            if (issue.getLatitude() == 0.0 && issue.getLongitude() == 0.0) continue;

            LatLng position = new LatLng(issue.getLatitude(), issue.getLongitude());
            float markerColor = getMarkerColor(issue.getPriority());

            googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(issue.getTitle())
                    .snippet(issue.getPriorityLabel() + " · " + issue.getStatusLabel() + " · " + issue.getLocation())
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));
        }

        // Centrer sur la Côte d'Azur pour voir la majorité des incidents
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(43.65, 7.00), 8f));
        googleMap.setInfoWindowAdapter(null);
    }

    private float getMarkerColor(Issue.Priority priority) {
        switch (priority) {
            case CRITICAL: return BitmapDescriptorFactory.HUE_RED;
            case HIGH:     return BitmapDescriptorFactory.HUE_ORANGE;
            case MEDIUM:   return BitmapDescriptorFactory.HUE_YELLOW;
            case LOW:
            default:       return BitmapDescriptorFactory.HUE_GREEN;
        }
    }

    // --- Forwarding du cycle de vie obligatoire pour MapView ---

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mapView != null) mapView.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapView != null) mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }
}
