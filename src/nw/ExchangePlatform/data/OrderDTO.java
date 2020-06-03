package nw.ExchangePlatform.data;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class OrderDTO {

    private final Direction direction;
    private final String tickerSymbol;
    private final int size;
    private final double price;
    private final OrderDuration orderDuration;
    private final OrderType orderType;
    public byte[] orderDTOByteArray;
    //constructor
    //this constructor is used for limit order data transfer object
    public OrderDTO(Direction direction, OrderType orderType, String tickerSymbol, int size, double price, OrderDuration orderDuration)
    {
        this.tickerSymbol = tickerSymbol;
        this.size = size;
        this.price = price;
        this.direction = direction;
        this.orderDuration = orderDuration;
        this.orderType = orderType;
    }

    public byte[] Serialize()
    {
        try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputStream.write(direction.getByteValue());

                outputStream.write(orderType.getByteValue());

                byte[] tickerSymbolByte = tickerSymbol.getBytes();
                byte tickerSize = (byte)tickerSymbolByte.length;
                outputStream.write(tickerSize);
                outputStream.write(tickerSymbolByte);


                byte[] sizeByte = ByteBuffer.allocate(4).putInt(size).array();
                outputStream.write(sizeByte);

                byte[] priceByte = ByteBuffer.allocate(8).putDouble(price).array();
                outputStream.write(priceByte);

                outputStream.write(orderDuration.getByteValue());

                orderDTOByteArray = outputStream.toByteArray();

            } catch (IOException e) {

            }

        return orderDTOByteArray;
    }

    public static OrderDTO Deserialize(byte[] DTOByteArray) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(DTOByteArray);

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

        OrderDTO orderDTO = new OrderDTO(directionT, orderTypeT, tickerSymbolT, sizeT, priceT, orderDurationT);

        return orderDTO;
    }
}
