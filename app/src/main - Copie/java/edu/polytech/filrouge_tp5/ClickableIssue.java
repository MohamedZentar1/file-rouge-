package edu.polytech.filrouge_tp5;

import android.content.Context;

import java.util.List;

import edu.polytech.filrouge_tp5.model.Issue;

public interface ClickableIssue<T extends Issue> {
   void onRatingBarChange(int itemIndex, float value, IssueAdapter<T> adapter, List<T> items);
    void onClickItem(List<T> items, int itemIndex);
    Context getContext();
}
