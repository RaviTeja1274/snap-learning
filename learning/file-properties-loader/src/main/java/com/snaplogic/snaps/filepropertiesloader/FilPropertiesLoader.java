package com.snaplogic.snaps.filepropertiesloader;


import com.snaplogic.api.ConfigurationException;
import com.snaplogic.common.properties.SnapProperty.DecoratorType;
import com.snaplogic.common.properties.builders.PropertyBuilder;
import com.snaplogic.snap.api.Document;
import com.snaplogic.snap.api.ExpressionProperty;
import com.snaplogic.snap.api.PropertyValues;
import com.snaplogic.snap.api.SimpleSnap;
import com.snaplogic.snap.api.SnapCategory;
import com.snaplogic.snap.api.capabilities.Category;
import com.snaplogic.snap.api.capabilities.Errors;
import com.snaplogic.snap.api.capabilities.General;
import com.snaplogic.snap.api.capabilities.Inputs;
import com.snaplogic.snap.api.capabilities.Outputs;
import com.snaplogic.snap.api.capabilities.Version;
import com.snaplogic.snap.api.capabilities.ViewType;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.filefilter.WildcardFileFilter;

@General(title = "File Properties Loader", purpose = "Load files from the provided path by applying filter and displays itts properties",
    author = "rpoluri@snaplogic.com", docLink = "")
@Inputs(min = 0, max = 1, accepts = {ViewType.DOCUMENT})
@Outputs(min = 1, max = 1, offers = {ViewType.DOCUMENT})
@Errors(min = 1, max = 1, offers = {ViewType.DOCUMENT})
@Version(snap = 1)
@Category(snap = SnapCategory.READ)
public class FilPropertiesLoader extends SimpleSnap {

    private static final String FILE_PATH = "filePath";
    private static final String FILTER = "filter";

    private ExpressionProperty filePath;
    private String filter;

    @Override
    public void defineProperties(PropertyBuilder propertyBuilder) {
        propertyBuilder
            .describe(FILE_PATH, "absolute path of file")
            .expression(DecoratorType.ENABLED_EXPRESSION)
            .schemaAware(DecoratorType.ACCEPTS_SCHEMA)
            .required()
            .add();

        propertyBuilder
            .describe(FILTER, "filters to be applied on files",
                "If matches will display all files with matching")
            .expression()
            .defaultValue("*")
            .add();

    }


    @Override
    public void configure(PropertyValues propertyValues) throws ConfigurationException {
        filePath = propertyValues.getAsExpression(FILE_PATH);
        filter = propertyValues.getAsExpression(FILTER).eval(null);
    }

    @Override
    protected void process(Document document, String s) {

        String absolutePath = filePath.eval(document);

        File directory = new File(absolutePath);
        FileFilter fileFilter = new WildcardFileFilter(filter);
        File[] files = directory.listFiles(fileFilter);
        Map<String, Object> output = new HashMap<>(5);
        if (files == null || files.length <= 0) {
            output.put("message", "no matching files found with pattern :: " + filter);
        } else {
            ArrayList<Object> list = new ArrayList<>(files.length);
            for (File aFile : files) {
                if (aFile.isDirectory()) {
                    continue;
                }
                Map<String, Object> output1 = new HashMap<>(5);
                long totalSize = aFile.length();
                boolean canExecute = aFile.canExecute();
                boolean canRead = aFile.canRead();
                boolean canWrite = aFile.canWrite();
                String name = aFile.getName();
                output1.put("name", name);
                output1.put("totalSize", totalSize);
                output1.put("canRead", canRead);
                output1.put("canWrite", canWrite);
                output1.put("canExecute", canExecute);
                list.add(output1);
            }
            output.put("message", "found properties for files");
            output.put("files", list);
        }
        outputViews.write(documentUtility.newDocumentFor(document, output));
    }
}
