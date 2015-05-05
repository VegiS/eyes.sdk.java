/*
 * Applitools SDK for Selenium integration.
 */
package com.applitools.eyes;

class AppOutputWithScreenshot {
    private final AppOutput appOutput;
    private final EyesScreenshot screenshot;

    public AppOutputWithScreenshot(AppOutput appOutput,
            EyesScreenshot screenshot) {
        this.appOutput = appOutput;
        this.screenshot = screenshot;
    }

    public AppOutput getAppOutput() {
        return appOutput;
    }

    public EyesScreenshot getScreenshot() {
        return screenshot;
    }
}