package edu.polytech.filrouge_tp5.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Base Parcelable contract for every SignalRoute incident.
 *
 * <p>The class is abstract because the factory creates specialized incidents
 * such as {@link UrbanIssue} and {@link HighwayIssue}. Android Parcelable
 * reconstruction is therefore handled by each child CREATOR, not by this base
 * class.</p>
 */
public abstract class Issue implements Parcelable, IssueObservable {
    private final String id;
    private final String title;
    private final String description;
    private final long timestamp;
    private final String location;
    private double latitude;
    private double longitude;
    private String photoPath;
    private Priority priority;
    private Status status;
    private transient List<IssueObserver> observers = new ArrayList<>();

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
        this(title, description, priority, status, "GPS auto - SignalRoute", 43.6156, 7.0718);
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
        this.photoPath = null;
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
        photoPath = in.readString();
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
        dest.writeString(photoPath);
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

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
        notifyObservers();
    }

    public void setLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Priority getPriority() {
        return priority;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        if (this.status != status) {
            this.status = status;
            notifyObservers();
        }
    }

    public void setPriority(Priority priority) {
        if (this.priority != priority) {
            this.priority = priority;
            // Note: If you want specific notification for priority, notifyObservers() can handle it
            // or we could add a notifyPriorityChanged() if needed by the interface.
            notifyObservers();
        }
    }

    @Override
    public void addObserver(IssueObserver observer) {
        if (observers == null) {
            observers = new ArrayList<>();
        }
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(IssueObserver observer) {
        if (observers != null) {
            observers.remove(observer);
        }
    }

    public void notifyObservers() {
        notifyObservers(this);
    }

    @Override
    public void notifyObservers(Issue issue) {
        if (observers != null && issue != null) {
            for (IssueObserver observer : observers) {
                observer.onStatusChanged(issue);
                observer.onPriorityChanged(issue);
            }
        }
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
