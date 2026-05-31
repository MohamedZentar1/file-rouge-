package edu.polytech.filrouge_tp5.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.polytech.filrouge_tp5.R;
import edu.polytech.filrouge_tp5.model.Issue;

public class IssueAdapter<T extends Issue> extends ArrayAdapter<T> {
    private final List<T> items;
    private final LayoutInflater inflater;
    private final ClickableIssue<T> callBackFragment;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.FRANCE);

    public IssueAdapter(@NonNull ClickableIssue<T> callback, List<T> items) {
        super(callback.getContext(), 0, items);
        this.items = items;
        this.callBackFragment = callback;
        inflater = LayoutInflater.from(callback.getContext());
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public T getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View layoutItem = convertView;
        if (layoutItem == null) {
            layoutItem = inflater.inflate(R.layout.item_issue, parent, false);
        }

        TextView name = layoutItem.findViewById(R.id.title);
        ImageView priority = layoutItem.findViewById(R.id.priority);
        TextView brief = layoutItem.findViewById(R.id.issueDescription);
        TextView type = layoutItem.findViewById(R.id.type);
        TextView timestamp = layoutItem.findViewById(R.id.timestamp);
        RatingBar status = layoutItem.findViewById(R.id.status);

        T currentIssue = items.get(position);
        name.setText(currentIssue.getTitle());
        brief.setText(currentIssue.getDescription());
        type.setText(currentIssue.getContextLabel() + " | " + currentIssue.getPriorityLabel());
        timestamp.setText(timeFormat.format(new Date(currentIssue.getTimestamp())));

        switch (currentIssue.getPriority()) {
            case LOW:
                priority.setImageResource(R.drawable.ic_warning_low);
                break;
            case MEDIUM:
                priority.setImageResource(R.drawable.ic_warning_medium);
                break;
            case HIGH:
            case CRITICAL:
            default:
                priority.setImageResource(R.drawable.ic_warning_critical);
                break;
        }

        status.setOnRatingBarChangeListener(null);
        status.setRating(currentIssue.getStatus().getRating());
        status.setOnRatingBarChangeListener((ratingBar, value, fromUser) -> {
            if (fromUser) {
                callBackFragment.onRatingBarChange(position, value, this, items);
            }
        });

        layoutItem.setOnClickListener(clic -> callBackFragment.onClickItem(items, position));
        return layoutItem;
    }
}
