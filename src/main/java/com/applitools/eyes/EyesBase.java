package com.applitools.eyes;

import com.applitools.utils.ArgumentGuard;
import com.applitools.utils.ImageDeltaCompressor;
import com.applitools.utils.ImageUtils;
import org.apache.commons.codec.binary.Base64;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Applitools Eyes Base for Java API .
 */
abstract class EyesBase {

    private static final int DEFAULT_MATCH_TIMEOUT = 2; // Seconds
    protected static final int USE_DEFAULT_TIMEOUT = -1;

    public static final String DEFAULT_CHARSET_NAME = "UTF-8";

    private boolean shouldMatchWindowRunOnceOnTimeout;

    protected ServerConnector serverConnector;
    private MatchWindowTask matchWindowTask;
    protected RunningSession runningSession;
    protected SessionStartInfo sessionStartInfo;
    protected RectangleSize viewportSize;
    protected EyesScreenshot lastScreenshot;

    // Will be checked <b>before</b> any argument validation. If true,
    // all method will immediately return without performing any action.
    private boolean isDisabled;

    protected Logger logger;
    private boolean isOpen;
    private String agentId;
    private String appName;
    private String testName;
    private ImageMatchSettings defaultMatchSettings;
    private int matchTimeout;
    private BatchInfo batch;
    private String hostApp;
    private String hostOS;
    private String baselineName;
    private String branchName;
    private String parentBranchName;
    private FailureReports failureReports;
    private final Queue<Trigger> userInputs;

    // Used for automatic save of a test run.
    private boolean saveNewTests, saveFailedTests;

    /**
     * Creates a new {@code EyesBase}instance that interacts with the Eyes
     * Server at the specified url.
     *
     * @param serverUrl  The Eyes server URL.
     */
    public EyesBase(URI serverUrl) {

        if (isDisabled) {
            userInputs = null;
            return;
        }

        ArgumentGuard.notNull(serverUrl, "serverUrl");

        logger = new Logger();
        viewportSize = null;
        serverConnector = new ServerConnector(logger, getBaseAgentId(),
                serverUrl);
        matchTimeout = DEFAULT_MATCH_TIMEOUT;
        runningSession = null;
        defaultMatchSettings = new ImageMatchSettings();
        failureReports = FailureReports.ON_CLOSE;
        userInputs = new ArrayDeque<Trigger>();

        // New tests are automatically saved by default.
        saveNewTests = true;
        saveFailedTests = false;
        agentId = null;
        lastScreenshot = null;
    }

    @SuppressWarnings("UnusedDeclaration")
    /**
     * Sets the user given agent id of the SDK. {@code null} is referred to
     * as no id.
     *
     * @param agentId The agent ID to set.
     */
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    /**
     * @return The user given agent id of the SDK.
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * Sets the API key of your applitools Eyes account.
     *
     * @param apiKey The api key to set.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setApiKey(String apiKey) {
        ArgumentGuard.notNull(apiKey, "apiKey");
        serverConnector.setApiKey(apiKey);
    }

    /**
     * @return The currently set API key or {@code null} if no key is set.
     */
    public String getApiKey() {
        return serverConnector.getApiKey();
    }


    /**
     * Sets the current server URL used by the rest client.
     * @param serverUrl The URI of the rest server, or {@code null} to use
     *                  the default server.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setServerUrl(URI serverUrl) {
        if (serverUrl == null) {
            serverConnector.setServerUrl(getDefaultServerUrl());
        } else {
            serverConnector.setServerUrl(serverUrl);
        }
    }

    /**
     *
     * @return The URI of the eyes server.
     */
    @SuppressWarnings("UnusedDeclaration")
    public URI getServerUrl() {
        return serverConnector.getServerUrl();
    }

    /**
     * Sets the proxy settings to be used by the rest client.
     * @param proxySettings The proxy settings to be used by the rest client.
     * If {@code null} then no proxy is set.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setProxy(ProxySettings proxySettings) {
        serverConnector.setProxy(proxySettings);
    }

    /**
     *
     * @return The current proxy settings used by the server connector,
     * or {@code null} if no proxy is set.
     */
    @SuppressWarnings("UnusedDeclaration")
    public ProxySettings getProxy() {
        return serverConnector.getProxy();
    }

