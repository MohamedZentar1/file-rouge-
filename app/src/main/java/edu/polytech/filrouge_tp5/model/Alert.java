package edu.polytech.filrouge_tp5.model;

public class Alert {

    public enum Type {
        INFO, CRITICAL
    }

    private final String message;
    private final String issueId;
    private final Type type;

    public Alert(String message, String issueId, Type type) {
        this.message = message;
        this.issueId = issueId;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public String getIssueId() {
        return issueId;
    }

    public Type getType() {
        return type;
    }

    public boolean isCritical() {
        return type == Type.CRITICAL;
    }
}
