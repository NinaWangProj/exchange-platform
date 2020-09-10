package trading.data;

import commonData.Order.MarketParticipantOrder;
import commonData.Order.Direction;
import commonData.Order.OrderDuration;
import commonData.Order.OrderType;
import com.opencsv.bean.CsvBindByName;

import java.util.Date;

public class PendingOrder extends MarketParticipantOrder {
    @CsvBindByName
    public String pendingMessage;

    public PendingOrder(MarketParticipantOrder order, String pendingMessage) {
        super(order.getSessionID(),order.getUserID(), order.getName(), order.getOrderID(), order.getTime(), order.getDirection(), order.getTickerSymbol(), order.getSize(), order.getPrice(), order.getOrderType(), order.getOrderDuration());
        this.pendingMessage = pendingMessage;
    }

    public PendingOrder(int sessionID, int userID, String name, int orderID, Date time, Direction direction, String tickerSymbol, int size,
                        double price, OrderType orderType, OrderDuration duration, String pendingMessage)
    {
        super(sessionID,userID,name,orderID,time,direction,tickerSymbol,size,price,orderType,duration);
        this.pendingMessage = pendingMessage;
    }

    public PendingOrder() {
        super();
        this.pendingMessage = "Default Message";
    }

    public String getReason() {
        return pendingMessage;
    }

    @Override
    public String toString() {
        String result = super.toString() + "," +
                        getReason();
        return result;
    }
}