    /**
     *
     * @param isDisabled If true, all interactions with this API will be
     *                   silently ignored.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setIsDisabled(boolean isDisabled) {
        this.isDisabled = isDisabled;
    }

    /**
     * @return Whether eyes is disabled.
     */
    @SuppressWarnings("UnusedDeclaration")
    public boolean getIsDisabled() {
        return isDisabled;
    }

    /**
     * Sets the branch in which the baseline for subsequent test runs resides.
     * If the branch does not already exist it will be created under the
     * specified parent branch (see {@link #setParentBranchName}).
     * Changes to the baseline or model of a branch do not propagate to other
     * branches.
     *
     * @param branchName Branch name or {@code null} to specify the default
     *                   branch.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    /**
     *
     * @return The current branch (see {@link #setBranchName(String)}).
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getBranchName() {
        return branchName;
    }

    /**
     * Sets the branch under which new branches are created. (see {@link
     * #setBranchName(String)}.
     *
     * @param branchName Branch name or {@code null} to specify the default
     *                   branch.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setParentBranchName(String branchName) {
        this.parentBranchName = branchName;
    }

    /**
     *
     * @return The name of the current parent branch under which new branches
     * will be created. (see {@link #setParentBranchName(String)}).
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getParentBranchName() {
        return parentBranchName;
    }

    /**
     * Clears the user inputs list.
     */
    protected void clearUserInputs() {
        if (isDisabled) {
            return;
        }
        userInputs.clear();
    }

    /**
     * @return User inputs collected between {@code checkWindowBase}
     * invocations.
     */
    protected Trigger[] getUserInputs() {
        if (isDisabled) {
            return null;
        }
        Trigger[] result = new Trigger[userInputs.size()];
        return userInputs.toArray(result);
    }

    /**
     * Sets the maximal time (in seconds) a match operation tries to perform
     * a match.
     *
     * @param seconds Total number of seconds to wait for a match.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setMatchTimeout(int seconds) {
        if (getIsDisabled()) {
            logger.verbose("setMatchTimeout(): Ignored");
            return;
        }

        logger.verbose("setMatchTimeout(" + seconds + ")");
        ArgumentGuard.greaterThanOrEqualToZero(seconds, "seconds");

        this.matchTimeout = seconds;

        logger.log("Match timeout set to " + seconds + " second(s)");
    }

    /**
     * @return The maximum time in seconds {@link #checkWindowBase
     * (RegionProvider, String, boolean, int)} waits for a match.
     */
    @SuppressWarnings("UnusedDeclaration")
    public int getMatchTimeout() {
        return matchTimeout;
    }

    /**
     * Set whether or not new tests are saved by default.
     *
     * @param saveNewTests True if new tests should be saved by default.
     *                     False otherwise.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setSaveNewTests(boolean saveNewTests) {
        this.saveNewTests = saveNewTests;
    }

    /**
     * @return True if new tests are saved by default.
     */
    @SuppressWarnings("UnusedDeclaration")
    public boolean getSaveNewTests() {
        return saveNewTests;
    }

    /**
     * Set whether or not failed tests are saved by default.
     *
     * @param saveFailedTests True if failed tests should be saved by
     *                        default, false otherwise.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setSaveFailedTests(boolean saveFailedTests) {
        this.saveFailedTests = saveFailedTests;
    }

    /**
     * @return True if failed tests are saved by default.
     */
    @SuppressWarnings("UnusedDeclaration")
    public boolean getSaveFailedTests() {
        return saveFailedTests;
    }

