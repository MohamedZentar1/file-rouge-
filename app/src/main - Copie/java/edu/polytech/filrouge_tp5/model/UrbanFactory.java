package edu.polytech.filrouge_tp5.model;

public class UrbanFactory implements AccidentFactory {
    @Override
    public Issue createIssue(String title, String description) {
        Issue issue = new UrbanIssue(title, description, Issue.Priority.HIGH, Issue.Status.REPORTED, "Centre-ville - GPS auto", 43.6156, 7.0718);
        issue.addObserver(EmergencyService.getInstance());
        return issue;
    }
}
