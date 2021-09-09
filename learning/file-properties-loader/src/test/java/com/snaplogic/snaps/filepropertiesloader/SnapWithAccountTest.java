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
package com.snaplogic.snaps.filepropertiesloader;

import com.snaplogic.snap.test.harness.OutputRecorder;
import com.snaplogic.snap.test.harness.SnapTestRunner;
import com.snaplogic.snap.test.harness.TestFixture;
import com.snaplogic.snap.test.harness.TestResult;
import com.snaplogic.snap.test.harness.TestSetup;

import org.junit.runner.RunWith;

import java.util.Map;

/**
 * Tests that the {@link SnapWithAccount} Snap uses its associated {@link ExampleAccount} to build
 * and output a hash-token.
 */
@RunWith(SnapTestRunner.class)
public class SnapWithAccountTest {

    // "account" defines the Account type to use
    // "accountProperties" defines the Account settings
    @TestFixture(snap = SnapWithAccount.class,
            account = ExampleAccount.class,
            accountProperties = "data/snap_with_account/account_properties.json",
            expectedOutputPath = "data/snap_with_account/expected",
            expectedErrorPath = "data/snap_with_account/expected")
    public void snapWithAccount_WithAccountProperties_OutputsCorrectToken(TestSetup testSetup)
            throws Exception {
        // inject in the stub instance to fix the expiration timestamp
        testSetup.inject().fieldName("snapAccount").dependency(new StubExampleAccount()).add();
        TestResult testResult = testSetup.test();
    }

    /**
     * Create a stub of the {@link ExampleAccount} so a fixed expiration timestamp can be used for
     * testing
     */
    public class StubExampleAccount extends ExampleAccount {
        @Override
        protected long getExpirationTimestamp() {
            return 1277968800000L;
        }
    }

}
