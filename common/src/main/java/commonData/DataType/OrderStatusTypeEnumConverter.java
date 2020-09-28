package commonData.DataType;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

public class OrderStatusTypeEnumConverter extends AbstractBeanField<String> {
    @Override
    protected Object convert(String value) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        if (value.equals("OrderStatusType.Pending"))
            return OrderStatusType.Pending;
        else if (value.equals("OrderStatusType.PartiallyFilled"))
            return OrderStatusType.PartiallyFilled;
        else if (value.equals("OrderStatusType.Unfilled"))
            return OrderStatusType.Unfilled;
        else
            throw new CsvDataTypeMismatchException(value + " does not exist in OrderStatusType enum");
    }
}
