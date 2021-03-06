package commonData.DTO;

import commonData.marketData.MarketDataItem;
import javafx.util.Pair;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class MarketDataDTO implements Transferable{
    private DTOType dtoType;
    private String tickerSymbol;
    private ArrayList<MarketDataItem> bids;
    private ArrayList<MarketDataItem> asks;
    private Long clientRequestID;


    public MarketDataDTO(Long clientRequestID, String tickerSymbol, ArrayList<MarketDataItem> bids, ArrayList<MarketDataItem> asks) {
        this.bids = bids;
        this.asks = asks;
        dtoType = DTOType.MarketData;
        this.tickerSymbol = tickerSymbol;
        this.clientRequestID = clientRequestID;
    }

    public byte[] Serialize() throws Exception {

        byte[] marketDataDTOByteArray = null;
        int numOfBids = bids.size();
        int numOfAsks = asks.size();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] requestIDByteArray = ByteBuffer.allocate(8).putLong(clientRequestID).array();
        outputStream.write(requestIDByteArray);

        byte[] tickerSymbolByte = tickerSymbol.getBytes();
        byte tickerSize = (byte)tickerSymbolByte.length;
        outputStream.write(tickerSize);
        outputStream.write(tickerSymbolByte);

        byte[] totalBidsByteSize = ByteBuffer.allocate(4).putInt(numOfBids).array();
        byte[] totalAsksByteSize = ByteBuffer.allocate(4).putInt(numOfAsks).array();
        outputStream.write(totalBidsByteSize);
        outputStream.write(totalAsksByteSize);

        //serialize bids
        if(numOfBids != 0) {
            for (int i = 0; i < numOfBids; i++) {
                byte[] sizeByte = ByteBuffer.allocate(4).putInt(bids.get(i).getSize()).array();
                outputStream.write(sizeByte);

                byte[] priceByte = ByteBuffer.allocate(8).putDouble(bids.get(i).getPrice()).array();
                outputStream.write(priceByte);
            }
        }
        //serialize asks
        if(numOfAsks != 0) {
            for (int j = 0; j < numOfAsks; j++) {
                byte[] sizeByte = ByteBuffer.allocate(4).putInt(asks.get(j).getSize()).array();
                outputStream.write(sizeByte);

                byte[] priceByte = ByteBuffer.allocate(8).putDouble(asks.get(j).getPrice()).array();
                outputStream.write(priceByte);
            }
        }

        marketDataDTOByteArray = outputStream.toByteArray();

        return marketDataDTOByteArray;
    }

    public static MarketDataDTO Deserialize(byte[] DTOByteArray) throws Exception{
        ByteArrayInputStream inputStream = new ByteArrayInputStream(DTOByteArray);
        ArrayList<MarketDataItem> bids = new ArrayList<MarketDataItem>();
        ArrayList<MarketDataItem> asks = new ArrayList<MarketDataItem>();

        byte[] requestIDBuffer = new byte[8];
        inputStream.read(requestIDBuffer, 0, 8);
        Long requestIDT = ByteBuffer.wrap(requestIDBuffer).getLong();

        int tickerSymbolLength = inputStream.read();
        byte[] tickerSymbolBuffer = new byte[tickerSymbolLength];
        inputStream.read(tickerSymbolBuffer, 0, tickerSymbolLength);
        String tickerSymbolT = new String(tickerSymbolBuffer);

        byte[] numOfBidsBuffer = new byte[4];
        inputStream.read(numOfBidsBuffer,0,4);
        int bidSizeT = ByteBuffer.wrap(numOfBidsBuffer).getInt();

        byte[] numOfAsksBuffer = new byte[4];
        inputStream.read(numOfAsksBuffer,0,4);
        int askSizeT = ByteBuffer.wrap(numOfAsksBuffer).getInt();

        if(bidSizeT != 0) {
            for(int i = 0; i < bidSizeT; i++) {
                MarketDataItem dataItem = DeserializeDataItem(inputStream, tickerSymbolT);
                bids.add(dataItem);
            }
        }

        if(askSizeT != 0) {
            for(int j = 0; j < askSizeT; j++) {
                MarketDataItem dataItem = DeserializeDataItem(inputStream, tickerSymbolT);
                asks.add(dataItem);
            }
        }

        MarketDataDTO DTO = new MarketDataDTO(requestIDT,tickerSymbolT,bids,asks);

        return DTO;
    }

    private static MarketDataItem DeserializeDataItem(ByteArrayInputStream inputStream, String tickerSymbolT) {
        byte[] sizeBuffer = new byte[4];
        inputStream.read(sizeBuffer,0,4);
        int sizeT = ByteBuffer.wrap(sizeBuffer).getInt();

        byte[] priceBuffer = new byte[8];
        inputStream.read(priceBuffer, 0, 8);
        double priceT = ByteBuffer.wrap(priceBuffer).getDouble();

        MarketDataItem dataItem = new MarketDataItem(tickerSymbolT,sizeT,priceT);
        return dataItem;
    }

    public DTOType getDtoType() {
        return dtoType;
    }

    public ArrayList<MarketDataItem> getBids() {
        return bids;
    }

    public ArrayList<MarketDataItem> getAsks() {
        return asks;
    }

    public String getTickerSymbol() {
        return tickerSymbol;
    }

    public Long getClientRequestID() {
        return clientRequestID;
    }

}
