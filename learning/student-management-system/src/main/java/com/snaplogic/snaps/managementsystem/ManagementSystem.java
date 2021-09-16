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
package com.snaplogic.snaps.managementsystem;

import com.google.common.collect.ImmutableSet;
import com.snaplogic.api.ConfigurationException;
import com.snaplogic.api.ExecutionException;
import com.snaplogic.api.InputSchemaProvider;
import com.snaplogic.common.SnapType;
import com.snaplogic.common.properties.builders.PropertyBuilder;
import com.snaplogic.snap.api.Document;
import com.snaplogic.snap.api.PropertyValues;
import com.snaplogic.snap.api.SimpleSnap;
import com.snaplogic.snap.api.SnapCategory;
import com.snaplogic.snap.api.SnapDataException;
import com.snaplogic.snap.api.capabilities.Category;
import com.snaplogic.snap.api.capabilities.Errors;
import com.snaplogic.snap.api.capabilities.General;
import com.snaplogic.snap.api.capabilities.Inputs;
import com.snaplogic.snap.api.capabilities.Outputs;
import com.snaplogic.snap.api.capabilities.Version;
import com.snaplogic.snap.api.capabilities.ViewType;
import com.snaplogic.snap.schema.api.SchemaBuilder;
import com.snaplogic.snap.schema.api.SchemaProvider;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Snap that manages student courses
 */
@General(title = "management system", purpose =
    "Manages student course details. supports three selections,"
        + "register: registers student to a course,"
        + "fee payment: pays a fee to course,"
        + "signoff: remove student from course. Used Mariadb as backend database.",
    author = "rpoluri@snaplogic.com")
@Inputs(min = 1, max = 1, accepts = {ViewType.DOCUMENT})
@Outputs(max = 0, offers = {ViewType.DOCUMENT})
@Errors(offers = {ViewType.DOCUMENT})
@Version()
@Category(snap = SnapCategory.WRITE)
public class ManagementSystem extends SimpleSnap implements InputSchemaProvider {

    //dropdown
    //schema display
    //mysql store (populate data based on )
    private static final Set<String> allowedValues = ImmutableSet.of(Options.REGISTER.getOption(),
        Options.PAY_FEE.getOption(),
        Options.SIGN_OFF.getOption());
    /*
     * register: populate schame (studentid, courseid)
     *         : store in mysql (studentid, courseid, coursename, registrationid)
     * pay fee : populate schema (studentid, courseid, amount, comment)
     *         : store in mysql (studentid, courseid, amount, transactionid)
     * signoff : populate schema (registration_id)
     *         : delete entry from mysql based on registration id
     *         : error if invalid registration id
     */
    private static final String OPTIONS = "options";
    private static final String STUDENT_ID = "studentId";
    private static final String COURSE_ID = "courseId";
    private static final String AMOUNT = "amount";
    private static final String COMMENT = "comment";
    private static final String REGISTRATION_ID = "registrationId";
    // Document utility is the only way to create a document
    // or manipulate the document header
    private Options optionSelected;
    private DbConnector dbConnector;

    private static int getIntFromBigIntDocument(Object o, String s) {
        if (!(o instanceof BigInteger)) {
            throw new SnapDataException(
                String.format("invalid information provided for property %s", s))
                .withReason("Either value not provided or the value provided is not a number")
                .withResolution("Please recheck the data provided");
        }
        return ((BigInteger) o).intValue();
    }

    @Override
    public void defineProperties(PropertyBuilder propertyBuilder) {
        propertyBuilder.describe(OPTIONS, OPTIONS)
            .required()
            .withAllowedValues(allowedValues)
            .add();
    }

    private void makeDbConnection() {
        try {
            dbConnector = new DbConnector();
        } catch (SQLException | ClassNotFoundException e) {
            SnapDataException sp = new SnapDataException("unable to acquire db connection")
                .withReason(e.getMessage())
                .withResolution("please recheck mysql server");
            errorViews.write(sp);
        }
    }

    @Override
    public void configure(PropertyValues propertyValues) throws ConfigurationException {
        String option = propertyValues.get(OPTIONS);
        optionSelected = Options.from(option);
        makeDbConnection();
    }

    @Override
    public void cleanup() throws ExecutionException {
        try {
            dbConnector.close();
        } catch (SQLException e) {
            SnapDataException sp = new SnapDataException("unable to close db connection")
                .withReason(e.getMessage());
            errorViews.write(sp);
        }
    }

    @Override
    protected void process(Document document, String s) {
        Map<String, String> data = new HashMap<>(2);
        Map<String, Object> dataAsMap = documentUtility.getAsMap(document, errorViews);
        switch (optionSelected) {
            case REGISTER:
                dbConnector.registerStudent(
                    getIntFromBigIntDocument(dataAsMap.get(STUDENT_ID), STUDENT_ID),
                    getIntFromBigIntDocument(dataAsMap.get(COURSE_ID), COURSE_ID), "courseName");
                data.put("message", "student registered successfully");
                break;
            case PAY_FEE:
                dbConnector.payFeesForStudent(
                    getIntFromBigIntDocument(dataAsMap.get(STUDENT_ID), STUDENT_ID),
                    getIntFromBigIntDocument(dataAsMap.get(COURSE_ID), COURSE_ID),
                    getIntFromBigIntDocument(dataAsMap.get(AMOUNT), AMOUNT),
                    (String) dataAsMap.get(COMMENT));
                data.put("message", "student fees paid");
                break;
            case SIGN_OFF:
                dbConnector.deregisterStudent(
                    getIntFromBigIntDocument(dataAsMap.get(REGISTRATION_ID), REGISTRATION_ID));
                data.put("message", "student registration closed successfully");
                break;
        }
        outputViews.write(documentUtility.newDocumentFor(document, data));
    }

    @Override
    public void defineInputSchema(SchemaProvider schemaProvider) {
        schemaProvider.getRegisteredInputViewNames()
            .forEach(view -> {
                SchemaBuilder schemaBuilder = schemaProvider.getSchemaBuilder(view);
                switch (optionSelected) {
                    case REGISTER:
                        schemaBuilder
                            .withChildSchema(
                                schemaProvider.createSchema(SnapType.INTEGER, STUDENT_ID))
                            .withChildSchema(
                                schemaProvider.createSchema(SnapType.INTEGER, COURSE_ID))
                            .build();
                        break;
                    case PAY_FEE:
                        schemaBuilder
                            .withChildSchema(
                                schemaProvider.createSchema(SnapType.INTEGER, STUDENT_ID))
                            .withChildSchema(
                                schemaProvider.createSchema(SnapType.INTEGER, COURSE_ID))
                            .withChildSchema(schemaProvider.createSchema(SnapType.INTEGER, AMOUNT))
                            .withChildSchema(schemaProvider.createSchema(SnapType.STRING, COMMENT))
                            .build();
                        break;
                    case SIGN_OFF:
                        schemaBuilder
                            .withChildSchema(
                                schemaProvider.createSchema(SnapType.INTEGER, REGISTRATION_ID))
                            .build();
                        break;
                }
            });
    }

    enum Options {
        REGISTER("register"), PAY_FEE("pay fee"), SIGN_OFF("sign off");

        private static final Map<String, Options> map = new HashMap<>();

        static {
            for (Options pageType : Options.values()) {
                map.put(pageType.getOption(), pageType);
            }
        }

        private final String option;

        Options(String register) {
            this.option = register;
        }

        public static Options from(String value) {
            return map.get(value);
        }

        public String getOption() {
            return option;
        }
    }
}