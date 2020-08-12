package commonData.DTO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MarketDataItemDTO {
    private final DTOType dtoType;
    private final String tickerSymbol;
    private int size;
    private final double price;
    public byte[] orderDTOByteArray;

    //constructor
    public MarketDataItemDTO(String tickerSymbol, int size, double price)
    {
        this.tickerSymbol = tickerSymbol;
        this.size = size;
        this.price = price;
        dtoType = DTOType.MarketDataItem;
    }

    public String getTickerSymbol() {
        return tickerSymbol;
    }

    public double getPrice() {
        return price;
    }

    public int getSize() {
        return size;
    }

    public byte[] Serialize()
    {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            byte[] tickerSymbolByte = getTickerSymbol().getBytes();
            byte tickerSize = (byte)tickerSymbolByte.length;
            outputStream.write(tickerSize);
            outputStream.write(tickerSymbolByte);

            byte[] sizeByte = ByteBuffer.allocate(4).putInt(getSize()).array();
            outputStream.write(sizeByte);

            byte[] priceByte = ByteBuffer.allocate(8).order(ByteOrder.nativeOrder()).putDouble(getPrice()).array();
            outputStream.write(priceByte);


            orderDTOByteArray = outputStream.toByteArray();

        } catch (IOException e) {

        }
        return orderDTOByteArray;
    }

    public static MarketDataItemDTO Deserialize(byte[] DTOByteArray) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(DTOByteArray);

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

        MarketDataItemDTO dataItemDTO = new MarketDataItemDTO(tickerSymbolT,sizeT,priceT);
        return dataItemDTO;
    }
}