    /**
     * Sets the batch in which context future tests will run or {@code null}
     * if tests are to run standalone.
     *
     * @param batch The batch info to set.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setBatch(BatchInfo batch) {
        if (isDisabled) {
            logger.verbose("setBatch(): Ignored");
            return;
        }

        logger.verbose("setBatch(" + batch + ")");

        this.batch = batch;
    }

    /**
     * @return The currently set batch info.
     */
    @SuppressWarnings("UnusedDeclaration")
    public BatchInfo getBatch() {
        return batch;
    }

    /**
     * @param failureReports The failure reports setting.
     * @see com.applitools.eyes.FailureReports
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setFailureReports(FailureReports failureReports) {
        this.failureReports = failureReports;
    }

    /**
     * @return the failure reports setting.
     */
    @SuppressWarnings("UnusedDeclaration")
    public FailureReports getFailureReports() {
        return failureReports;
    }

    @SuppressWarnings("UnusedDeclaration")
    /**
     * Updates the match settings to be used for the session.
     *
     * @param defaultMatchSettings The match settings to be used for the
     *                             session.
     */
    public void setDefaultMatchSettings(ImageMatchSettings
                                                defaultMatchSettings) {
        ArgumentGuard.notNull(defaultMatchSettings, "defaultMatchSettings");
        this.defaultMatchSettings = defaultMatchSettings;
    }

    @SuppressWarnings("UnusedDeclaration")
    /**
     *
     * @return The match settings used for the session.
     */
    public ImageMatchSettings getDefaultMatchSettings() {
        return defaultMatchSettings;
    }

    /**
     * This function is deprecated. Please use {@link
     * #setDefaultMatchSettings} instead.
     *
     * The test-wide match level to use when checking application screenshot
     * with the expected output.
     *
     * @param matchLevel The match level setting.
     * @see com.applitools.eyes.MatchLevel
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setMatchLevel(MatchLevel matchLevel) {
        this.defaultMatchSettings.setMatchLevel(matchLevel);
    }

    /**
     * This function is deprecated. Please use {@link
     * #getDefaultMatchSettings} instead.
     * @return The test-wide match level.
     */
    @SuppressWarnings("UnusedDeclaration")
    public MatchLevel getMatchLevel() {
        return defaultMatchSettings.getMatchLevel();
    }

    /**
     * @return The base agent id of the SDK.
     */
    protected abstract String getBaseAgentId();

    /**
     * @return The full agent id composed of both the base agent id and the
     * user given agent id.
     */
    protected String getFullAgentId() {
        String agentId = getAgentId();
        if (agentId == null) {
            return getBaseAgentId();
        }
        return String.format("%s [%s]", agentId, getBaseAgentId());
    }

    /**
     * @return Whether a session is open.
     */
    @SuppressWarnings("UnusedDeclaration")
    public boolean getIsOpen() {
        return isOpen;
    }

    public static URI getDefaultServerUrl() {
        try {
            return new URI("https://eyessdk.applitools.com");
        } catch (URISyntaxException ex) {
            throw new EyesException(ex.getMessage(), ex);
        }
    }

    /**
     * Sets a handler of log messages generated by this API.
     *
     * @param logHandler Handles log messages generated by this API.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setLogHandler(LogHandler logHandler) {
        logger.setLogHandler(logHandler);
    }

    /**
     * @return The currently set log handler.
     */
    @SuppressWarnings("UnusedDeclaration")
    public LogHandler getLogHandler() {
        return logger.getLogHandler();
    }

    /**
     * @see #close(boolean) .
     * throwEx defaults to {@code true}.
     *
     * @return The test results.
     */
    public TestResults close() {
        return close(true);
    }

