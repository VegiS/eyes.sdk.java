package com.applitools.eyes;

/**
 * Encapsulates a callback which returns an application output.
 */
public interface AppOutputProvider {
    public AppOutputWithScreenshot getAppOutput(
            RegionProvider regionProvider, EyesScreenshot lastScreenshot);
}
