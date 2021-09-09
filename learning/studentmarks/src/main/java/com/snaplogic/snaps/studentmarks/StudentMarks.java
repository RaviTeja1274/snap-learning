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
package com.snaplogic.snaps.studentmarks;

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
 * A Snap that generates the configured number of documents.
 */
@General(title = "Student Marks Calculator", purpose = "Calculates total marks secured by a student and percentage if selected",
    author = "rpoluri@snaplogic.com", docLink = "")
@Inputs(min = 0, max = 1, accepts = {ViewType.DOCUMENT})
@Outputs(min = 1, max = 1, offers = {ViewType.DOCUMENT})
@Errors(min = 1, max = 1, offers = {ViewType.DOCUMENT})
@Version(snap = 1)
@Category(snap = SnapCategory.READ)
public class StudentMarks implements Snap {

    private static final String STUDENT_NAME = "studentName";
    private static final String SUBJECT1 = "subject1";
    private static final String SUBJECT2 = "subject2";
    private static final String SUBJECT3 = "subject3";
    private static final String IS_PERCNTAGE = "percentage";

    // Document utility is the only way to create a document
    // or manipulate the document header
    @Inject
    private DocumentUtility documentUtility;
    @Inject
    private OutputViews outputViews;
    @Inject
    private ErrorViews errorViews;

    private int sub1Marks;
    private int sub2Marks;
    private int sub3Marks;
    private String name;
    private boolean checkPercentage;

    @Override
    public void defineProperties(PropertyBuilder propertyBuilder) {
        propertyBuilder.describe(STUDENT_NAME, "name of student")
            .type(SnapType.STRING).required().add();

        propertyBuilder.describe(SUBJECT1, "marks secured in subject 1")
            .type(SnapType.INTEGER).required().add();
        propertyBuilder.describe(SUBJECT2, "marks secured in subject 2")
            .type(SnapType.INTEGER).required().add();
        propertyBuilder.describe(SUBJECT3, "marks secured in subject 3")
            .type(SnapType.INTEGER).required().add();

        propertyBuilder.describe(IS_PERCNTAGE, "should percentage be calculated",
                "If selected then percentage will be displayed in output.")
            .type(SnapType.BOOLEAN).add();
    }

    @Override
    public void configure(PropertyValues propertyValues) throws ConfigurationException {
        name = propertyValues.get(STUDENT_NAME);
        sub1Marks = propertyValues.getInt(SUBJECT1).intValue();
        sub2Marks = propertyValues.getInt(SUBJECT2).intValue();
        sub3Marks = propertyValues.getInt(SUBJECT3).intValue();
        checkPercentage = propertyValues.getBoolean(IS_PERCNTAGE, false);
    }

    @Override
    public void execute() throws ExecutionException {

        if (isValidName(name) &&
            isValidMarks(sub1Marks, SUBJECT1) &&
            isValidMarks(sub2Marks, SUBJECT2) &&
            isValidMarks(sub3Marks, SUBJECT3)) {

            Map<String, Object> data = new LinkedHashMap<>();
            data.put(STUDENT_NAME, name);
            int totalMarks = calculateTotalMarks();
            data.put("totalMarks", totalMarks);
            if (checkPercentage) {
                data.put(IS_PERCNTAGE, calculatePercentage(3, totalMarks));
            }
            outputViews.write(documentUtility.newDocument(data));
        }
    }


    @Override
    public void cleanup() throws ExecutionException {
        // NOOP
    }

    private float calculatePercentage(int count, int total){
        return total/(float)count;
    }
    private int calculateTotalMarks() {
        return sub1Marks + sub2Marks + sub3Marks;
    }

    private boolean isValidName(String name) {
        if (name == null || name.isEmpty() || name.trim().isEmpty()) {
            SnapDataException snapDataException =
                new SnapDataException(String.format("Invalid student name %s", name))
                    .withReason("Value of student name property cannot be empty")
                    .withResolution(
                        "Ensure Value of student name property contains at least one character");
            errorViews.write(snapDataException);
            return false;
        }
        return true;
    }

    private boolean isValidMarks(int marks, String subName) {
        if (marks < 0 || marks > 100) {
            SnapDataException snapDataException =
                new SnapDataException(String.format("Invalid marks for subject %s", subName))
                    .withReason("Value of subject marks should not be negative or greater than 100")
                    .withResolution("Ensure subject marks are between 0 and 100 inclusive");
            errorViews.write(snapDataException);
            return false;
        }
        return true;
    }
}