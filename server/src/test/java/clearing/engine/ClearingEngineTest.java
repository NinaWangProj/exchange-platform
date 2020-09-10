package clearing.engine;

import clearing.data.DTCCWarehouse;
import com.opencsv.bean.CsvToBeanBuilder;
import common.SortedOrderList;
import common.TradingOutput;
import common.Transaction;
import common.csv.MarketParticipantPortfolioRow;
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

        List<MarketParticipantPortfolioRow> portfolioRows = GetRowsFromCSV(initialPortfoliosFileName, MarketParticipantPortfolioRow.class);
        DTCCWarehouse dtccWarehouse = GetDTCC(portfolioRows);

        ClearingEngine clearingEngine = new ClearingEngine(dtccWarehouse);

        // get inputs
        ArrayList<Transaction> inputTransactions = (ArrayList<Transaction>)GetRowsFromCSV(transactionDataFileName, Transaction.class);

        clearingEngine.ClearTrade(inputTransactions);

        // get expected outputs
        List<MarketParticipantPortfolioRow> finalPortfolioRows = GetRowsFromCSV(finalPortfoliosFileName, MarketParticipantPortfolioRow.class);
        DTCCWarehouse expectedFinalWarehouse = GetDTCC(finalPortfolioRows);

        Assertions.assertThat(dtccWarehouse).usingRecursiveComparison().isEqualTo(expectedFinalWarehouse);
    }

    private DTCCWarehouse GetDTCC(List<MarketParticipantPortfolioRow> portfolioRows) {

        HashMap<String, HashMap<Integer, SecurityCertificate>> certificatesMap = new HashMap<String, HashMap<Integer, SecurityCertificate>>();
        HashMap<Integer, MarketParticipantPortfolio> portfoliosMap = new HashMap<Integer, MarketParticipantPortfolio>();

        for (MarketParticipantPortfolioRow row :portfolioRows){
            if(!portfoliosMap.containsKey(row.userID))
                portfoliosMap.put(row.userID,
                        new MarketParticipantPortfolio(new HashMap<String, SecurityCertificate>(), row.cash));

            MarketParticipantPortfolio port = portfoliosMap.get(row.userID);
            port.getSecurities().put(row.tickerSymbol,
                    new SecurityCertificate(row.shareHolderName, row.shareHolderName, row.quantity, row.issuedDate));

            if(!certificatesMap.containsKey(row.tickerSymbol))
                certificatesMap.put(row.tickerSymbol, new HashMap<Integer, SecurityCertificate>());

            certificatesMap.get(row.tickerSymbol).put(row.userID,
                    new SecurityCertificate(row.shareHolderName, row.shareHolderName, row.quantity, row.issuedDate));
        }

        DTCCWarehouse warehouse = new DTCCWarehouse();
        warehouse.certificatesMap = certificatesMap;
        warehouse.portfoliosMap = portfoliosMap;
        return warehouse;
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
