package commonData.DTO;

import commonData.Order.Direction;
import commonData.Order.MarketParticipantOrder;
import commonData.limitOrderBook.BookOperation;
import commonData.limitOrderBook.ChangeOperation;
import commonData.marketData.MarketDataItem;
import javafx.util.Pair;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class BookChangeDTO implements Transferable{
    private DTOType dtoType;
    private List<ChangeOperation> bookChanges;
    private String tickerSymbol;
    private Direction direction;

    public BookChangeDTO(String tickerSymbol, Direction direction, List<ChangeOperation> bookChanges) {
        this.bookChanges = bookChanges;
        dtoType = DTOType.BookChanges;
        this.tickerSymbol = tickerSymbol;
        this.direction = direction;
    }

    public byte[] Serialize() throws Exception {

        int numOfChangeOperations = bookChanges.size();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] tickerSymbolByte = tickerSymbol.getBytes();
        byte tickerSize = (byte)tickerSymbolByte.length;
        outputStream.write(tickerSize);
        outputStream.write(tickerSymbolByte);

        outputStream.write(direction.getByteValue());

        byte[] totalOperationsByteSize = ByteBuffer.allocate(4).putInt(numOfChangeOperations).array();
        outputStream.write(totalOperationsByteSize);

        for (int i = 0; i < numOfChangeOperations; i++) {
            BookOperation operationType = bookChanges.get(i).Operation;
            outputStream.write(operationType.getByteValue());
            int index = (Integer) bookChanges.get(i).IndexOfChange;
            byte[] indexByteArray = ByteBuffer.allocate(4).putInt(index).array();
            outputStream.write(indexByteArray);

            switch (operationType) {
                case INSERT:
                    MarketDataItem order = bookChanges.get(i).AddedRow;
                    //Since we are transmitting market data, so order will be converted to market data item
                    MarketDataItemDTO dataItemDTO = new MarketDataItemDTO(order.getTickerSymbol(),
                            order.getSize(),order.getPrice());
                    byte[] orderByteArray = dataItemDTO.Serialize();
                    byte[] orderByteSize = ByteBuffer.allocate(4).putInt(orderByteArray.length).array();
                    outputStream.write(orderByteSize);
                    outputStream.write(orderByteArray);
                    break;
            }
        }
        byte[] bookChangesDTOByteArray = outputStream.toByteArray();

        return bookChangesDTOByteArray;
    }

    public static BookChangeDTO Deserialize(byte[] DTOByteArray) {
        ArrayList<ChangeOperation> bookChanges = new ArrayList<>();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(DTOByteArray);

        int tickerSymbolLength = inputStream.read();
        byte[] tickerSymbolBuffer = new byte[tickerSymbolLength];
        inputStream.read(tickerSymbolBuffer, 0, tickerSymbolLength);
        String tickerSymbolT = new String(tickerSymbolBuffer);

        Direction directionT = Direction.lookupEnumType(inputStream.read());

        byte[] numOfOperationsBuffer = new byte[4];
        inputStream.read(numOfOperationsBuffer,0,4);
        int numOfOperationsT = ByteBuffer.wrap(numOfOperationsBuffer).getInt();

        for(int i =0; i < numOfOperationsT; i++) { ;
            BookOperation operationTypeT = BookOperation.valueOf(inputStream.read());

            byte[] indexByteBuffer = new byte[4];
            inputStream.read(indexByteBuffer,0,4);
            int indexT = ByteBuffer.wrap(indexByteBuffer).getInt();

            switch(operationTypeT) {
                case REMOVE:
                    ChangeOperation removeOperation = new ChangeOperation(BookOperation.REMOVE, indexT, null);
                    bookChanges.add(removeOperation);
                    break;
                case INSERT:
                    byte[] dataItemByteSizeBuffer = new byte[4];
                    inputStream.read(dataItemByteSizeBuffer,0,4);
                    int dataItemSizeT = ByteBuffer.wrap(dataItemByteSizeBuffer).getInt();

                    byte[] dataItemBuffer = new byte[dataItemSizeT];
                    inputStream.read(dataItemBuffer,0,dataItemSizeT);
                    MarketDataItemDTO DataItemDTO = MarketDataItemDTO.Deserialize(dataItemBuffer);
                    MarketDataItem dataItem = new MarketDataItem(DataItemDTO.getTickerSymbol(),
                            DataItemDTO.getSize(),DataItemDTO.getPrice());

                    ChangeOperation insertOperation = new ChangeOperation(BookOperation.INSERT, indexT, dataItem);
                    bookChanges.add(insertOperation);
                    break;
            }
        }
        return new BookChangeDTO(tickerSymbolT, directionT, bookChanges);
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

    public String getTickerSymbol() {
        return tickerSymbol;
    }

    public List<ChangeOperation> getBookChanges() {
        return bookChanges;
    }

    public Direction getDirection() {
        return direction;
    }
}
