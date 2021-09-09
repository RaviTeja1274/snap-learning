package com.snaplogic.snaps.employeegrading;

import static com.snaplogic.snap.api.capabilities.ViewType.DOCUMENT;

import com.google.common.collect.ImmutableSet;
import com.snaplogic.api.ConfigurationException;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@General(title = "Employee Grading", purpose = "Grades employee based on salary and experience",
    docLink = "", author = "rplouri@snaplogic.com")
@Inputs(max = 1, accepts = {DOCUMENT})
@Outputs(min = 1, max = 1, offers = DOCUMENT)
@Errors(offers = DOCUMENT)
@Version
@Category(snap = SnapCategory.READ)
public class GradeEmployee extends SimpleSnap implements Constants {

    private static final Set<String> allowedValues = ImmutableSet.of("dev", "qa", "doc");
    private static final int A_GRADE_LIMIT_FOR_LESS_THAN_5_YRS_EXP = 50000;
    private static final int A_GRADE_LIMIT_FOR_GREATER_THAN_5_YRS_EXP = 100000;

    private int empId;
    private String empName;
    private String department;
    private int yearsOfExp;
    private int currentSalary;

    private boolean isCurrentEmployer;


    @Override
    protected void process(Document document, String s) {

        if (!isCurrentEmployer) {
            SnapDataException snapDataException = new SnapDataException(
                "Employee is not currently working with org")
                .withReason("Employee should be working currently in org")
                .withResolution("Select only employee who is part of  org");
            errorViews.write(snapDataException);
        } else {
            Map<String, Object> data = new HashMap<>();
            data.put("employeeName", empName);
            data.put("employeeId", empId);
            data.put("grade", popGrade());

            outputViews.write(documentUtility.newDocumentFor(document, data));
        }
    }

    private String popGrade() {
        if (yearsOfExp < 5) {
            if (currentSalary <= A_GRADE_LIMIT_FOR_LESS_THAN_5_YRS_EXP) {
                return "A";
            }
            return "B";
        } else {
            if (currentSalary <= A_GRADE_LIMIT_FOR_GREATER_THAN_5_YRS_EXP) {
                return "A";
            }
            return "B";
        }
    }

    @Override
    public void defineProperties(PropertyBuilder propertyBuilder) {
        propertyBuilder.describe(EMPLOYEE_ID, "id of the employee")
            .required()
            .type(SnapType.INTEGER)
            .withMinValue(1)
            .add();

        propertyBuilder.describe(EMPLOYEE_NAME, "name of the employee")
            .required()
            .type(SnapType.STRING)
            .withMinLength(1)
            .add();

        propertyBuilder.describe(DEPARTMENT, "department employee belongs to")
            .required()
            .withAllowedValues(allowedValues)
            .add();

        propertyBuilder.describe(YEARS_OF_EXP, "total experience of employee")
            .required()
            .type(SnapType.INTEGER)
            .withMinValue(1)
            .withMaxValue(10)
            .defaultValue(2)
            .add();

        propertyBuilder.describe(CURRENT_EMPLOYEE,
                "is this employee currently working with the org")
            .optional()
            .type(SnapType.BOOLEAN)
            .defaultValue(false)
            .add();

        propertyBuilder.describe(CURRENT_SALARY_PER_MONTH, "current salary of employee")
            .required()
            .type(SnapType.INTEGER)
            .add();
    }

    @Override
    public void configure(PropertyValues propertyValues) throws ConfigurationException {
        empId = propertyValues.getInt(EMPLOYEE_ID).intValue();
        empName = propertyValues.get(EMPLOYEE_NAME);
        department = propertyValues.get(DEPARTMENT);
        yearsOfExp = propertyValues.getInt(YEARS_OF_EXP).intValue();
        currentSalary = propertyValues.getInt(CURRENT_SALARY_PER_MONTH).intValue();
        isCurrentEmployer = propertyValues.getBoolean(CURRENT_EMPLOYEE, false);
    }
}