    /**
     * Ends the test.
     *
     * @param throwEx If true, an exception will be thrown for failed/new tests.
     * @return The test results.
     * @throws TestFailedException if a mismatch was found and throwEx is true.
     * @throws NewTestException    if this is a new test was found and throwEx
     *                             is true.
     */
    public TestResults close(boolean throwEx) {
        try {
            if (isDisabled) {
                logger.verbose("close(): Ignored");
                return null;
            }
            logger.verbose("close()");
            ArgumentGuard.isValidState(isOpen, "Eyes not open");

            isOpen = false;

            lastScreenshot = null;
            clearUserInputs();

            if (runningSession == null) {
                logger.verbose("close(): Server session was not started");
                logger.log("--- Empty test ended.");
                return new TestResults();
            }

            boolean isNewSession = runningSession.getIsNewSession();
            String sessionResultsUrl = runningSession.getUrl();

            logger.verbose("close(): Ending server session...");
            boolean save = (isNewSession && saveNewTests)
                    || (!isNewSession && saveFailedTests);
            logger.verbose("Automatically save test? " + String.valueOf(save));
            TestResults results =
                    serverConnector.stopSession(runningSession, false,
                            save);

            results.setNew(isNewSession);
            results.setUrl(sessionResultsUrl);
            logger.verbose("close(): " + results);

            String instructions;
            if (!isNewSession &&
                    (0 < results.getMismatches() || 0 < results.getMissing())) {

                logger.log("--- Failed test ended. See details at "
                        + sessionResultsUrl);

                if (throwEx) {
                    String message =
                            "'" + sessionStartInfo.getScenarioIdOrName()
                                    + "' of '" + sessionStartInfo
                                    .getAppIdOrName()
                                    + "'. See details at " + sessionResultsUrl;
                    throw new TestFailedException(results, message);
                }
                return results;
            }

            if (isNewSession) {
                instructions = "Please approve the new baseline at "
                        + sessionResultsUrl;

                logger.log("--- New test ended. " + instructions);

                if (throwEx) {
                    String message =
                            "'" + sessionStartInfo.getScenarioIdOrName()
                                    + "' of '" + sessionStartInfo
                                    .getAppIdOrName()
                                    + "'. " + instructions;
                    throw new NewTestException(results, message);
                }
                return results;
            }

            // Test passed
            logger.log("--- Test passed. See details at " + sessionResultsUrl);

            return results;
        } finally {
            // Making sure that we reset the running session even if an
            // exception was thrown during close.
            runningSession = null;
            logger.getLogHandler().close();
        }
    }

    /**
     * If a test is running, aborts it. Otherwise, does nothing.
     */
    public void abortIfNotClosed() {
        try {
            if (isDisabled) {
                logger.verbose("abortIfNotClosed(): Ignored");
                return;
            }

            isOpen = false;

            lastScreenshot = null;
            clearUserInputs();

            if (null == runningSession) {
                logger.verbose("abortIfNotClosed(): closed");
                return;
            }

            logger.verbose("abortIfNotClosed(): Aborting server session...");
            try {
                // When aborting we do not save the test.
                serverConnector.stopSession(runningSession, true, false);
                logger.log("--- Test aborted.");
            } catch (EyesException ex) {
                logger.log(
                        "Failed to abort server session: " + ex.getMessage());
            }
        } finally {
            runningSession = null;
            logger.getLogHandler().close();
        }
    }

    /**
     * @param hostOS The host OS running the AUT.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setHostOS(String hostOS) {

        logger.log("Host OS: " + hostOS);

        if(hostOS == null || hostOS.isEmpty()) {
            this.hostOS = null;
        }
        else {
            this.hostOS = hostOS.trim();
        }
    }

    /**
     * @return get the host OS running the AUT.
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getHostOS() {
        return hostOS;
    }

    /**
     * @param hostApp The application running the AUT (e.g., Chrome).
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setHostApp(String hostApp) {

        logger.log("Host App: " + hostApp);

        if (hostApp == null || hostApp.isEmpty()) {
            this.hostApp = null;
        } else {
            this.hostApp = hostApp.trim();
        }
    }

    /**
     * @return The application name running the AUT.
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getHostApp() {
        return hostApp;
    }

    /**
     * @param baselineName If specified, determines the baseline to compare
     *                     with and disables automatic baseline inference.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setBaselineName(String baselineName) {

        logger.log("Baseline name: " + baselineName);

        if(baselineName == null || baselineName.isEmpty()) {
            this.baselineName = null;
        }
        else {
            this.baselineName = baselineName.trim();
        }
    }

    /**
     * @return The baseline name, if specified.
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getBaselineName() {
        return baselineName;
    }

    /**
     * Superseded by {@link #setHostOS(String)} and {@link #setHostApp
     * (String)}.
     * Sets the OS (e.g., Windows) and application (e.g., Chrome) that host the
     * application under test.
     *
     * @param hostOS  The name of the OS hosting the application under test or
     *                {@code null} to auto-detect.
     * @param hostApp The name of the application hosting the application under
     *                test or {@code null} to auto-detect.
     */
    @Deprecated
    @SuppressWarnings("UnusedDeclaration")
    public void setAppEnvironment(String hostOS, String hostApp) {
        if (isDisabled) {
            logger.verbose("setAppEnvironment(): Ignored");
            return;
        }

        logger.log("Warning: SetAppEnvironment is deprecated! Please use " +
                "'setHostOS' and 'setHostApp'");

        logger.verbose("setAppEnvironment(" + hostOS + ", " + hostApp + ")");
        setHostOS(hostOS);
        setHostApp(hostApp);
    }

