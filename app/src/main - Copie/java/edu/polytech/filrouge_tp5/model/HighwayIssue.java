package edu.polytech.filrouge_tp5.model;

import android.os.Parcel;

public class HighwayIssue extends Issue {

    public HighwayIssue(String title, String description, Priority priority, Status status) {
        super(title, description, priority, status);
    }

    public HighwayIssue(String title, String description, Priority priority, Status status, String location, double lat, double lon) {
        super(title, description, priority, status, location, lat, lon);
    }

    protected HighwayIssue(Parcel in) {
        super(in);
    }

    public static final Creator<HighwayIssue> CREATOR = new Creator<HighwayIssue>() {
        @Override
        public HighwayIssue createFromParcel(Parcel in) {
            return new HighwayIssue(in);
        }

        @Override
        public HighwayIssue[] newArray(int size) {
            return new HighwayIssue[size];
        }
    };

    @Override
    public String getSafetyProtocol() {
        return "Rester derriere la glissiere de securite et appeler les secours.";
    }

    @Override
    public String getContextLabel() {
        return "Autoroute";
    }
}
