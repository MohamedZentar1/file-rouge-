package edu.polytech.filrouge_tp5;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;

import edu.polytech.filrouge_tp5.R;
import edu.polytech.filrouge_tp5.model.Issue;
import edu.polytech.filrouge_tp5.model.IssueCatalog;

/**
 * Main controller of SignalRoute.
 *
 * <p>The activity owns the navigation and shared incident list. Fragments never
 * manipulate each other directly: they notify this activity through
 * {@link Notifiable}, then the activity decides which fragment to display and
 * which data to pass through a Bundle.</p>
 *
 * <p>ScreenxFragment is the teaching pattern used by the correction: each
 * ScreenNFragment represents one independent application screen. Its role is to
 * display a view, capture user events, and report those events to the Activity
 * through interfaces instead of knowing another fragment.</p>
 */
public class ControlActivity extends AppCompatActivity implements Menuable, Notifiable {
    private static final String DATA_IS_STARTING = "sauvegarde";
    private static final String DATA_MENU_NUMBER = "num";
    private static final String DATA_ISSUES = "signalroute_issues";

    private final String TAG = "frallo " + getClass().getSimpleName();

    private MenuFragment menu;

    /**
     * True only while the initial screen is being installed.
     *
     * <p>It prevents the first fragment transaction from being added to the
     * BackStack. After the first display, user navigation is added to the
     * BackStack so the physical Back button returns to the previous fragment
     * instead of closing the app immediately.</p>
     */
    private boolean isStarting = true;

    private int menuNumber;
    private ArrayList<Issue> issues = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        if (savedInstanceState != null) {
            savedInstanceState.setClassLoader(Issue.class.getClassLoader());
            isStarting = savedInstanceState.getBoolean(DATA_IS_STARTING, false);
            menuNumber = savedInstanceState.getInt(DATA_MENU_NUMBER, Screen4Fragment.FRAGMENT_ID);
            ArrayList<Issue> restoredIssues = savedInstanceState.getParcelableArrayList(DATA_ISSUES);
            issues = restoredIssues != null ? restoredIssues : IssueCatalog.createDefaultIssues();
        } else {
            issues = IssueCatalog.createDefaultIssues();
            menuNumber = Screen4Fragment.FRAGMENT_ID;
            Intent intent = getIntent();
            if (intent != null) {
                menuNumber = intent.getIntExtra(getString(R.string.index), Screen4Fragment.FRAGMENT_ID);
            }
        }

        Bundle args = new Bundle();
        args.putInt(getString(R.string.index), menuNumber);