    @SuppressWarnings("UnusedDeclaration")
    /**
     * @see #checkWindowBase(RegionProvider, String, boolean, int) .
     * {@code retryTimeout} defaults to USE_DEFAULT_TIMEOUT.
     *
     * @param regionProvider Returns the region to check or the empty
     *                       rectangle to check the entire window.
     * @param tag An optional tag to be associated with the snapshot.
     * @param ignoreMismatch Whether to ignore this check if a mismatch is
     *                       found.
     * @return The result of matching the output with the expected output.
     */
    protected MatchResult checkWindowBase(RegionProvider regionProvider,
                                          String tag, boolean ignoreMismatch) {
        return checkWindowBase(regionProvider, tag, ignoreMismatch,
                USE_DEFAULT_TIMEOUT);
    }

    /**
     * Takes a snapshot of the application under test and matches it with the
     * expected output.
     *
     * @param regionProvider      Returns the region to check or the empty rectangle
     *                            to check the entire window.
     * @param tag                 An optional tag to be associated with the snapshot.
     * @param ignoreMismatch      Whether to ignore this check if a mismatch is
     *                            found.
     * @param retryTimeout        The amount of time to retry matching in milliseconds
     *                            or a negative value to use the default retry timeout.
     * @return The result of matching the output with the expected output.
     * @throws com.applitools.eyes.TestFailedException Thrown if a mismatch is
     *                                                 detected and immediate failure reports are enabled.
     */
    protected MatchResult checkWindowBase(RegionProvider regionProvider,
                                          String tag, boolean ignoreMismatch,
                                          int retryTimeout) {

        MatchResult result;

        if (getIsDisabled()) {
            logger.verbose("CheckWindowBase(): Ignored");
            result = new MatchResult();
            result.setAsExpected(true);
            return result;
        }

        ArgumentGuard.isValidState(getIsOpen(), "Eyes not open");
        ArgumentGuard.notNull(regionProvider, "regionProvider");

        logger.verbose(String.format(
                "CheckWindowBase(regionProvider, '%s', %b, %d)",
                tag, ignoreMismatch, retryTimeout));

        if (tag == null) {
            tag = "";
        }

        if (runningSession == null) {
            logger.verbose("No running session, calling start session..");
            startSession();
            logger.verbose("Done!");

            matchWindowTask = new MatchWindowTask(
                    logger,
                    serverConnector,
                    runningSession,
                    matchTimeout,
                    // A callback which will call getAppOutput
                    new AppOutputProvider() {
                        public AppOutputWithScreenshot getAppOutput(
                                RegionProvider regionProvider_,
                                EyesScreenshot lastScreenshot_) {

                            return getAppOutputWithScreenshot(
                                    regionProvider_, lastScreenshot_);
                        }
                    }
            );
        }

        logger.verbose("Calling match window...");
        result = matchWindowTask.matchWindow(getUserInputs(), lastScreenshot,
                regionProvider, tag,
                shouldMatchWindowRunOnceOnTimeout, ignoreMismatch,
                retryTimeout);
        logger.verbose("MatchWindow Done!");

        if (!result.getAsExpected()) {
            if (!ignoreMismatch) {
                clearUserInputs();
                lastScreenshot = result.getScreenshot();
            }

            shouldMatchWindowRunOnceOnTimeout = true;

            if (!runningSession.getIsNewSession()) {
                logger.log(String.format("Mismatch! (%s)", tag));
            }

            if (getFailureReports() == FailureReports.IMMEDIATE) {
                throw new TestFailedException(String.format(
                        "Mismatch found in '%s' of '%s'",
                        sessionStartInfo.getScenarioIdOrName(),
                        sessionStartInfo.getAppIdOrName()));
            }
        } else { // Match successful
            clearUserInputs();
            lastScreenshot = result.getScreenshot();
        }

        logger.verbose("CheckWindowBase Done!");
        return result;
    }

