package edu.polytech.filrouge_tp5.controller;

import android.util.Log;

import org.osmdroid.views.overlay.Marker;

import edu.polytech.filrouge_tp5.model.Issue;
import edu.polytech.filrouge_tp5.model.IssueManager;

/**
 * Controller of the MVC map exercise.
 * It translates marker gestures into updates on the model.
 */
public class IssueController {
    private final String tag = "frallo " + getClass().getSimpleName();
    private final IssueManager model;

    public IssueController(IssueManager model) {
        this.model = model;
    }

    public void controlMarker(Issue issue, Marker marker) {
        marker.setOnMarkerClickListener((clickedMarker, mapView) -> {
            if (clickedMarker.isInfoWindowOpen()) {
                clickedMarker.closeInfoWindow();
            } else {
                clickedMarker.showInfoWindow();
            }
            return true;
        });

        marker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                model.setIssueLocation(issue, marker.getPosition().getLatitude(), marker.getPosition().getLongitude());
                Log.d(tag, "Position updated for " + issue.getTitle());
            }

            @Override
            public void onMarkerDragStart(Marker marker) {
                marker.closeInfoWindow();
            }
        });
    }
}
