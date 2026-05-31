package edu.polytech.filrouge_tp5.factory;

import edu.polytech.filrouge_tp5.model.EmergencyService;
import edu.polytech.filrouge_tp5.model.HighwayIssue;
import edu.polytech.filrouge_tp5.model.Issue;

public class HighwayFactory implements AccidentFactory {
    @Override
    public Issue createIssue(String title, String description) {
        Issue issue = new HighwayIssue(title, description, Issue.Priority.CRITICAL, Issue.Status.REPORTED, "A8 - GPS auto", 43.6210, 7.0750);
        issue.addObserver(EmergencyService.getInstance());
        return issue;
    }
}
