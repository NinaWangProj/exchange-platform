package common.csv;

import com.opencsv.bean.CsvToBeanBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class CsvHelper {
    static public <T> List<T> GetRowsFromCSV(String resourcePath, Class<T> type)
    {
        ClassLoader classLoader = CsvHelper.class.getClassLoader();


        FileReader reader = null;
        try {
            File file = new File(classLoader.getResource(resourcePath).toURI());
            reader = new FileReader(file);
        }
        catch(FileNotFoundException | URISyntaxException fex) {
            assert false: "Cannot find resource file " + resourcePath;
            return new ArrayList<T>();
        }

        List<T> beans = new CsvToBeanBuilder(reader)
                .withType(type).build().parse();

        return beans;
    }
}
