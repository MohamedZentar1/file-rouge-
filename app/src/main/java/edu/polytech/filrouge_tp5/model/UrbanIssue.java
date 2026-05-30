package edu.polytech.filrouge_tp5.model;

import android.os.Parcel;

public class UrbanIssue extends Issue {

    public UrbanIssue(String title, String description, Priority priority, Status status) {
        super(title, description, priority, status);
    }

    public UrbanIssue(String title, String description, Priority priority, Status status, String location) {
        super(title, description, priority, status, location);
    }

    public UrbanIssue(String title, String description, Priority priority, Status status, String location, double latitude, double longitude) {
        super(title, description, priority, status, location, latitude, longitude);
    }

    protected UrbanIssue(Parcel in) {
        super(in);
    }

    public static final Creator<UrbanIssue> CREATOR = new Creator<UrbanIssue>() {
        @Override
        public UrbanIssue createFromParcel(Parcel in) {
            return new UrbanIssue(in);
        }

        @Override
        public UrbanIssue[] newArray(int size) {
            return new UrbanIssue[size];
        }
    };

    @Override
    public String getSafetyProtocol() {
        return "Securiser la zone, prevenir les secours et eviter de deplacer les blesses.";
    }

    @Override
    public String getContextLabel() {
        return "Urbain";
    }
}
