package edu.polytech.filrouge_tp5.view;

import android.content.Context;

import java.util.List;

import edu.polytech.filrouge_tp5.model.Issue;

public interface ClickableIssue<T extends Issue> {
    void onStatusChange(int itemIndex, Issue.Status status, List<T> items);

    void onClickItem(List<T> items, int itemIndex);

    Context getContext();
}
