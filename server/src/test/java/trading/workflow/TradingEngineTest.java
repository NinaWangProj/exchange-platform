package trading.workflow;

import common.SortedOrderList;
import common.TradingOutput;
import common.Transaction;
import commonData.Order.Direction;
import commonData.Order.MarketParticipantOrder;
import javafx.util.Pair;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import trading.data.PendingOrder;
import trading.data.UnfilledOrder;
import trading.limitOrderBook.AskPriceTimeComparator;
import trading.limitOrderBook.BidPriceTimeComparator;
import trading.limitOrderBook.OrderComparator;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.FileNotFoundException;
import java.io.FileReader;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class TradingEngineTest {

    @Test
    public void SmallOrderBatchTest()
    {
        String testTicker = "AAPL";
        Pair<SortedOrderList, SortedOrderList> limitOrderBook = new Pair<>(
                        new SortedOrderList(new BidPriceTimeComparator(), Direction.BUY),
                        new SortedOrderList(new AskPriceTimeComparator(), Direction.SELL));

        TradingEngine tradingEngine = new TradingEngine(testTicker,limitOrderBook);

        // inputs
        String orderDataFileName = "data/orderflow/small/Order Data.csv";
        List<MarketParticipantOrder> orderFlow = GetRowsFromCSV(orderDataFileName, MarketParticipantOrder.class);

        TradingOutput testResult = null;
        try {
            testResult = tradingEngine.ProcessBatch((ArrayList<MarketParticipantOrder>) orderFlow);
        }
        catch(Exception ex) {assert false: "Exception in Trading Engine";}

        // expected output
        String transactionDataFileName = "data/orderflow/small/Transaction Data.csv";
        String pendingDataFileName = "data/orderflow/small/Pending Order Data.csv";
        String unfilledDataFileName = "data/orderflow/small/Unfilled Order Data.csv";

        List<Transaction> expectedTransactions = GetRowsFromCSV(transactionDataFileName, Transaction.class);
        List<PendingOrder> expectedPendingOrders = GetRowsFromCSV(pendingDataFileName, PendingOrder.class);
        List<UnfilledOrder> expectedUnfilledOrders = GetRowsFromCSV(unfilledDataFileName, UnfilledOrder.class);

        Assertions.assertThat(testResult.Transactions).usingRecursiveComparison().isEqualTo(expectedTransactions);
        Assertions.assertThat(testResult.PendingOrders).usingRecursiveComparison().isEqualTo(expectedPendingOrders);
        Assertions.assertThat(testResult.UnfilledOrders).usingRecursiveComparison().isEqualTo(expectedUnfilledOrders);
    }

    @Test
    public void LargeOrderBatchTest()
    {

    }

    private <T> List<T> GetRowsFromCSV(String resourcePath, Class<T> type)
    {
        ClassLoader classLoader = getClass().getClassLoader();


        FileReader reader = null;
        try {
            File file = new File(classLoader.getResource(resourcePath).toURI());
            reader = new FileReader(file);
        }
        catch(FileNotFoundException | URISyntaxException fex) {
            assert false: "Cannot find resource file " + resourcePath;
            return new ArrayList<T>();
        }

        List<T> beans = new CsvToBeanBuilder(reader)
                .withType(type).build().parse();

        return beans;
    }
}