    /**
     * Starts a test.
     *
     * @param appName        The name of the application under test.
     * @param testName       The test name.
     * @param viewportSize   The client's viewport size (i.e., the visible part
     *                       of the document's body) or {@code null} to allow
     *                       any viewport size.
     */
    public void openBase(String appName, String testName,
                     RectangleSize viewportSize) {

        logger.getLogHandler().open();

        try {
            if (isDisabled) {
                logger.verbose("open(): Ignored");
                return;
            }

            ArgumentGuard.notNull(appName, "appName");
            ArgumentGuard.notNull(testName, "testName");

            logger.log("Agent = " + getFullAgentId());
            logger.verbose(String.format("open('%s', '%s', '%s')", appName,
                    testName, viewportSize));

            if (getApiKey() == null) {
                String errMsg =
                        "API key is missing! Please set it using setApiKey()";
                logger.log(errMsg);
                throw new EyesException(errMsg);
            }

            logger.log(String.format("Eyes server URL is '%s'",
                    serverConnector.getServerUrlBase()));
            logger.verbose(String.format("Timeout = '%d'",
                    serverConnector.getTimeout()));
            logger.log(String.format("matchTimeout = '%d' ", matchTimeout));
            logger.log(String.format("Default match settings = '%s' ",
                    defaultMatchSettings));
            logger.log(String.format("FailureReports = '%s' ", failureReports));


            if (isOpen) {
                abortIfNotClosed();
                String errMsg = "A test is already running";
                logger.log(errMsg);
                throw new EyesException(errMsg);
            }

            this.appName = appName;
            this.testName = testName;
            this.viewportSize = viewportSize;
            isOpen = true;

        } catch (EyesException e) {
            logger.log(String.format("open(): %s", e.getMessage()));
            logger.getLogHandler().close();
            throw e;
        }
    }

    /**
     * @return The viewport size of the AUT.
     */
    protected abstract RectangleSize getViewportSize();

    /**
     * @param size The viewport size of the AUT.
     */
    protected abstract void setViewportSize(RectangleSize size);

    /**
     * @return The inferred environment string
     * or {@code null} if none is available. The inferred string is in the
     * format "source:info" where source is either "useragent" or "pos".
     * Information associated with a "useragent" source is a valid browser user
     * agent string. Information associated with a "pos" source is a string of
     * the format "process-name;os-name" where "process-name" is the name of the
     * main module of the executed process and "os-name" is the OS name.
     */
    protected abstract String getInferredEnvironment();

    /**
     * @return An updated screenshot.
     */
    protected abstract EyesScreenshot getScreenshot();

    /**
     * @return The current title of of the AUT.
     */
    protected abstract String getTitle();


    // FIXME add "GetScreenshotUrl"
    // FIXME add CloseOrAbort ??

    /**
     * Adds a trigger to the current list of user inputs.
     *
     * @param trigger The trigger to add to the user inputs list.
     */
    protected void addUserInput(Trigger trigger) {
        if (isDisabled) {
            return;
        }
        ArgumentGuard.notNull(trigger, "trigger");
        userInputs.add(trigger);
    }

