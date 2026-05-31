package edu.polytech.filrouge_tp5;

/**
 * Communication contract from ScreenxFragment instances to ControlActivity.
 */
public interface Notifiable {
    /** Reports a simple click action from a fragment to the activity. */
    void onClick(int numFragment);

    /**
     * Reports a data update or navigation request from a fragment.
     *
     * <p>The fragment provides its id, a payload object, an action code, and
     * optional action arguments. ControlActivity interprets the request.</p>
     */
    void onDataChange(int numFragment, Object object, int actionCode, Object argsAction);

    /**
     * Called when a fragment becomes visible so the activity can synchronize
     * the selected menu item.
     */
    void onFragmentDisplayed(int fragmentId);
}
