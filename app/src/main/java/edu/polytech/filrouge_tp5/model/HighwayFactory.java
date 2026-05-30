package edu.polytech.filrouge_tp5.model;

public class HighwayFactory implements AccidentFactory {
    @Override
    public Issue createIssue(String title, String description) {
        return new HighwayIssue(title, description, Issue.Priority.CRITICAL, Issue.Status.REPORTED, "A8 - GPS auto");
    }
}
