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
package com.snaplogic.snaps.stringprocessor.string;

import com.google.inject.Inject;
import com.snaplogic.api.ConfigurationException;
import com.snaplogic.api.ExecutionException;
import com.snaplogic.api.Snap;
import com.snaplogic.common.SnapType;
import com.snaplogic.common.properties.builders.PropertyBuilder;
import com.snaplogic.snap.api.DocumentUtility;
import com.snaplogic.snap.api.ErrorViews;
import com.snaplogic.snap.api.OutputViews;
import com.snaplogic.snap.api.PropertyValues;
import com.snaplogic.snap.api.SnapCategory;
import com.snaplogic.snap.api.SnapDataException;
import com.snaplogic.snap.api.capabilities.Category;
import com.snaplogic.snap.api.capabilities.Errors;
import com.snaplogic.snap.api.capabilities.General;
import com.snaplogic.snap.api.capabilities.Inputs;
import com.snaplogic.snap.api.capabilities.Outputs;
import com.snaplogic.snap.api.capabilities.Version;
import com.snaplogic.snap.api.capabilities.ViewType;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A Snap that consumes all incoming documents.
 *
 * @author <you>
 */
@General(title = "String Processor", purpose = "Generates lowercase,uppercase, reverse ane length of string",
    author = "rpoluri", docLink = "")
@Inputs(min = 0, max = 0, accepts = {ViewType.DOCUMENT})
@Outputs(min = 1, max = 1, offers = {ViewType.DOCUMENT})
@Errors(min = 1, max = 1, offers = {ViewType.DOCUMENT})
@Version(snap = 1)
@Category(snap = SnapCategory.READ)
public class StringProcessor implements Snap {

    private static final String STRING_INPUT = "input";

    // Document utility is the only way to create a document
    // or manipulate the document header
    @Inject
    private DocumentUtility documentUtility;
    @Inject
    private OutputViews outputViews;
    @Inject
    private ErrorViews errorViews;
    private String value;

    @Override
    public void defineProperties(PropertyBuilder propertyBuilder) {
        propertyBuilder.describe(STRING_INPUT, "string input", "any string as input")
            .type(SnapType.STRING).required().add();
    }

    @Override
    public void configure(PropertyValues propertyValues) throws ConfigurationException {
        value = propertyValues.get(STRING_INPUT);
    }

    @Override
    public void execute() throws ExecutionException {
        if (value == null || value.isEmpty() || value.trim().isEmpty()) {
            SnapDataException snapDataException =
                new SnapDataException(String.format("Invalid input string valuee %s", value))
                    .withReason("input string must not be empty")
                    .withResolution("Ensure that input string has atleast one character"
                        + "");
            errorViews.write(snapDataException);
        } else {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("length", getLength(value));
            data.put("upper", toUpper(value));
            data.put("lower", toLower(value));
            data.put("reverse", reverse(value));
            outputViews.write(documentUtility.newDocument(data));
        }
    }

    @Override
    public void cleanup() throws ExecutionException {
        // NOOP
    }

    private int getLength(String s) {
        return s.length();
    }

    private String reverse(String s) {
        StringBuilder stringBuilder = new StringBuilder(s);
        return stringBuilder.reverse().toString();
    }

    private String toUpper(String s) {
        return s.toUpperCase();
    }

    private String toLower(String s) {
        return s.toLowerCase();
    }
}