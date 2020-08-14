package commonData.DTO;


import commonData.Order.Direction;
import commonData.Order.OrderDuration;
import commonData.Order.OrderType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class OrderDTO implements Transferable {

    private final Long clientRequestID;
    private final DTOType dtoType;
    private final Direction direction;
    private final String tickerSymbol;
    private final int size;
    private final double price;
    private final OrderDuration orderDuration;
    private final OrderType orderType;
    public byte[] orderDTOByteArray;
    //constructor
    //this constructor is used for limit order data transfer object
    public OrderDTO(long clientRequestID, Direction direction, OrderType orderType, String tickerSymbol, int size, double price, OrderDuration orderDuration)
    {
        this.clientRequestID = clientRequestID;
        this.tickerSymbol = tickerSymbol;
        this.size = size;
        this.price = price;
        this.direction = direction;
        this.orderDuration = orderDuration;
        this.orderType = orderType;
        dtoType = DTOType.Order;
    }

    public byte[] Serialize()
    {
        try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                byte[] requestIDByteArray = ByteBuffer.allocate(8).putLong(clientRequestID).array();
                outputStream.write(requestIDByteArray);

                outputStream.write(getDirection().getByteValue());

                outputStream.write(getOrderType().getByteValue());

                byte[] tickerSymbolByte = getTickerSymbol().getBytes();
                byte tickerSize = (byte)tickerSymbolByte.length;
                outputStream.write(tickerSize);
                outputStream.write(tickerSymbolByte);

                byte[] sizeByte = ByteBuffer.allocate(4).putInt(getSize()).array();
                outputStream.write(sizeByte);


                byte[] priceByte = ByteBuffer.allocate(8).order(ByteOrder.nativeOrder()).putDouble(getPrice()).array();
                outputStream.write(priceByte);


                outputStream.write(getOrderDuration().getByteValue());

                orderDTOByteArray = outputStream.toByteArray();

            } catch (IOException e) {

            }

        return orderDTOByteArray;
    }

    public static OrderDTO Deserialize(byte[] DTOByteArray) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(DTOByteArray);

        byte[] requestIDBuffer = new byte[8];
        inputStream.read(requestIDBuffer, 0, 8);
        Long requestIDT = ByteBuffer.wrap(requestIDBuffer).getLong();

        Direction directionT = Direction.lookupEnumType(inputStream.read());

        OrderType orderTypeT = OrderType.valueOf(inputStream.read());

        int tickerSymbolLength = inputStream.read();
        byte[] tickerSymbolBuffer = new byte[tickerSymbolLength];
        inputStream.read(tickerSymbolBuffer, 0, tickerSymbolLength);
        String tickerSymbolT = new String(tickerSymbolBuffer);

        byte[] sizeBuffer = new byte[4];
        inputStream.read(sizeBuffer,0,4);
        int sizeT = ByteBuffer.wrap(sizeBuffer).getInt();

        byte[] priceBuffer = new byte[8];
        inputStream.read(priceBuffer, 0, 8);
        double priceT = ByteBuffer.wrap(priceBuffer).getDouble();

        OrderDuration orderDurationT = OrderDuration.valueOf(inputStream.read());

        OrderDTO orderDTO = new OrderDTO(requestIDT, directionT, orderTypeT, tickerSymbolT, sizeT, priceT, orderDurationT);

        return orderDTO;
    }

    public String getTickerSymbol() {
        return tickerSymbol;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getSize() {
        return size;
    }

    public double getPrice() {
        return price;
    }

    public OrderDuration getOrderDuration() {
        return orderDuration;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public DTOType getDtoType() {
        return dtoType;
    }

    public Long getClientRequestID() {
        return clientRequestID;
    }
}
