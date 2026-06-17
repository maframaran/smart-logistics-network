package com.logistics.tests.acceptance;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.*;

// Runs the 5 UI scenarios tagged @ui using Playwright browser automation.
// Requires Docker Compose stack + logistics-ui running on port 3000.
// Execute with: mvn verify -pl acceptance-tests -Dit.test=UiAcceptanceRunner
@Suite
@IncludeEngines("cucumber")
@ConfigurationParameter(
        key = Constants.FEATURES_PROPERTY_NAME,
        value = "specs-documentation/specs/acceptance-tests"
)
@ConfigurationParameter(
        key = Constants.GLUE_PROPERTY_NAME,
        value = "com.logistics.tests.acceptance.stepdefinitions"
)
@ConfigurationParameter(
        key = Constants.FILTER_TAGS_PROPERTY_NAME,
        value = "@ui"
)
@ConfigurationParameter(
        key = Constants.PLUGIN_PROPERTY_NAME,
        value = "pretty, html:target/cucumber-reports/ui.html, json:target/cucumber-reports/ui.json"
)
public class UiAcceptanceRunner {
}
