package edu.polytech.filrouge_tp5;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;

import edu.polytech.filrouge_tp5.controller.IssueController;
import edu.polytech.filrouge_tp5.model.Issue;
import edu.polytech.filrouge_tp5.model.IssueCatalog;
import edu.polytech.filrouge_tp5.model.IssueManager;
import edu.polytech.filrouge_tp5.view.MenuFragment;
import edu.polytech.filrouge_tp5.view.Screen1Fragment;
import edu.polytech.filrouge_tp5.view.Screen2Fragment;
import edu.polytech.filrouge_tp5.view.Screen3Fragment;
import edu.polytech.filrouge_tp5.view.Screen4Fragment;
import edu.polytech.filrouge_tp5.view.Screen5Fragment;
import edu.polytech.filrouge_tp5.view.Screen6Fragment;
import edu.polytech.filrouge_tp5.view.Screen7Fragment;

/**
 * Main controller of SignalRoute.
 */
public class ControlActivity extends AppCompatActivity implements Menuable, Notifiable, Picturable {
    private static final String DATA_IS_STARTING = "sauvegarde";
    private static final String DATA_MENU_NUMBER = "num";
    private static final String DATA_ISSUES = "signalroute_issues";

    /**
     * Maps each visible menu slot (0..n) to the fragment it must open.
     * The detail screen (Screen1Fragment) is intentionally absent: it is only
     * reached by tapping an item in the incident list, never from the menu bar.
     */
    private static final int[] MENU_TO_FRAGMENT = {
            Screen5Fragment.FRAGMENT_ID, // 0 - ic_map      -> Carte (accueil MVC)
            Screen2Fragment.FRAGMENT_ID, // 1 - ic_list     -> Liste des incidents
            Screen3Fragment.FRAGMENT_ID, // 2 - ic_alert    -> Signaler un incident
            Screen6Fragment.FRAGMENT_ID, // 3 - ic_mic      -> Aide vocale
            Screen7Fragment.FRAGMENT_ID  // 4 - ic_settings -> Parametres
    };

    private final String TAG = "frallo " + getClass().getSimpleName();

    private MenuFragment menu;
    private boolean isStarting = true;
    private int menuNumber;
    private ArrayList<Issue> issues = new ArrayList<>();
    private String activeIssueId;
    private IssueManager issueManager;
    private IssueController issueController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        if (savedInstanceState != null) {
            savedInstanceState.setClassLoader(Issue.class.getClassLoader());
            isStarting = savedInstanceState.getBoolean(DATA_IS_STARTING, false);
            menuNumber = savedInstanceState.getInt(DATA_MENU_NUMBER, Screen5Fragment.FRAGMENT_ID);
            activeIssueId = savedInstanceState.getString("active_issue_id");
            ArrayList<Issue> restoredIssues = savedInstanceState.getParcelableArrayList(DATA_ISSUES);
            issues = restoredIssues != null ? restoredIssues : new ArrayList<>();
        } else {
            issues = IssueCatalog.createDefaultIssues();
            menuNumber = Screen5Fragment.FRAGMENT_ID;
            Intent intent = getIntent();
            if (intent != null) {
                menuNumber = intent.getIntExtra(getString(R.string.index), Screen5Fragment.FRAGMENT_ID);
            }
        }
        startMapMvc();

        Bundle args = new Bundle();
        int initialSlot = fragmentIdToMenuSlot(menuNumber);
        args.putInt(getString(R.string.index), initialSlot < 0 ? 0 : initialSlot);

        menu = new MenuFragment();
        menu.setArguments(args);

        Bundle fragmentArgs = null;
        if (menuNumber == Screen1Fragment.FRAGMENT_ID && activeIssueId != null) {
            Issue activeIssue = findIssueById(activeIssueId);
            if (activeIssue != null) {
                fragmentArgs = new Bundle();
                fragmentArgs.putParcelable(getString(R.string.issue), activeIssue);
            }
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_menu, menu);
        transaction.replace(R.id.fragment_main, createFragment(menuNumber, fragmentArgs));
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
                Screen5Fragment mapFragment = Screen5Fragment.newInstance(issues);
                mapFragment.setMvc(issueManager, issueController);
                fragment = mapFragment;
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

