package clearing.engine;

import clearing.data.DTCCWarehouse;
import com.opencsv.bean.CsvToBeanBuilder;
import common.SortedOrderList;
import common.TradingOutput;
import common.Transaction;
import common.csv.CsvHelper;
import common.csv.MarketParticipantPortfolioRow;
import common.csv.DtccFromCsv;
import commonData.Order.Direction;
import commonData.Order.MarketParticipantOrder;
import commonData.clearing.MarketParticipantPortfolio;
import commonData.clearing.SecurityCertificate;
import javafx.util.Pair;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import trading.data.PendingOrder;
import trading.data.UnfilledOrder;
import trading.limitOrderBook.AskPriceTimeComparator;
import trading.limitOrderBook.BidPriceTimeComparator;
import trading.workflow.TradingEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClearingEngineTest {
    @Test
    public void SmallOrderBatchTest()
    {
        String initialPortfoliosFileName = "data/orderflow/small/Initial Portfolios1.csv";
        String transactionDataFileName = "data/orderflow/small/Transactions Data1.csv";
        String finalPortfoliosFileName = "data/orderflow/small/Final Portfolios1.csv";

        RunTest(initialPortfoliosFileName, transactionDataFileName, finalPortfoliosFileName);
    }

    @Test
    public void MediumOrderBatchTest()
    {
        String initialPortfoliosFileName = "data/orderflow/medium/Initial Portfolios2.csv";
        String transactionDataFileName = "data/orderflow/medium/Transactions Data2.csv";
        String finalPortfoliosFileName = "data/orderflow/medium/Final Portfolios2.csv";

        RunTest(initialPortfoliosFileName, transactionDataFileName, finalPortfoliosFileName);
    }

    @Test
    public void LargeOrderBatchTest()
    {
        String initialPortfoliosFileName = "data/orderflow/large/Initial Portfolios3.csv";
        String transactionDataFileName = "data/orderflow/large/Transactions Data3.csv";
        String finalPortfoliosFileName = "data/orderflow/large/Final Portfolios3.csv";

        RunTest(initialPortfoliosFileName, transactionDataFileName, finalPortfoliosFileName);
    }

    private void RunTest(String initialPortfoliosFileName, String transactionDataFileName, String finalPortfoliosFileName)
    {
        String testTicker = "AAPL";
        DTCCWarehouse dtccWarehouse  = DtccFromCsv.Get(initialPortfoliosFileName);

        ClearingEngine clearingEngine = new ClearingEngine(dtccWarehouse);
        // get inputs
        ArrayList<Transaction> inputTransactions = (ArrayList<Transaction>) CsvHelper.GetRowsFromCSV(transactionDataFileName, Transaction.class);

        clearingEngine.ClearTrade(inputTransactions);

        // get expected outputs
        DTCCWarehouse expectedFinalWarehouse = DtccFromCsv.Get(finalPortfoliosFileName);

        Assertions.assertThat(dtccWarehouse).usingRecursiveComparison().isEqualTo(expectedFinalWarehouse);
    }
}
