package com.logistics.tests.acceptance;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.*;

// Runs the 12 backend API scenarios (all features not tagged @ui).
// Requires the full Docker Compose stack to be running (docker compose up -d).
// Execute with: mvn verify -pl acceptance-tests -Dit.test=BackendAcceptanceRunner
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
        value = "not @ui"
)
@ConfigurationParameter(
        key = Constants.PLUGIN_PROPERTY_NAME,
        value = "pretty, html:target/cucumber-reports/backend.html, json:target/cucumber-reports/backend.json"
)
public class BackendAcceptanceRunner {
}
