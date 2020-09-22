package commonData.Order;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class PythonTimeConverter extends AbstractBeanField<String> {
    String formatIn = "yyyy-MM-dd HH:mm:ss.SSSSSS";

    @Override
    protected Object convert(String value) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        LocalDateTime ldt = LocalDateTime.parse(value, DateTimeFormatter.ofPattern(formatIn));
        Date out = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());

        return out;
    }
}