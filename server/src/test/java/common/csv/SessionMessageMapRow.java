package common.csv;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import commonData.DataType.OrderStatusType;
import commonData.DataType.OrderStatusTypeEnumConverter;

public class SessionMessageMapRow {
    @CsvBindByName
    public int sessionID;
    @CsvBindByName
    public String tickerSymbol;
    @CsvBindByName
    public int orderID;
    @CsvCustomBindByName(converter = OrderStatusTypeEnumConverter.class)
    public OrderStatusType orderStatus;
    @CsvBindByName
    public String message;
}

