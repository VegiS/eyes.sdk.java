/*
 * Applitools SDK for Selenium integration.
 */
package com.applitools.eyes;

import com.applitools.utils.ArgumentGuard;

/**
 * Encapsulates the data to be sent to the agent on a "matchWindow" command.
 */
class MatchWindowData {
    private Trigger[] userInputs;
    private AppOutput appOutput;
    private String tag;
    private boolean ignoreMismatch;

    /**
     * @param userInputs     A list of triggers between the previous matchWindow
     *                       call and the current matchWindow call. Can be array
     *                       of size 0, but MUST NOT be null.
     * @param appOutput      The appOutput for the current matchWindow call.
     * @param tag            The tag of the window to be matched.
     * @param ignoreMismatch Tells the server whether or not to store
     *                       a mismatch for the current window as window in
     *                       the session.
     */
    public MatchWindowData(Trigger[] userInputs, AppOutput appOutput,
                           String tag, boolean ignoreMismatch) {
        ArgumentGuard.notNull(userInputs, "userInputs");

        this.userInputs = userInputs;
        this.appOutput = appOutput;
        this.tag = tag;
        this.ignoreMismatch = ignoreMismatch;
    }

    public Trigger[] getUserInputs() {
        return userInputs;
    }

    public AppOutput getAppOutput() {
        return appOutput;
    }

    public String getTag() {
        return tag;
    }

    public boolean getIgnoreMismatch() {
        return ignoreMismatch;
    }
}
