package edu.polytech.filrouge_tp5.model;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton service that centralizes emergency management logic.
 * Reacts to status and priority changes in incidents.
 */
public class EmergencyService implements IssueObserver {
    private static final String TAG = "EmergencyService";
    private static EmergencyService instance;
    private final List<String> alerts = new ArrayList<>();

    private EmergencyService() {
        Log.d(TAG, "EmergencyService initialized");
    }

    public static synchronized EmergencyService getInstance() {
        if (instance == null) {
            instance = new EmergencyService();
        }
        return instance;
    }

    @Override
    public void onStatusChanged(Issue issue) {
        String msg = "STATUS UPDATE: Incident [" + issue.getTitle() + "] is now " + issue.getStatusLabel();
        Log.i(TAG, msg);
        addAlert(msg);

        if (issue.getStatus() == Issue.Status.CONFIRMED) {
            String alert = "ALERT: Deployment authorized for [" + issue.getTitle() + "]";
            Log.w(TAG, alert);
            addAlert(alert);
        }
    }

    @Override
    public void onPriorityChanged(Issue issue) {
        String msg = "PRIORITY UPDATE: Incident [" + issue.getTitle() + "] changed to " + issue.getPriorityLabel();
        Log.i(TAG, msg);
        addAlert(msg);
    }

    private synchronized void addAlert(String message) {
        alerts.add(0, message); // Latest first
        if (alerts.size() > 20) {
            alerts.remove(alerts.size() - 1);
        }
    }

    public synchronized List<String> getAlerts() {
        return new ArrayList<>(alerts);
    }
}
