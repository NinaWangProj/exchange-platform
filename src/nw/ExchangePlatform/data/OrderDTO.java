package nw.ExchangePlatform.data;


public class OrderDTO {

    private final Direction direction;
    private final String tickerSymbol;
    private final int size;
    private final double price;
    public final OrderDuration orderDuration;

    //constructor
    //this constructor is used for limit order data transfer object
    public OrderDTO(Direction direction, String tickerSymbol, int size, double price, OrderDuration orderDuration)
    {
        this.tickerSymbol = tickerSymbol;
        this.size = size;
        this.price = price;
        this.direction = direction;
        this.orderDuration = orderDuration;
    }

    //this constructor is used for market order data transfer object
    public OrderDTO(Direction direction, String tickerSymbol, int size, OrderDuration orderDuration)
    {
        this.tickerSymbol = tickerSymbol;
        this.size = size;
        this.direction = direction;
        this.orderDuration = orderDuration;
        this.price = -1;
    }
}
