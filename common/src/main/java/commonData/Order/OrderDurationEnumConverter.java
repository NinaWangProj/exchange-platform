package commonData.Order;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

public class OrderDurationEnumConverter extends AbstractBeanField<String> {
    @Override
    protected Object convert(String value) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        if (value.equals("OrderDuration.DAY"))
            return OrderDuration.DAY;
        else if (value.equals("OrderDuration.GTC"))
            return OrderDuration.GTC;
        else
            throw new CsvDataTypeMismatchException(value + " does not exist in OrderDuration enum");
    }
}