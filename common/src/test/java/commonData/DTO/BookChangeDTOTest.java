package commonData.DTO;

import commonData.Order.Direction;
import commonData.Order.MarketParticipantOrder;
import commonData.Order.OrderDuration;
import commonData.Order.OrderType;
import commonData.limitOrderBook.BookOperation;
import commonData.limitOrderBook.ChangeOperation;
import commonData.marketData.MarketDataItem;
import javafx.util.Pair;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;

public class BookChangeDTOTest {

    @Test
    public void serialize() throws Exception{
        String tickerSymbol = "AAPL";
        Direction direction = Direction.SELL;
        ArrayList<ChangeOperation> bookChanges = new ArrayList<>();
        ArrayList<ChangeOperation> expectedBookChanges = new ArrayList<>();
        MarketDataItem marketDataItem1 = new MarketDataItem(tickerSymbol,400,262.5);
        ChangeOperation expectedOperation1 = new ChangeOperation(BookOperation.INSERT, 0, marketDataItem1);
        expectedBookChanges.add(expectedOperation1);

        BookChangeDTO expectedDTO = new BookChangeDTO(tickerSymbol,direction,expectedBookChanges);
        byte[] dTOByteArray = expectedDTO.Serialize();
        BookChangeDTO deserializedDTO = BookChangeDTO.Deserialize(dTOByteArray);

        Assertions.assertThat(deserializedDTO).usingRecursiveComparison().isEqualTo(expectedDTO);
    }
}