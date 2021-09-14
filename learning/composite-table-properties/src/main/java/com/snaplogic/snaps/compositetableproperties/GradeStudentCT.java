package com.snaplogic.snaps.compositetableproperties;

import com.google.inject.Inject;
import com.snaplogic.api.ConfigurationException;
import com.snaplogic.api.ExecutionException;
import com.snaplogic.api.Snap;
import com.snaplogic.common.SnapType;
import com.snaplogic.common.properties.SnapProperty;
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
import com.snaplogic.util.JsonPathBuilder;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@General(title = "Grade Student CT", purpose = "Grades students using marks secured in each subject. Used composite and table properties",
    author = "rpoluri@snaplogic.com", docLink = "")
@Inputs(min = 0, max = 1, accepts = {ViewType.DOCUMENT})
@Outputs(min = 1, max = 1, offers = {ViewType.DOCUMENT})
@Errors(min = 1, max = 1, offers = {ViewType.DOCUMENT})
@Version(snap = 1)
@Category(snap = SnapCategory.READ)
public class GradeStudentCT implements Snap {

    private static final String STUDENT_DETAILS_PROP = "Student details";// root
    private static final String MARKS_PER_SUBJECT = "marks";// root

    private static final String ST_NAME = "student name";//nest
    private static final String ST_ID = "student id";//nest
    private static final String ST_ADDRESS = "student address";//nest

    private static final String SUB_NAME_ROW = "subject name";//table
    private static final String T_MARKS_ROW = "total marks";//table
    private static final String S_MARKS_ROW = "marks secured";//table
    List<Map<String, Map<String, Object>>> subjectMarks = new ArrayList<>();
    @Inject
    private DocumentUtility documentUtility;
    @Inject
    private OutputViews outputViews;
    @Inject
    private ErrorViews errorViews;
    private String name;
    private int id;
    private String address;

    @Override
    public void defineProperties(PropertyBuilder propertyBuilder) {
        defineStudentProperties(propertyBuilder);
        defineTableProperties(propertyBuilder);
    }

    private void defineTableProperties(final PropertyBuilder propertyBuilder) {
        SnapProperty subjectMarks = propertyBuilder
            .describe(SUB_NAME_ROW, SUB_NAME_ROW)
            .type(SnapType.STRING)
            .required()
            .build();

        SnapProperty totalMarks = propertyBuilder
            .describe(T_MARKS_ROW, T_MARKS_ROW)
            .type(SnapType.INTEGER)
            .withMinValue(1)
            .withMaxValue(100)
            .required()
            .build();

        SnapProperty marksSecured = propertyBuilder.describe(S_MARKS_ROW, S_MARKS_ROW)
            .type(SnapType.INTEGER)
            .withMinValue(0)
            .withMaxValue(100)
            .required()
            .build();

        propertyBuilder.describe(MARKS_PER_SUBJECT, MARKS_PER_SUBJECT)
            .type(SnapType.TABLE)
            .withEntry(subjectMarks)
            .withEntry(totalMarks)
            .withEntry(marksSecured)
            .add();
    }

    private void defineStudentProperties(final PropertyBuilder propertyBuilder) {
        SnapProperty studentName = propertyBuilder
            .describe(ST_NAME, ST_NAME)
            .type(SnapType.STRING)
            .required()
            .build();

        SnapProperty studentId = propertyBuilder.describe(ST_ID, ST_ID)
            .type(SnapType.INTEGER)
            .required()
            .build();

        SnapProperty studentAddress = propertyBuilder.describe(ST_ADDRESS, ST_ADDRESS)
            .type(SnapType.STRING)
            .uiRowCount(5)
            .optional()
            .build();

        propertyBuilder.describe(STUDENT_DETAILS_PROP, STUDENT_DETAILS_PROP)
            .type(SnapType.COMPOSITE)
            .withEntry(studentName)
            .withEntry(studentId)
            .withEntry(studentAddress)
            .required()
            .add();
    }


    @Override
    public void configure(PropertyValues propertyValues) throws ConfigurationException {
        readStudentProperties(propertyValues);
        readSubMarks(propertyValues);
    }

    private void readSubMarks(PropertyValues propertyValues) {
        try (JsonPathBuilder rootPath = new JsonPathBuilder(MARKS_PER_SUBJECT)) {
                subjectMarks = propertyValues.get(
                    rootPath
                        .build()
                );
        } catch (Exception e) {
            SnapDataException snapException = new SnapDataException(
                "unable to read subject marks")
                .withReason(e.getMessage())
                .withResolution("ensure marks provided per subject are correct");
            errorViews.write(snapException);
        }
    }

    private void readStudentProperties(PropertyValues propertyValues) {
        try (JsonPathBuilder rootPath = new JsonPathBuilder(STUDENT_DETAILS_PROP);
            JsonPathBuilder rootPath1 = new JsonPathBuilder(STUDENT_DETAILS_PROP);
        JsonPathBuilder rootPath2 = new JsonPathBuilder(STUDENT_DETAILS_PROP)) {
            name = propertyValues.get(rootPath
                .appendValueElement()
                .appendCurrentElement(ST_NAME)
                .build());

            id = propertyValues.getInt(rootPath1
                .appendValueElement()
                .appendCurrentElement(ST_ID)
                .build()).intValue();

            address = propertyValues.get(rootPath2
                .appendValueElement()
                .appendCurrentElement(ST_ADDRESS)
                .build());
        } catch (Exception e) {
            SnapDataException snapException = new SnapDataException(
                "unable to read student details "+e.getMessage())
                .withReason(e.getMessage())
                .withResolution("ensure details provided are correct");
            errorViews.write(snapException);
        }

    }

    @Override
    public void execute() throws ExecutionException {
        try {
            int totalMarks = 0;
            int totalMarksSecured = 0;

            for (Map<String, Map<String, Object>> sub : subjectMarks) {
                int totalMarksRow = ((BigInteger) sub.get(T_MARKS_ROW)
                    .get("value")).intValue();
                int marksSecuredRow = ((BigInteger) sub.get(S_MARKS_ROW)
                    .get("value")).intValue();
                if (totalMarksRow < marksSecuredRow){
                    throw new SnapDataException(String.format(
                        "marks secured for subject %s should not be greater than total marks", sub.get(SUB_NAME_ROW)
                            .get("value")));
                }
                totalMarks += totalMarksRow;
                totalMarksSecured += marksSecuredRow;
            }

            float percentage = getPercentage(totalMarks, totalMarksSecured);

            Map<String,Object> data =  new HashMap<>();
            data.put("studentName", name);
            data.put("id", id);
            data.put("totalMarks", totalMarks);
            data.put("marksSecured", totalMarksSecured);
            data.put("percentage", percentage);
            outputViews.write(documentUtility.newDocument(data));
        } catch (Exception e){
            SnapDataException snapException = new SnapDataException(
                "unable to calculate student marks")
                .withReason(e.getMessage());
            errorViews.write(snapException);
        }
    }

    private float getPercentage(int totalMarks, int totalMarksSecured) {
        return (totalMarksSecured/(float)totalMarks)*100;
    }

    @Override
    public void cleanup() throws ExecutionException {
        // nothing to do here
    }
}
