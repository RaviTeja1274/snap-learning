/*
 * SnapLogic - Data Integration
 *
 * Copyright (C) 2016, SnapLogic, Inc.  All rights reserved.
 *
 * This program is licensed under the terms of
 * the SnapLogic Commercial Subscription agreement.
 *
 * "SnapLogic" is a trademark of SnapLogic, Inc.
 */
package com.snaplogic.snaps.employeegrading;

import com.snaplogic.snap.test.harness.OutputRecorder;
import com.snaplogic.snap.test.harness.SnapTestRunner;
import com.snaplogic.snap.test.harness.TestFixture;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests that the {@link DocGenerator} Snap creates the number of Documents specified by the "count"
 * property.
 */
@RunWith(SnapTestRunner.class)
@TestFixture(snap = DocGenerator.class,
        expectedOutputPath = "data/doc_generator/expected",
        expectedErrorPath = "data/doc_generator/expected")
public class DocGeneratorTest {

    // "properties" is the path to the JSON file representing the Snap's settings
    @TestFixture(properties = "data/doc_generator/doc_generator_properties.json")
    public void docGenerator_WithProperties_OutputsCorrectNumberOfDocuments() {
    }

    // "propertyOverrides" allows overriding specific values defined in the "properties" file
    @TestFixture(properties = "data/doc_generator/doc_generator_properties.json",
            propertyOverrides = {"$.settings.count.value", "20"})
    public void docGenerator_WithPropertyOverrides_OutputsCorrectNumDocuments() {
    }

    /**
     * Tests that the {@link DocGenerator} Snap's output generated for the given value of "count"
     * property matches with what is provided in a separate file referred to by "expectedOutputPath"
     * attribute of the TestFixture annotation.
     */
    @TestFixture(properties = "data/doc_generator/doc_generator_properties.json")
    public void docGenerator_WithExpectedOutputPath_OutputsDocumentsCorrectly() {
    }

    // comparing the run time error generated with the expected error message
    @TestFixture(properties = "data/doc_generator/doc_generator_properties.json",
            propertyOverrides = {"$.settings.count.value", "-2"})
    public void docGenerator_WithExpectedErrorPath_OutputsErrorDocumentCorrectly() {
    }
}