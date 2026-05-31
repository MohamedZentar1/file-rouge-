package edu.polytech.filrouge_tp5.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The Model in the MVC pattern.
 * Manages the list of incidents and notifies observers of changes.
 */
public class IssueManager {
    private final List<Issue> issues;
    private final List<IssueObserver> observers = new ArrayList<>();

    public IssueManager(List<Issue> initialIssues) {
        this.issues = initialIssues != null ? initialIssues : new ArrayList<>();
    }

    public List<Issue> getIssues() {
        return issues;
    }

    public void addObserver(IssueObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(IssueObserver observer) {
        observers.remove(observer);
    }

    public void setIssueLocation(int index, double lat, double lon) {
        if (index >= 0 && index < issues.size()) {
            Issue issue = issues.get(index);
            setIssueLocation(issue, lat, lon);
        }
    }

    public void setIssueLocation(Issue issue, double lat, double lon) {
        if (issue != null && issues.contains(issue)) {
            issue.setLocation(lat, lon);
            notifyObservers(issue);
        }
    }

    private void notifyObservers(Issue issue) {
        for (IssueObserver observer : observers) {
            observer.onStatusChanged(issue);
        }
    }
}
