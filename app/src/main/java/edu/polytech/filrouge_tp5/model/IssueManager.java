package edu.polytech.filrouge_tp5.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Singleton model for the MVC/Observable exercise.
 * It owns the incident list and notifies registered views after every update.
 */
public final class IssueManager implements IssueObservable {
    private static IssueManager instance;

    private final ArrayList<Issue> issues = new ArrayList<>();
    private final List<IssueObserver> observers = new ArrayList<>();

    private IssueManager() {
        replaceIssues(IssueCatalog.createDefaultIssues(), false);
    }

    public static synchronized IssueManager getInstance() {
        if (instance == null) {
            instance = new IssueManager();
        }
        return instance;
    }

    /**
     * Returns the shared model list used by adapters/fragments.
     * Mutations should still go through IssueManager so observers are notified.
     */
    public synchronized ArrayList<Issue> getIssues() {
        return issues;
    }

    public synchronized ArrayList<Issue> snapshotIssues() {
        return new ArrayList<>(issues);
    }

    public synchronized void replaceIssues(List<Issue> nextIssues) {
        replaceIssues(nextIssues, true);
    }

    private void replaceIssues(List<Issue> nextIssues, boolean shouldNotify) {
        List<Issue> sourceIssues = nextIssues == null ? new ArrayList<>() : new ArrayList<>(nextIssues);
        issues.clear();
        for (Issue issue : sourceIssues) {
            attachDefaultObservers(issue);
            issues.add(issue);
        }
        if (shouldNotify) {
            notifyObservers(null);
        }
    }

    public synchronized void addIssue(Issue issue) {
        if (issue == null) {
            return;
        }
        attachDefaultObservers(issue);
        issues.add(0, issue);
        notifyObservers(issue);
    }

    public synchronized Issue findIssueById(String id) {
        if (id == null) {
            return null;
        }
        for (Issue issue : issues) {
            if (Objects.equals(issue.getId(), id)) {
                return issue;
            }
        }
        return null;
    }

    public void setIssueStatus(Issue issue, Issue.Status status) {
        Issue storedIssue = findStoredIssue(issue);
        if (storedIssue != null && status != null) {
            storedIssue.setStatus(status);
            notifyObservers(storedIssue);
        }
    }

    public void setIssuePhotoPath(String issueId, String path) {
        Issue issue = findIssueById(issueId);
        if (issue != null) {
            issue.setPhotoPath(path);
            notifyObservers(issue);
        }
    }

    public void setIssueLocation(int index, double lat, double lon) {
        Issue issue = null;
        synchronized (this) {
            if (index >= 0 && index < issues.size()) {
                issue = issues.get(index);
            }
        }
        setIssueLocation(issue, lat, lon);
    }

    public void setIssueLocation(Issue issue, double lat, double lon) {
        Issue storedIssue = findStoredIssue(issue);
        if (storedIssue != null) {
            storedIssue.setLocation(lat, lon);
            notifyObservers(storedIssue);
        }
    }

    @Override
    public synchronized void addObserver(IssueObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public synchronized void removeObserver(IssueObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers() {
        notifyObservers(null);
    }

    @Override
    public void notifyObservers(Issue issue) {
        List<IssueObserver> currentObservers;
        synchronized (this) {
            currentObservers = new ArrayList<>(observers);
        }

        for (IssueObserver observer : currentObservers) {
            observer.onStatusChanged(issue);
            if (issue != null) {
                observer.onPriorityChanged(issue);
            }
        }
    }

    private synchronized Issue findStoredIssue(Issue candidate) {
        if (candidate == null) {
            return null;
        }
        return findIssueById(candidate.getId());
    }

    private void attachDefaultObservers(Issue issue) {
        if (issue != null) {
            issue.addObserver(EmergencyService.getInstance());
        }
    }
}
