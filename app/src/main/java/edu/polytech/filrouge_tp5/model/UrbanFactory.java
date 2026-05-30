package edu.polytech.filrouge_tp5.model;

public class UrbanFactory implements AccidentFactory {
    @Override
    public Issue createIssue(String title, String description) {
        return new UrbanIssue(title, description, Issue.Priority.HIGH, Issue.Status.REPORTED, "Centre-ville - GPS auto");
    }
}
