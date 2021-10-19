package com.snaplogic.snaps.managementsystem;

import com.snaplogic.api.ConfigurationException;
import com.snaplogic.common.properties.Suggestions;
import com.snaplogic.common.properties.builders.PropertyBuilder;
import com.snaplogic.common.properties.builders.SuggestionBuilder;
import com.snaplogic.snap.api.Document;
import com.snaplogic.snap.api.DocumentUtility;
import com.snaplogic.snap.api.PropertyValues;
import com.snaplogic.snap.api.SimpleSnap;
import com.snaplogic.snap.api.SnapCategory;
import com.snaplogic.snap.api.capabilities.Category;
import com.snaplogic.snap.api.capabilities.General;
import com.snaplogic.snap.api.capabilities.Inputs;
import com.snaplogic.snap.api.capabilities.Outputs;
import com.snaplogic.snap.api.capabilities.Version;
import com.snaplogic.snap.api.capabilities.ViewType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

@General(title = "admission control",
    purpose = "manages the admission of student",
    docLink = "http://my-link",
    author = "rpoluri@snaploi.com")
@Inputs(max = 1,accepts = ViewType.DOCUMENT)
@Outputs(min = 1,max = 1,offers = ViewType.DOCUMENT)
@Version(snap = 1)
@Category(snap = SnapCategory.WRITE)
public class AdmissionControl extends SimpleSnap {

    String goal;
    String year;
    String specialiazation;

    @Override
    protected void process(Document document, String s) {
        Map<String,Object> data = new HashMap<>(1);
        data.put("message",
            String.format("Student joined in %s for year %s and has goal to become %s", specialiazation,year,goal));
        outputViews.write(documentUtility.newDocument(data));
    }

    @Override
    public void defineProperties(PropertyBuilder propertyBuilder) {
        propertyBuilder.describe("Year","Year" ,"To which Year Student admission taking to")
            .required()
            .withAllowedValues(Set.of("1","2","3","4"))
            .add();

        propertyBuilder.describe("Specialization","Specialization", "Course student want to pursue")
            .required()
            .withSuggestions((suggestionBuilder, propertyValues) -> suggestionBuilder.node("Specialization")
                .suggestions("CSE","EEE","ECE","CIVIL","MECHANICAL"))
            .add();

        propertyBuilder.describe("Goal", "Goal","what student want to become after pursuing degree")
            .required()
            .withSuggestions(new GoalSuggestions())
            .add();
    }

    @Override
    public void configure(PropertyValues propertyValues) throws ConfigurationException {
        year = propertyValues.get("Year");
        goal = propertyValues.get("Goal");
        specialiazation = propertyValues.get("Specialization");
    }
}
class GoalSuggestions implements Suggestions{

    @Override
    public void suggest(SuggestionBuilder suggestionBuilder, PropertyValues propertyValues) {
        String specializationSelected = propertyValues.get("Specialization");
        Set<String> suggestions = new HashSet<>(4);
        suggestions.add("Not Decided");
        if (StringUtils.isNotBlank(specializationSelected)){
            switch (specializationSelected){
                case "CSE":
                    suggestions.add("Programmer");
                    suggestions.add("Tester");
                    suggestions.add("DevOps");
                    break;
                case "EEE":
                    suggestions.add("Electrician");
                    suggestions.add("PowerGrid Manager");
                    suggestions.add("Transformer station manager");
                    break;
                case "ECE":
                    suggestions.add("chip designer");
                    suggestions.add("Programmer");
                    suggestions.add("Circuit Designer");
                    break;
                case "CIVIL":
                    suggestions.add("Architect");
                    suggestions.add("Builder");
                    break;
                case "MECHANICAL":
                    suggestions.add("Robot Designer");
                    suggestions.add("Car Modeler");
                    break;
            }
        }
        suggestionBuilder.node("Goal")
            .suggestions(suggestions.toArray(new String[0]));
    }
}
