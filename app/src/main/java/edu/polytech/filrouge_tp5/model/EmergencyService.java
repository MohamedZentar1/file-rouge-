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
    private final List<Alert> alerts = new ArrayList<>();

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
        if (issue == null) {
            return;
        }
        String msg = "Statut: [" + issue.getTitle() + "] -> " + issue.getStatusLabel();
        Log.i(TAG, msg);
        addAlert(new Alert(msg, issue.getId(), Alert.Type.INFO));

        if (issue.getStatus() == Issue.Status.CONFIRMED) {
            String alert = "ALERTE: Deploiement autorise pour [" + issue.getTitle() + "]";
            Log.w(TAG, alert);
            addAlert(new Alert(alert, issue.getId(), Alert.Type.CRITICAL));
        }
    }

    @Override
    public void onPriorityChanged(Issue issue) {
        if (issue == null) {
            return;
        }
        String msg = "Priorite: [" + issue.getTitle() + "] -> " + issue.getPriorityLabel();
        Log.i(TAG, msg);
        addAlert(new Alert(msg, issue.getId(), Alert.Type.INFO));
    }

    private synchronized void addAlert(Alert alert) {
        alerts.add(0, alert); // Latest first
        if (alerts.size() > 20) {
            alerts.remove(alerts.size() - 1);
        }
    }

    public synchronized List<Alert> getAlerts() {
        return new ArrayList<>(alerts);
    }
}