    private void startMapMvc() {
        issueManager = IssueManager.getInstance();
        if (issues == null || issues.isEmpty()) {
            issues = issueManager.getIssues().isEmpty()
                    ? IssueCatalog.createDefaultIssues()
                    : issueManager.getIssues();
        }
        if (issues != issueManager.getIssues()) {
            issueManager.replaceIssues(issues);
        }
        issues = issueManager.getIssues();
        issueController = new IssueController(issueManager);
    }

    @Override
    public void onMenuChange(int slot) {
        if (slot < 0 || slot >= MENU_TO_FRAGMENT.length) {
            return;
        }
        int destinationFragmentId = MENU_TO_FRAGMENT[slot];
        if (destinationFragmentId == menuNumber) {
            return;
        }
        changeFragment(destinationFragmentId, null);
    }

    @Override
    public void onFragmentDisplayed(int fragmentId) {
        menuNumber = fragmentId;
        int slot = fragmentIdToMenuSlot(fragmentId);
        // Screens without a menu slot (e.g. the detail screen) leave the
        // current highlight untouched.
        if (slot >= 0 && menu != null) {
            menu.setCurrentActivatedIndex(slot);
        }
    }

    private int fragmentIdToMenuSlot(int fragmentId) {
        for (int slot = 0; slot < MENU_TO_FRAGMENT.length; slot++) {
            if (MENU_TO_FRAGMENT[slot] == fragmentId) {
                return slot;
            }
        }
        return -1;
    }

    @Override
    public void onClick(int numFragment) {
        if (numFragment == Screen1Fragment.FRAGMENT_ID) {
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void onDataChange(int numFragment, Object data, int actionCode, Object argsAction) {
        switch (numFragment) {
            case Screen2Fragment.FRAGMENT_ID:
                handleListAction(data, actionCode, argsAction);
                break;
            case Screen3Fragment.FRAGMENT_ID:
                if (actionCode == Screen3Fragment.Action.ISSUE_CREATED.ordinal() && data instanceof Issue) {
                    Issue issue = (Issue) data;
                    issueController.addIssue(issue);
                    issues = issueManager.getIssues();
                    navigateToDetail(issue);
                }
                break;
            case Screen4Fragment.FRAGMENT_ID:
                if (data instanceof Issue) {
                    navigateToDetail((Issue) data);
                }
                break;
            case Screen6Fragment.FRAGMENT_ID:
                if (data instanceof Issue) {
                    Issue voiceIssue = (Issue) data;
                    issueController.addIssue(voiceIssue);
                    issues = issueManager.getIssues();
                    navigateToDetail(voiceIssue);
                }
                break;
        }
    }

    @Override
    public void onPictureTaken(String path) {
        if (activeIssueId != null) {
            issueController.updateIssuePhoto(activeIssueId, path);
            issues = issueManager.getIssues();
        }
    }

    private void handleListAction(Object data, int actionCode, Object argsAction) {
        if (!(data instanceof Issue)) {
            return;
        }

        Issue selectedIssue = (Issue) data;
        if (actionCode == Screen2Fragment.Action.DISPLAY.ordinal()) {
            navigateToDetail(selectedIssue);
        } else if (actionCode == Screen2Fragment.Action.STATUS_CHANGE.ordinal() && argsAction instanceof Issue.Status) {
            issueController.updateIssueStatus(selectedIssue, (Issue.Status) argsAction);
            issues = issueManager.getIssues();
        }
    }

    private Issue findIssueById(String id) {
        return issueManager != null ? issueManager.findIssueById(id) : null;
    }

    private void navigateToDetail(Issue issue) {
        activeIssueId = issue.getId();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.issue), issue);
        changeFragment(Screen1Fragment.FRAGMENT_ID, args);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(DATA_IS_STARTING, isStarting);
        outState.putInt(DATA_MENU_NUMBER, menuNumber);
        outState.putParcelableArrayList(DATA_ISSUES,
                issueManager != null ? issueManager.snapshotIssues() : issues);
        outState.putString("active_issue_id", activeIssueId);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        savedInstanceState.setClassLoader(Issue.class.getClassLoader());
        isStarting = savedInstanceState.getBoolean(DATA_IS_STARTING);
        menuNumber = savedInstanceState.getInt(DATA_MENU_NUMBER);
        activeIssueId = savedInstanceState.getString("active_issue_id");
        ArrayList<Issue> restoredIssues = savedInstanceState.getParcelableArrayList(DATA_ISSUES);
        if (restoredIssues != null) {
            issues = restoredIssues;
            startMapMvc();
        }
    }
}
