package edu.polytech.filrouge_tp5.factory;

import edu.polytech.filrouge_tp5.model.Issue;

public interface AccidentFactory {
    Issue createIssue(String title, String description);
}