    /**
     * Adds a text trigger.
     *
     * @param control The control's position relative to the window.
     * @param text    The trigger's text.
     */
    protected void addTextTriggerBase(Region control, String text) {
        if (getIsDisabled()) {
            logger.verbose(String.format(
                    "AddTextTrigger: Ignoring '%s' (disabled)", text));
            return;
        }

        ArgumentGuard.notNull(control, "control");
        ArgumentGuard.notNull(text, "text");

        // We don't want to change the objects we received.
        control = new Region(control);

        if (lastScreenshot == null) {
            logger.verbose(String.format(
                    "AddTextTrigger: Ignoring '%s' (no screenshot)", text));
            return;
        }

        control = lastScreenshot.getIntersectedRegion(control,
                CoordinatesType.CONTEXT_RELATIVE,
                CoordinatesType.SCREENSHOT_AS_IS);
        if (control.isEmpty()) {
            logger.verbose(String.format(
                    "AddTextTrigger: Ignoring '%s' (out of bounds)", text));
            return;
        }

        Trigger trigger = new TextTrigger(control, text);
        addUserInput(trigger);

        logger.verbose(String.format("AddTextTrigger: Added %s", trigger));
    }

    /**
     * Adds a mouse trigger.
     *
     * @param action  Mouse action.
     * @param control The control on which the trigger is activated
     *                (location is relative to the window).
     * @param cursor  The cursor's position relative to the control.
     */
    protected void addMouseTriggerBase(MouseAction action, Region control,
                                   Location cursor) {
        if (getIsDisabled()) {
            logger.verbose(String.format(
                    "AddMouseTrigger: Ignoring %s (disabled)",
                    action));
            return;
        }

        ArgumentGuard.notNull(action, "action");
        ArgumentGuard.notNull(control, "control");
        ArgumentGuard.notNull(cursor, "cursor");

        // Triggers are actually performed on the previous window.
        if (lastScreenshot == null) {
            logger.verbose(String.format(
                    "AddMouseTrigger: Ignoring %s (no screenshot)",
                    action));
            return;
        }

        // Getting the location of the cursor in the screenshot
        Location cursorInScreenshot = new Location(cursor);
        // First we need to getting the cursor's coordinates relative to the
        // context (and not to the control).
        cursorInScreenshot.offset(control.getLocation());
        try {
            cursorInScreenshot = lastScreenshot.getLocationInScreenshot(
                    cursorInScreenshot, CoordinatesType.CONTEXT_RELATIVE);
        } catch (OutOfBoundsException e) {
            logger.verbose(String.format(
                    "AddMouseTrigger: Ignoring %s (out of bounds)",
                    action));
            return;
        }

        Region controlScreenshotIntersect =
                lastScreenshot.getIntersectedRegion(control,
                        CoordinatesType.CONTEXT_RELATIVE,
                        CoordinatesType.SCREENSHOT_AS_IS);

        // If the region is NOT empty, we'll give the coordinates relative to
        // the control.
        if (!controlScreenshotIntersect.isEmpty()) {
            Location l = controlScreenshotIntersect.getLocation();
            cursorInScreenshot.offset(-l.getX(), -l.getY());
        }

        Trigger trigger = new MouseTrigger(action, controlScreenshotIntersect,
                cursorInScreenshot);
        addUserInput(trigger);

        logger.verbose(String.format("AddMouseTrigger: Added %s", trigger));
    }

    // FIXME add getScreenshot (Wrapper) ?? (Check EyesBase in .NET)

    /**
     * Application environment is the environment (e.g., the host OS) which
     * runs the application under test.
     * @return The current application environment.
     */
    protected AppEnvironment getAppEnvironment() {

        AppEnvironment appEnv = new AppEnvironment();

        // If hostOS isn't set, we'll try and extract and OS ourselves.
        if (hostOS != null) {
            appEnv.setOs(hostOS);
        }

        if (hostApp != null) {
            appEnv.setHostingApp(hostApp);
        }

        appEnv.setInferred(getInferredEnvironment());
        appEnv.setDisplaySize(viewportSize);
        return appEnv;
    }

