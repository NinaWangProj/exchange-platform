package commonData.Order;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

public class DirectionEnumConverter extends AbstractBeanField<String> {
    @Override
    protected Object convert(String value) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        if (value.equals("Direction.BUY"))
            return Direction.BUY;
        else if (value.equals("Direction.SELL"))
            return Direction.SELL;
        else
            throw new CsvDataTypeMismatchException(value + " does not exist in Direction enum");
    }
}