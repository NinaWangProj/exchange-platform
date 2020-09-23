package commonData.Order;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

public class OrderTypeEnumConverter extends AbstractBeanField<String> {
    @Override
    protected Object convert(String value) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        if (value.equals("OrderType.LIMITORDER"))
            return OrderType.LIMITORDER;
        else if (value.equals("OrderType.MARKETORDER"))
            return OrderType.MARKETORDER;
        else
            throw new CsvDataTypeMismatchException(value + " does not exist in OrderType enum");
    }
}