    /**
     * Start eyes session on the eyes server.
     */
    protected void startSession() {
        logger.verbose("startSession()");

        if (viewportSize == null) {
            viewportSize = getViewportSize();
        } else {
            setViewportSize(viewportSize);
        }

        BatchInfo testBatch;
        if (batch == null) {
            logger.verbose("startSession(): No batch set");
            testBatch = new BatchInfo(null);
        } else {
            logger.verbose("startSession(): Batch is " + batch);
            testBatch = batch;
        }

        AppEnvironment appEnv = getAppEnvironment();
        logger.verbose("startSession(): Application environment is " + appEnv);

        sessionStartInfo = new SessionStartInfo(getBaseAgentId(), appName, null,
                testName, testBatch, baselineName, appEnv, defaultMatchSettings,
                branchName,
                parentBranchName);

        logger.verbose("startSession(): Starting server session...");
        runningSession = serverConnector.startSession(sessionStartInfo);

        logger.verbose("startSession(): Server session ID is "
                + runningSession.getId());

        String testInfo = "'" + testName + "' of '" + appName + "' " + appEnv;
        if (runningSession.getIsNewSession()) {
            logger.log("--- New test started - " + testInfo);
            shouldMatchWindowRunOnceOnTimeout = true;
        } else {
            logger.log("--- Test started - " + testInfo);
            shouldMatchWindowRunOnceOnTimeout = false;
        }
    }

    /**
     * @param regionProvider      A callback for getting the region of the screenshot
     *                            which will be set in the application output.
     * @param lastScreenshot      Previous application screenshot (used for
     *                            compression) or {@code null} if not available.
     * @return The updated app output and screenshot.
     */
    private AppOutputWithScreenshot getAppOutputWithScreenshot(
            RegionProvider regionProvider, EyesScreenshot lastScreenshot) {

        logger.verbose("getAppOutputWithScreenshot()");

        logger.verbose("getting screenshot...");
        // Getting the screenshot (abstract function implemented by each SDK).
        EyesScreenshot screenshot = getScreenshot();
        logger.verbose("Done getting screenshot!");

        // Cropping by region if necessary
        Region region = regionProvider.getRegion();
        if (!region.isEmpty()) {
            screenshot = screenshot.getSubScreenshot(region,
                    regionProvider.getCoordinatesType(), false);
        }

        logger.verbose("Compreesing screenshot...");
        String compressResult =
                compressScreenshot64(screenshot, lastScreenshot);
        logger.verbose("Done! Getting title...");
        String title = getTitle();
        logger.verbose("Done!");
        AppOutputWithScreenshot result = new AppOutputWithScreenshot(
                new AppOutput(title, compressResult), screenshot);
        logger.verbose("getAppOutputWithScreenshot Done!");
        return result;
    }

    /**
     * Compresses a given screenshot.
     *
     * @param screenshot     The screenshot to compress.
     * @param lastScreenshot The previous screenshot, or null.
     * @return A base64 encoded compressed screenshot.
     */
    private String compressScreenshot64(EyesScreenshot screenshot,
                                        EyesScreenshot lastScreenshot) {

        ArgumentGuard.notNull(screenshot, "screenshot");

        BufferedImage screenshotImage = screenshot.getImage();
        byte[] uncompressed =
                ImageUtils.encodeAsPng(screenshotImage);

        BufferedImage source = (lastScreenshot != null) ?
                lastScreenshot.getImage() : null;

        // Compressing the screenshot
        byte[] compressedScreenshot;
        try {
            compressedScreenshot =
                    ImageDeltaCompressor.compressByRawBlocks(
                            screenshotImage, uncompressed,
                            source);
        } catch (IOException e) {
            throw new EyesException("Failed to compress screenshot!", e);
        }

        return Base64.encodeBase64String(compressedScreenshot);
    }
}
