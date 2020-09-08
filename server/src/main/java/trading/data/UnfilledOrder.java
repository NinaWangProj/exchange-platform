package trading.data;

import commonData.Order.MarketParticipantOrder;
import commonData.Order.Direction;
import commonData.Order.OrderDuration;
import commonData.Order.OrderType;
import com.opencsv.bean.CsvBindByName;

import java.util.Date;

public class UnfilledOrder extends MarketParticipantOrder {
    //fields
    @CsvBindByName
    public final String reason;

    //constructor
    public UnfilledOrder(MarketParticipantOrder order, String reason) {
        super(order.getSessionID(),order.getUserID(), order.getName(), order.getOrderID(), order.getTime(), order.getDirection(), order.getTickerSymbol(), order.getSize(), order.getPrice(), order.getOrderType(), order.getOrderDuration());
        this.reason = reason;
    }

    public UnfilledOrder(int sessionID, int userID, String name, int orderID, Date time, Direction direction, String tickerSymbol, int size,
                         double price, OrderType orderType, OrderDuration duration, String reason)
    {
        super(sessionID,userID,name,orderID,time,direction,tickerSymbol,size,price,orderType,duration);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        String result = super.toString() + "," +
                        getReason();
        return result;
    }
}
