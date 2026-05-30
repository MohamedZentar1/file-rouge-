package edu.polytech.filrouge_tp5.model;

@Deprecated
public interface IssueFactory extends AccidentFactory {
    default Issue build(String title, String description) {
        return createIssue(title, description);
    }
}
