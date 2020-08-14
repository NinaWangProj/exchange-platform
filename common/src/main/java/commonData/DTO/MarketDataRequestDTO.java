package commonData.DTO;

import commonData.DataType.MarketDataType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class MarketDataRequestDTO implements Transferable {
    private final Long clientRequestID;
    private final DTOType dtoType;
    private final String tickerSymbol;
    private final MarketDataType dataType;

    public MarketDataRequestDTO(Long clientRequestID, String tickerSymbol, MarketDataType dataType) {
        this.tickerSymbol = tickerSymbol;
        this.dataType = dataType;
        dtoType = DTOType.MareketDataRequest;
        this.clientRequestID = clientRequestID;
    }

    public byte[] Serialize() throws Exception{
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] requestIDByteArray = ByteBuffer.allocate(8).putLong(clientRequestID).array();
        outputStream.write(requestIDByteArray);

        byte[] tickerSymbolBytes = tickerSymbol.getBytes();
        byte tickerSymbolSize = (byte)tickerSymbolBytes.length;
        outputStream.write(tickerSymbolSize);
        outputStream.write(tickerSymbolBytes);

        byte dataTypeByte = dataType.getByteValue();
        outputStream.write(dataTypeByte);

        return outputStream.toByteArray();
    }

    public static MarketDataRequestDTO Deserialize(byte[] marketDataRequestBytes) throws Exception{
        ByteArrayInputStream inputStream = new ByteArrayInputStream(marketDataRequestBytes);

        byte[] requestIDBuffer = new byte[8];
        inputStream.read(requestIDBuffer, 0, 8);
        Long requestIDT = ByteBuffer.wrap(requestIDBuffer).getLong();

        int tickerSymbolSize = inputStream.read();
        byte[] tickerSymbolBuffer = new byte[tickerSymbolSize];
        inputStream.read(tickerSymbolBuffer, 0, tickerSymbolSize);
        String tickerSymbolT = new String(tickerSymbolBuffer);

        int typeInteger = inputStream.read();
        MarketDataType typeT = MarketDataType.valueOf(typeInteger);

        MarketDataRequestDTO DTO = new MarketDataRequestDTO(requestIDT,tickerSymbolT,typeT);
        return DTO;
    }

    public String getTickerSymbol() {
        return tickerSymbol;
    }

    public MarketDataType getDataType() {
        return dataType;
    }

    public DTOType getDtoType() {
        return dtoType;
    }

    public Long getClientRequestID() {
        return clientRequestID;
    }
}
