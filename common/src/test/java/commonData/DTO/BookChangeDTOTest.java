package commonData.DTO;

import commonData.Order.Direction;
import commonData.Order.MarketParticipantOrder;
import commonData.Order.OrderDuration;
import commonData.Order.OrderType;
import commonData.limitOrderBook.BookOperation;
import commonData.marketData.MarketDataItem;
import javafx.util.Pair;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BookChangeDTOTest {

    @Test
    public void serialize() throws Exception{
        long clientRequestID = 110;
        String tickerSymbol = "AAPL";
        ArrayList<Pair<BookOperation, Object[]>> bookChanges = new ArrayList<Pair<BookOperation, Object[]>>();
        ArrayList<Pair<BookOperation, Object[]>> expectedBookChanges = new ArrayList<Pair<BookOperation, Object[]>>();

        MarketParticipantOrder order1 = new MarketParticipantOrder(101,20,"user1",200, new Date(),
                Direction.BUY,tickerSymbol,400,262.5, OrderType.LIMITORDER, OrderDuration.DAY);
        /*MarketParticipantOrder order2 = new MarketParticipantOrder(150,22,"user2",201, new Date(),
                Direction.BUY,tickerSymbol,200,261.03, OrderType.LIMITORDER, OrderDuration.DAY);*/
        MarketDataItem marketDataItem1 = new MarketDataItem(tickerSymbol,400,262.5);
       /* MarketDataItem marketDataItem2 = new MarketDataItem(tickerSymbol,200,261.03);*/

        Pair<BookOperation, Object[]> operation1 = new Pair<>(BookOperation.INSERT, new Object[]{0,order1});
        /*Pair<BookOperation, Object[]> operation2 = new Pair<>(BookOperation.INSERT, new Object[]{1,order2});*/
        Pair<BookOperation, Object[]> expectedOperation1 = new Pair<>(BookOperation.INSERT, new Object[]{0,marketDataItem1});
        /*Pair<BookOperation, Object[]> expectedOperation2 = new Pair<>(BookOperation.INSERT, new Object[]{1,marketDataItem2});*/
        expectedBookChanges.add(expectedOperation1);
/*        expectedBookChanges.add(expectedOperation2);*/
        bookChanges.add(operation1);
/*        bookChanges.add(operation2);*/

        BookChangeDTO dto = new BookChangeDTO(tickerSymbol,bookChanges);
        byte[] dTOByteArray = dto.Serialize();
        BookChangeDTO deserializedDTO = BookChangeDTO.Deserialize(dTOByteArray);

        BookChangeDTO expectedDTO = new BookChangeDTO(tickerSymbol,expectedBookChanges);

        int expectedSize = expectedDTO.getBookChanges().size();
        int size = deserializedDTO.getBookChanges().size();


        Assertions.assertThat(deserializedDTO.getBookChanges()).usingRecursiveComparison().isEqualTo(expectedDTO.getBookChanges());

    }
}