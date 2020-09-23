package commonData.Order;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import com.opencsv.bean.CsvDate;

import java.util.Date;

public class MarketParticipantOrder implements Info {

    //fields
    @CsvBindByName
    private final int sessionID;
    @CsvBindByName
    private final int userID;
    @CsvBindByName
    private final String name;
    @CsvBindByName
    private final int orderID;
    @CsvCustomBindByName(converter = PythonTimeConverter.class)
    private final Date time;
    @CsvCustomBindByName(converter = DirectionEnumConverter.class)
    private final Direction direction;
    @CsvBindByName
    private final String tickerSymbol;
    @CsvBindByName
    private int size;
    @CsvBindByName
    private final double price;
    @CsvCustomBindByName(converter = OrderTypeEnumConverter.class)
    private final OrderType orderType;
    @CsvCustomBindByName(converter = OrderDurationEnumConverter.class)
    private final OrderDuration orderDuration;


    //constructor
    public MarketParticipantOrder(int sessionID, int userID, String name, int orderID, Date time, Direction direction, String tickerSymbol, int size,
                                  double price, OrderType orderType, OrderDuration orderDuration)
    {
        this.sessionID = sessionID;
        this.userID = userID;
        this.name = name;
        this.orderID = orderID;
        this.time = time;
        this.tickerSymbol = tickerSymbol;
        this.size = size;
        this.price = price;
        this.orderType = orderType;
        this.direction = direction;
        this.orderDuration = orderDuration;
    }

    public MarketParticipantOrder() {
        this.sessionID = -1;
        this.userID = -1;
        this.name = "Default Name";
        this.orderID = -1;
        this.time = new Date();
        this.tickerSymbol = "Def";
        this.size = -1;
        this.price = -1;
        this.orderType = OrderType.LIMITORDER;
        this.direction = Direction.BUY;
        this.orderDuration = OrderDuration.DAY;
    }

    public int getUserID() {
        return userID;
    }

    public String getName() {
        return name;
    }

    public int getOrderID() {
        return orderID;
    }

    public Date getTime() {
        return time;
    }

    public String getTickerSymbol() {
        return tickerSymbol;
    }

    public double getPrice() {
        return price;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getReason() {
        return "";
    }

    public int getSessionID() {
        return sessionID;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public OrderDuration getOrderDuration() {
        return orderDuration;
    }

    @Override
    public String toString() {
        String result = getSessionID() + "," +
                        getUserID() + "," +
                        getName() + "," +
                        getOrderID() + "," +
                        getTime() + "," +
                        getTickerSymbol() + "," +
                        getSize() + "," +
                        getPrice() + "," +
                        getOrderType()+ "," +
                        getDirection() + "," +
                        getOrderDuration();
        return result;
    }
}