        menu = new MenuFragment();
        menu.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_menu, menu);
        transaction.replace(R.id.fragment_main, createFragment(menuNumber, null));
        transaction.commit();
        isStarting = false;
    }

    private Fragment createFragment(int destinationIndex, Bundle args) {
        Fragment fragment;
        switch (destinationIndex) {
            case Screen1Fragment.FRAGMENT_ID:
                fragment = new Screen1Fragment();
                break;
            case Screen2Fragment.FRAGMENT_ID:
                fragment = Screen2Fragment.newInstance(issues);
                break;
            case Screen3Fragment.FRAGMENT_ID:
                fragment = new Screen3Fragment();
                break;
            case Screen4Fragment.FRAGMENT_ID:
                fragment = new Screen4Fragment();
                break;
            case Screen5Fragment.FRAGMENT_ID:
                fragment = new Screen5Fragment();
                break;
            case Screen6Fragment.FRAGMENT_ID:
                fragment = new Screen6Fragment();
                break;
            case Screen7Fragment.FRAGMENT_ID:
                fragment = new Screen7Fragment();
                break;
            default:
                Log.e(TAG, "Unknown fragment index " + destinationIndex);
                fragment = new Screen4Fragment();
                break;
        }

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    private void changeFragment(int destinationIndex, Bundle args) {
        menuNumber = destinationIndex;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_main, createFragment(destinationIndex, args));

        if (!isStarting) {
            transaction.addToBackStack(null);
        } else {
            isStarting = false;
        }
        transaction.commit();
    }

    @Override
    public void onMenuChange(int index) {
        Log.d(TAG, "Menu changed to " + index);
        if (index == menuNumber) {
            return;
        }
        changeFragment(index, null);
    }

    /**
     * Called from a ScreenxFragment when it becomes visible.
     *
     * <p>The activity uses it to synchronize the active menu icon with the
     * fragment currently displayed, including after BackStack navigation.</p>
     */
    @Override
    public void onFragmentDisplayed(int fragmentId) {
        Log.d(TAG, "onFragmentDisplayed ==>" + fragmentId);
        if (menuNumber != fragmentId && menu != null) {
            menuNumber = fragmentId;
            menu.setCurrentActivatedIndex(menuNumber);
        }
    }

    @Override
    public void onClick(int numFragment) {
        Log.d(TAG, "Fragment " + numFragment + " clicked.");
        if (numFragment == Screen1Fragment.FRAGMENT_ID) {
            getSupportFragmentManager().popBackStack();
        }
    }

    /**
     * Receives data or actions emitted by fragments.
     *
     * <p>For example, Screen2 sends a selected Issue to request detail display,
     * while Screen3 sends a newly created Issue built by the selected factory.
     * This keeps fragment communication routed through the activity.</p>
     */
    @Override
    public void onDataChange(int numFragment, Object data, int actionCode, Object argsAction) {
        switch (numFragment) {
            case Screen2Fragment.FRAGMENT_ID:
                handleListAction(data, actionCode, argsAction);
                break;
            case Screen3Fragment.FRAGMENT_ID:
                if (actionCode == Screen3Fragment.Action.ISSUE_CREATED.ordinal() && data instanceof Issue) {
                    Issue issue = (Issue) data;
                    issues.add(0, issue);
                    navigateToDetail(issue);
                }
                break;
            default:
                Log.d(TAG, "Unhandled data change from fragment " + numFragment);
                break;
        }
    }

    private void handleListAction(Object data, int actionCode, Object argsAction) {
        if (!(data instanceof Issue)) {
            return;
        }

        Issue selectedIssue = (Issue) data;
        if (actionCode == Screen2Fragment.Action.DISPLAY.ordinal()) {
            navigateToDetail(selectedIssue);
        } else if (actionCode == Screen2Fragment.Action.STATUS_CHANGE.ordinal() && argsAction instanceof Float) {
            Issue.Status newStatus = statusFromRating((Float) argsAction);
            selectedIssue.setStatus(newStatus);
            updateStoredIssueStatus(selectedIssue);
        }
    }

    private void navigateToDetail(Issue issue) {
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.issue), issue);
        changeFragment(Screen1Fragment.FRAGMENT_ID, args);
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

    private void updateStoredIssueStatus(Issue changedIssue) {
        for (Issue issue : issues) {
            if (issue.getId().equals(changedIssue.getId())) {
                issue.setStatus(changedIssue.getStatus());
                return;
            }
        }
    }

    /**
     * Called before Android may destroy/recreate the Activity, for example
     * during rotation or process pressure. It saves navigation flags and the
     * current incident list.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(DATA_IS_STARTING, isStarting);
        outState.putInt(DATA_MENU_NUMBER, menuNumber);
        outState.putParcelableArrayList(DATA_ISSUES, issues);
    }

    /**
     * Called after Android recreated the Activity with a saved Bundle. It
     * restores values saved by {@link #onSaveInstanceState(Bundle)}.
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        savedInstanceState.setClassLoader(Issue.class.getClassLoader());
        isStarting = savedInstanceState.getBoolean(DATA_IS_STARTING);
        menuNumber = savedInstanceState.getInt(DATA_MENU_NUMBER);
        ArrayList<Issue> restoredIssues = savedInstanceState.getParcelableArrayList(DATA_ISSUES);
        if (restoredIssues != null) {
            issues = restoredIssues;
        }
    }
}
