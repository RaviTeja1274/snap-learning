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
package com.snaplogic.snaps.stringprocessor;

import com.snaplogic.snap.test.harness.OutputRecorder;
import com.snaplogic.snap.test.harness.SnapTestRunner;
import com.snaplogic.snap.test.harness.TestFixture;

import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Tests that the {@link SchemaExample} Snap routes to the output and error views when valid and
 * invalid data, respectively, is passed in as input.
 */
@RunWith(SnapTestRunner.class)
@TestFixture(snap = SchemaExample.class,
        expectedOutputPath = "data/schema_example/expected",
        expectedErrorPath = "data/schema_example/expected")
public class SchemaExampleTest {

    @TestFixture(input = "data/schema_example/schema_valid_input.data")
    public void schemaExample_WithValidData_OutputsDocument() {
    }

    @TestFixture(input = "data/schema_example/schema_invalid_input.data")
    public void schemaExample_WithInvalidData_OutputsErrorDocumentOnly() {
        // Since the input document does not conform to the expected schema,
        // there should be no output.
        // Input document should be forwarded to the error view as it does not
        // match the expected schema.
    }
}
