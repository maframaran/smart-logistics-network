package com.logistics.tests.acceptance.runners;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.*;

// Cucumber test runner — points feature files at the specs-documentation source of truth.
// See tests/acceptance/README.md for the rationale on this approach.
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource(".")
@ConfigurationParameter(
        key = Constants.FEATURES_PROPERTY_NAME,
        // Path relative to project root — resolves to specs-documentation/specs/acceptance-tests/
        // Adjust if the Maven project root differs from the working directory.
        value = "specs-documentation/specs/acceptance-tests"
)
@ConfigurationParameter(
        key = Constants.GLUE_PROPERTY_NAME,
        value = "com.logistics.tests.acceptance"
)
@ConfigurationParameter(
        key = Constants.PLUGIN_PROPERTY_NAME,
        value = "pretty, html:target/cucumber-reports/acceptance.html, json:target/cucumber-reports/acceptance.json"
)
public class AcceptanceTestRunner {
    // No body — JUnit Platform Suite runs Cucumber engine
}
