package edu.polytech.filrouge_tp5.view;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import edu.polytech.filrouge_tp5.Notifiable;
import edu.polytech.filrouge_tp5.R;
import edu.polytech.filrouge_tp5.model.Issue;
import edu.polytech.filrouge_tp5.model.IssueCatalog;
import edu.polytech.filrouge_tp5.model.IssueManager;
import edu.polytech.filrouge_tp5.model.IssueObserver;

/**
 * ScreenxFragment dedicated to the Adapter pattern.
 *
 * <p>It displays SignalRoute incidents with a custom {@link IssueAdapter}. User
 * actions are reported to {@link ControlActivity} through {@link Notifiable};
 * this fragment never opens Screen1Fragment by itself.</p>
 */
public class Screen2Fragment extends Fragment implements ClickableIssue<Issue>, IssueObserver {
    public static final int FRAGMENT_ID = 1;
    private static final String ARG_ISSUES = "signalroute_issues";

    private final String TAG = "frallo " + getClass().getSimpleName();
    private Notifiable notifiable;
    private ArrayList<Issue> issues = new ArrayList<>();
    private IssueManager model;
    private IssueAdapter<Issue> adapter;

    public enum Action {
        DISPLAY, STATUS_CHANGE
    }

    public static Screen2Fragment newInstance(ArrayList<Issue> issues) {
        Screen2Fragment fragment = new Screen2Fragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_ISSUES, issues);
        fragment.setArguments(args);
        return fragment;
    }

    public Screen2Fragment() {
        Log.d(TAG, "screenFragment type 2 created");
    }

    /**
     * Called when this ScreenxFragment enters the Started state. The fragment
     * tells the activity it is visible so the menu can highlight the Adapter
     * screen.
     */
    @Override
    public void onStart() {
        super.onStart();
        notifiable.onFragmentDisplayed(FRAGMENT_ID);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (requireActivity() instanceof Notifiable) {
            notifiable = (Notifiable) requireActivity();
        } else {
            throw new AssertionError("Classe " + requireActivity().getClass().getName() + " ne met pas en oeuvre Notifiable.");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = IssueManager.getInstance();
        if (getArguments() != null) {
            getArguments().setClassLoader(Issue.class.getClassLoader());
            ArrayList<Issue> argumentIssues = getArguments().getParcelableArrayList(ARG_ISSUES);
            if (argumentIssues != null && model.getIssues().isEmpty()) {
                model.replaceIssues(argumentIssues);
            }
        }
        if (model.getIssues().isEmpty()) {
            model.replaceIssues(IssueCatalog.createDefaultIssues());
        }
        issues = model.getIssues();
        model.addObserver(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_screen2, container, false);
        ListView list = view.findViewById(R.id.list_item);
        adapter = new IssueAdapter<>(this, issues);
        list.setAdapter(adapter);
        return view;
    }

    @Override
    public void onRatingBarChange(int itemIndex, float value, IssueAdapter<Issue> adapter, List<Issue> items) {
        Issue issue = items.get(itemIndex);
        notifiable.onDataChange(FRAGMENT_ID, issue, Action.STATUS_CHANGE.ordinal(), value);
    }

    @Override
    public void onClickItem(List<Issue> items, int itemIndex) {
        notifiable.onDataChange(FRAGMENT_ID, items.get(itemIndex), Action.DISPLAY.ordinal(), null);
    }

    private Issue.Status statusFromRating(float value) {
        int index = Math.round(value) - 1;
        if (index < 0) {
            index = 0;
        }
        if (index >= Issue.Status.values().length) {
            index = Issue.Status.values().length - 1;
        }
        return Issue.Status.values()[index];
    }

    @Override
    public void onStatusChanged(Issue issue) {
        refreshList();
    }

    @Override
    public void onPriorityChanged(Issue issue) {
        refreshList();
    }

    private void refreshList() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        if (model != null) {
            model.removeObserver(this);
        }
        super.onDestroy();
    }
}
