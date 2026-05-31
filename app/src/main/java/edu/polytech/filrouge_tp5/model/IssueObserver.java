package edu.polytech.filrouge_tp5.model;

/**
 * Interface representing an observer that reacts to changes in an Issue.
 */
public interface IssueObserver {
    void onStatusChanged(Issue issue);
    void onPriorityChanged(Issue issue);
}
