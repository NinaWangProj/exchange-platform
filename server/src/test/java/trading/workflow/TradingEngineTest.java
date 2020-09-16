package trading.workflow;

import common.SortedOrderList;
import common.TradingOutput;
import common.Transaction;
import commonData.DTO.MarketDataRequestDTO;
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
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TradingEngineTest {

    @Test
    public void SmallOrderBatchTest() {
        String orderDataFileName = "data/orderflow/small/Order Data1.csv";
        String transactionDataFileName = "data/orderflow/small/Transactions Data1.csv";
        String pendingDataFileName = "data/orderflow/small/Pending Order Data1.csv";
        String unfilledDataFileName = "data/orderflow/small/Unfilled Order Data1.csv";

        RunTest(orderDataFileName, transactionDataFileName, pendingDataFileName, unfilledDataFileName);
   }

    @Test
    public void MediumOrderBatchTest()
    {
        String orderDataFileName = "data/orderflow/medium/Order Data2.csv";
        String transactionDataFileName = "data/orderflow/medium/Transactions Data2.csv";
        String pendingDataFileName = "data/orderflow/medium/Pending Order Data2.csv";
        String unfilledDataFileName = "data/orderflow/medium/Unfilled Order Data2.csv";

        RunTest(orderDataFileName, transactionDataFileName, pendingDataFileName, unfilledDataFileName);
    }

    @Test
    public void LargeOrderBatchTest()
    {
        String orderDataFileName = "data/orderflow/large/Order Data3.csv";
        String transactionDataFileName = "data/orderflow/large/Transactions Data3.csv";
        String pendingDataFileName = "data/orderflow/large/Pending Order Data3.csv";
        String unfilledDataFileName = "data/orderflow/large/Unfilled Order Data3.csv";

        RunTest(orderDataFileName, transactionDataFileName, pendingDataFileName, unfilledDataFileName);
    }

    private void RunTest(String orderDataFileName, String transactionDataFileName, String pendingDataFileName,
                              String unfilledDataFileName)
    {
        String testTicker = "AAPL";
        Pair<SortedOrderList, SortedOrderList> limitOrderBook = new Pair<>(
                new SortedOrderList(new BidPriceTimeComparator(), new ReentrantReadWriteLock(),testTicker, Direction.BUY),
                new SortedOrderList(new AskPriceTimeComparator(), new ReentrantReadWriteLock(),testTicker, Direction.SELL));

        TradingEngine tradingEngine = new TradingEngine(testTicker,limitOrderBook);

        // get inputs
        List<MarketParticipantOrder> orderFlow = GetRowsFromCSV(orderDataFileName, MarketParticipantOrder.class);

        TradingOutput testResult = null;
        try {
            testResult = tradingEngine.ProcessBatch((ArrayList<MarketParticipantOrder>) orderFlow);
        }
        catch(Exception ex) {assert false: "Exception in Trading Engine";}

        String test = System.getProperty("user.dir");

        // get expected outputs
        List<Transaction> expectedTransactions = GetRowsFromCSV(transactionDataFileName, Transaction.class);
        List<PendingOrder> expectedPendingOrders = GetRowsFromCSV(pendingDataFileName, PendingOrder.class);
        List<UnfilledOrder> expectedUnfilledOrders = GetRowsFromCSV(unfilledDataFileName, UnfilledOrder.class);

        Assertions.assertThat(testResult.Transactions).usingRecursiveComparison().ignoringFields("time")
                .isEqualTo(expectedTransactions);
        Assertions.assertThat(testResult.PendingOrders).usingRecursiveComparison().ignoringFields("time")
                .isEqualTo(expectedPendingOrders);
        Assertions.assertThat(testResult.UnfilledOrders).usingRecursiveComparison().ignoringFields("time")
                .isEqualTo(expectedUnfilledOrders);
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
