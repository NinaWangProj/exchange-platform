package nw.ExchangePlatform.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class MareketDataRequestDTO implements Transferable{
    private final String tickerSymbol;
    private final MarketDataType dataType;

    public MareketDataRequestDTO(String tickerSymbol,MarketDataType dataType) {
        this.tickerSymbol = tickerSymbol;
        this.dataType = dataType;
    }

    public byte[] Serialize() throws Exception{
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] tickerSymbolBytes = tickerSymbol.getBytes();
        byte tickerSymbolSize = (byte)tickerSymbolBytes.length;
        outputStream.write(tickerSymbolSize);
        outputStream.write(tickerSymbolBytes);

        byte dataTypeByte = dataType.getByteValue();
        outputStream.write(dataTypeByte);

        return outputStream.toByteArray();
    }

    public static Transferable Deserialize(byte[] marketDataRequestBytes) throws Exception{
        ByteArrayInputStream inputStream = new ByteArrayInputStream(marketDataRequestBytes);

        int tickerSymbolSize = inputStream.read();
        byte[] tickerSymbolBuffer = new byte[tickerSymbolSize];
        inputStream.read(tickerSymbolBuffer, 0, tickerSymbolSize);
        String tickerSymbolT = new String(tickerSymbolBuffer);

        int typeInteger = inputStream.read();
        MarketDataType typeT = MarketDataType.valueOf(typeInteger);

        MareketDataRequestDTO DTO = new MareketDataRequestDTO(tickerSymbolT,typeT);
        return DTO;
    }

    public String getTickerSymbol() {
        return tickerSymbol;
    }

    public MarketDataType getDataType() {
        return dataType;
    }
}
