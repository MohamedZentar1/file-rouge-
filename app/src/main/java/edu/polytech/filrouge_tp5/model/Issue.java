package edu.polytech.filrouge_tp5.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.UUID;

/**
 * Base Parcelable contract for every SignalRoute incident.
 *
 * <p>The class is abstract because the factory creates specialized incidents
 * such as {@link UrbanIssue} and {@link HighwayIssue}. Android Parcelable
 * reconstruction is therefore handled by each child CREATOR, not by this base
 * class.</p>
 */
public abstract class Issue implements Parcelable {
    private final String id;
    private final String title;
    private final String description;
    private final long timestamp;
    private final String location;
    private final double latitude;
    private final double longitude;
    private Priority priority;
    private Status status;

    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum Status {
        REPORTED(1.0f),
        CONFIRMED(2.0f),
        ON_SITE(3.0f),
        CLEARING(4.0f),
        RESOLVED(5.0f);

        private final float rating;

        Status(float rating) {
            this.rating = rating;
        }

        public float getRating() {
            return rating;
        }
    }

    public Issue(String title, String description, Priority priority, Status status) {
        this(title, description, priority, status, "GPS auto - SignalRoute", 0.0, 0.0);
    }

    public Issue(String title, String description, Priority priority, Status status, String location) {
        this(title, description, priority, status, location, 0.0, 0.0);
    }

    public Issue(String title, String description, Priority priority, Status status, String location, double latitude, double longitude) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = System.currentTimeMillis();
    }

    protected Issue(Parcel in) {
        id = in.readString();
        title = in.readString();
        description = in.readString();
        timestamp = in.readLong();
        location = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        priority = Priority.valueOf(in.readString());
        status = Status.valueOf(in.readString());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeLong(timestamp);
        dest.writeString(location);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(priority.name());
        dest.writeString(status.name());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getLocation() {
        return location;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Priority getPriority() {
        return priority;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getPriorityLabel() {
        switch (priority) {
            case LOW:
                return "Legere";
            case MEDIUM:
                return "Moderee";
            case HIGH:
                return "Grave";
            case CRITICAL:
            default:
                return "Critique";
        }
    }

    public String getStatusLabel() {
        switch (status) {
            case REPORTED:
                return "Signale";
            case CONFIRMED:
                return "Confirme";
            case ON_SITE:
                return "Secours en route";
            case CLEARING:
                return "Degagement";
            case RESOLVED:
            default:
                return "Resolu";
        }
    }

    public abstract String getSafetyProtocol();

    public abstract String getContextLabel();

    @NonNull
    @Override
    public String toString() {
        return title + " [" + getPriorityLabel() + "] - " + getStatusLabel();
    }
}
