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

    //this constructor is used for market order data transfer object
    public OrderDTO(Direction direction, OrderType orderType, String tickerSymbol, int size, OrderDuration orderDuration)
    {
        this.tickerSymbol = tickerSymbol;
        this.size = size;
        this.direction = direction;
        this.orderDuration = orderDuration;
        this.orderType = orderType;
        this.price = -1;
    }

    public OrderDTO(byte[] DTOByteArray) {
        this.orderDTOByteArray = DTOByteArray;
        int position = 0;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(DTOByteArray);

        byte[] buffer = new byte[1];
        inputStream.read(buffer, position, 1);
        direction = Direction.valueOf(ByteBuffer.wrap(buffer).getInt());
        position += 1;

        inputStream.read(buffer,position,1);
        orderType = OrderType.valueOf(ByteBuffer.wrap(buffer).getInt());
        position += 1;

        inputStream.read(buffer, position, 1);
        int numOfTickerChar = ByteBuffer.wrap(buffer).getInt();
        byte[] tickerSymbolBuffer = new byte[numOfTickerChar];
        inputStream.read(tickerSymbolBuffer, position, numOfTickerChar);
        position += numOfTickerChar;
        tickerSymbol = tickerSymbolBuffer.toString();

        byte[] sizeBuffer = new byte[4];
        inputStream.read(sizeBuffer,position,4);
        size = ByteBuffer.wrap(buffer).getInt();
        position += 4;

        if(orderType.equals(OrderType.LIMITORDER)) {
            byte[] priceBuffer = new byte[8];
            inputStream.read(priceBuffer, position, 8);
            price = ByteBuffer.wrap(priceBuffer).getDouble();
            position += 8;
        } else {
            price = -1;
        }

        inputStream.read(buffer, position, 1);
        orderDuration = OrderDuration.valueOf(ByteBuffer.wrap(buffer).getInt());
    }

    public byte[] Serialize()
    {
        try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputStream.write(direction.getByteValue());

                outputStream.write(orderType.getByteValue());

                byte[] tickerSymbolByte = tickerSymbol.getBytes();
                int tickerSymbolByteSize = tickerSymbolByte.length;
                outputStream.write((byte) tickerSymbolByteSize);
                outputStream.write(tickerSymbolByte);

                byte[] sizeByte = ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).putInt(size).array();
                outputStream.write(sizeByte);

                if(orderType.equals(OrderType.LIMITORDER)) {
                    byte[] priceByte = ByteBuffer.allocate(8).order(ByteOrder.nativeOrder()).putDouble(price).array();
                    outputStream.write(priceByte);
                }

                outputStream.write(orderDuration.getByteValue());

                orderDTOByteArray = outputStream.toByteArray();

            } catch (IOException e) {

            }

        return orderDTOByteArray;
    }
}
