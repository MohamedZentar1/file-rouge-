package edu.polytech.filrouge_tp5.view;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

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
    private final ArrayList<Issue> displayed = new ArrayList<>();
    private IssueManager model;
    private IssueAdapter<Issue> adapter;

    private Issue.Status statusFilter;
    private Issue.Priority priorityFilter;

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
        adapter = new IssueAdapter<>(this, displayed);
        list.setAdapter(adapter);

        setupFilters(view);
        applyFilter();
        return view;
    }

    private void setupFilters(View view) {
        Spinner statusSpinner = view.findViewById(R.id.filterStatus);
        Spinner prioritySpinner = view.findViewById(R.id.filterPriority);

        ArrayList<String> statusLabels = new ArrayList<>();
        statusLabels.add("Tous");
        for (String label : IssueAdapter.STATUS_LABELS) {
            statusLabels.add(label);
        }
        statusSpinner.setAdapter(buildSpinnerAdapter(statusLabels));

        ArrayList<String> priorityLabels = new ArrayList<>();
        priorityLabels.add("Toutes");
        for (Issue.Priority p : Issue.Priority.values()) {
            priorityLabels.add(p.name());
        }
        prioritySpinner.setAdapter(buildSpinnerAdapter(priorityLabels));

        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                statusFilter = pos == 0 ? null : Issue.Status.values()[pos - 1];
                applyFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        prioritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                priorityFilter = pos == 0 ? null : Issue.Priority.values()[pos - 1];
                applyFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private ArrayAdapter<String> buildSpinnerAdapter(List<String> labels) {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, labels);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return spinnerAdapter;
    }

    private void applyFilter() {
        displayed.clear();
        for (Issue issue : issues) {
            boolean statusOk = statusFilter == null || issue.getStatus() == statusFilter;
            boolean priorityOk = priorityFilter == null || issue.getPriority() == priorityFilter;
            if (statusOk && priorityOk) {
                displayed.add(issue);
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStatusChange(int itemIndex, Issue.Status status, List<Issue> items) {
        Issue issue = items.get(itemIndex);
        notifiable.onDataChange(FRAGMENT_ID, issue, Action.STATUS_CHANGE.ordinal(), status);
    }

    @Override
    public void onClickItem(List<Issue> items, int itemIndex) {
        notifiable.onDataChange(FRAGMENT_ID, items.get(itemIndex), Action.DISPLAY.ordinal(), null);
    }

    @Override
    public void onStatusChanged(Issue issue) {
        applyFilter();
    }

    @Override
    public void onPriorityChanged(Issue issue) {
        applyFilter();
    }

    @Override
    public void onDestroy() {
        if (model != null) {
            model.removeObserver(this);
        }
        super.onDestroy();
    }
}
