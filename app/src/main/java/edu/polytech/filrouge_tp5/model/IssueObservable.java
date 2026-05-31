package edu.polytech.filrouge_tp5.model;

/**
 * Interface for objects that can be observed by IssueObserver.
 */
public interface IssueObservable {
    void addObserver(IssueObserver observer);
    void removeObserver(IssueObserver observer);
    void notifyObservers(Issue issue);
}
