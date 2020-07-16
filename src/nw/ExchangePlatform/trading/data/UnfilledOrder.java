package nw.ExchangePlatform.trading.data;

import java.util.Date;

public class UnfilledOrder extends MarketParticipantOrder {
    //fields
    public final String reason;

    //constructor
    public UnfilledOrder(MarketParticipantOrder order, String reason) {
        super(order.getSessionID(),order.getUserID(), order.getName(), order.getOrderID(), order.getTime(), order.getDirection(), order.getTickerSymbol(), order.getSize(), order.getPrice(),order.orderType,order.orderDuration);
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
}
