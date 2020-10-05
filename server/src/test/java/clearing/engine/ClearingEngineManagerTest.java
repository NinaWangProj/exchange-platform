package clearing.engine;

import clearing.data.DTCCWarehouse;
import common.*;
import common.csv.CsvHelper;
import common.csv.DtccFromCsv;
import common.csv.SessionMessageMapRow;
import common.utility.BinSelector;
import common.utility.MessageJSONConstants;
import javafx.util.Pair;
import org.assertj.core.api.Assertions;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;
import trading.data.PendingOrder;
import trading.data.UnfilledOrder;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


public class ClearingEngineManagerTest {
    private final double threshold = 1e-6;

    @Test
    public void SmallOrderFlowTest() throws Exception {
        String transactionDataFileName = "data/orderflow/multistock/small/Transactions Data4.csv";
        String pendingDataFileName = "data/orderflow/multistock/small/Pending Order Data4.csv";
        String unfilledDataFileName = "data/orderflow/multistock/small/Unfilled Order Data4.csv";

        String initialPortfoliosFileName = "data/orderflow/multistock/small/Initial Portfolios4.csv";
        String finalPortfoliosFileName = "data/orderflow/multistock/small/Final Portfolios4.csv";
        String clientMessagesFileName = "data/orderflow/multistock/small/Messages4.csv";

        int numSimulatedTradingEngines = 3;
        int numClearingEngineQueues = 3;
        int numSessions = 6;
        RunTest(numSimulatedTradingEngines, numClearingEngineQueues, numSessions, transactionDataFileName, pendingDataFileName,
                unfilledDataFileName, initialPortfoliosFileName, finalPortfoliosFileName, clientMessagesFileName);
    }

    @Test
    public void MediumOrderFlowTest() throws Exception {
        String transactionDataFileName = "data/orderflow/multistock/medium/Transactions Data5.csv";
        String pendingDataFileName = "data/orderflow/multistock/medium/Pending Order Data5.csv";
        String unfilledDataFileName = "data/orderflow/multistock/medium/Unfilled Order Data5.csv";

        String initialPortfoliosFileName = "data/orderflow/multistock/medium/Initial Portfolios5.csv";
        String finalPortfoliosFileName = "data/orderflow/multistock/medium/Final Portfolios5.csv";
        String clientMessagesFileName = "data/orderflow/multistock/medium/Messages5.csv";

        int numSimulatedTradingEngines = 3;
        int numClearingEngineQueues = 5;
        int numSessions = 12;
        RunTest(numSimulatedTradingEngines, numClearingEngineQueues, numSessions, transactionDataFileName, pendingDataFileName,
                unfilledDataFileName, initialPortfoliosFileName, finalPortfoliosFileName, clientMessagesFileName);
    }

    @Test
    public void LargeOrderFlowTest() throws Exception {
        String transactionDataFileName = "data/orderflow/multistock/large/Transactions Data6.csv";
        String pendingDataFileName = "data/orderflow/multistock/large/Pending Order Data6.csv";
        String unfilledDataFileName = "data/orderflow/multistock/large/Unfilled Order Data6.csv";

        String initialPortfoliosFileName = "data/orderflow/multistock/large/Initial Portfolios6.csv";
        String finalPortfoliosFileName = "data/orderflow/multistock/large/Final Portfolios6.csv";
        String clientMessagesFileName = "data/orderflow/multistock/large/Messages6.csv";

        int numSimulatedTradingEngines = 3;
        int numClearingEngineQueues = 5;
        int numSessions = 12;
        RunTest(numSimulatedTradingEngines, numClearingEngineQueues, numSessions, transactionDataFileName, pendingDataFileName,
                unfilledDataFileName, initialPortfoliosFileName, finalPortfoliosFileName, clientMessagesFileName);
    }


    private void RunTest(int numSimulatedTradingGroups, int numTradingOutputQueues, int numSessions, String transactionDataFileName,
                         String pendingDataFileName, String unfilledDataFileName, String initialPortfoliosFileName,
                         String finalPortfoliosFileName, String clientMessagesFilePath) throws Exception {

        ServerQueue centralQueue = new ServerQueue(0, numTradingOutputQueues);
        DTCCWarehouse dtccWarehouse  = DtccFromCsv.Get(initialPortfoliosFileName);
        ClearingEngineManager clearingEngineManager = new ClearingEngineManager(centralQueue, dtccWarehouse);
        clearingEngineManager.Start();

        // Create Mock Session tasks to collect OrderStatus from queues
        // <sessionId, tickerSymbol>, OrderStatus
        ConcurrentMap<Pair<Integer, String>, List<OrderStatus>> orderStatusFromQueue = new ConcurrentHashMap<>();
        ExecutorService executorService = Executors.newFixedThreadPool(numSessions);
        List<Future<Void>> sessionResultStates = new ArrayList<>();
        for(int sessionId= 0; sessionId < numSessions; sessionId++){
            centralQueue.RegisterSessionWithQueue(sessionId);
            Future<Void> sessionResultState = executorService.submit(new MockSessionTask(centralQueue, sessionId, orderStatusFromQueue));
            sessionResultStates.add(sessionResultState);
        }

        // Submit Trade Outputs as input to workflow
        Map<Integer, List<TradingOutput>> tradingOutputByBin = GetTradingOutputs(numSimulatedTradingGroups,
                transactionDataFileName, pendingDataFileName, unfilledDataFileName);

        List<Callable<Void>> mockTradingEngineTasks = new ArrayList<>();
        for(int i= 0; i < numSimulatedTradingGroups; i++){
            mockTradingEngineTasks.add(new MockTradingEngineTask(centralQueue, i, tradingOutputByBin));
        }

        ExecutorService tradeEngineExecutorService = Executors.newFixedThreadPool(numSimulatedTradingGroups);
        List<Future<Void>> tradingTaskResultState = tradeEngineExecutorService.invokeAll(mockTradingEngineTasks, 100, TimeUnit.MILLISECONDS);

        //Collect any error from Mock TradingEngine tasks
        for(int i= 0; i < numSimulatedTradingGroups; i++){
            try {
                tradingTaskResultState.get(0).get(100, TimeUnit.MILLISECONDS);
            } catch(TimeoutException tex) {}
            catch(CancellationException canex){}
        }

        //Collect any errors from Mock Session Tasks
        for(int i= 0; i < numSessions; i++){
            try {
                sessionResultStates.get(0).get(100, TimeUnit.MILLISECONDS);
            }
//            catch(RuntimeException rex)
//            {
//                System.out.println("Exception from session Id: " + i);
//                rex.printStackTrace();
//            }
            catch(TimeoutException tex) {}
            catch(CancellationException csex){}
        }

        // Get expected outputs
        Map<Pair<Integer, String>, List<OrderStatus>> expectedMessageMap = GetClientMessages(clientMessagesFilePath);
        DTCCWarehouse expectedFinalWarehouse = DtccFromCsv.Get(finalPortfoliosFileName);

        Comparator<Double> closeEnough = (d1, d2) -> {
            if((d1 < threshold) && (d2 < threshold) ||
                (Math.abs((d1 - d2) / Math.max(d1, d2)) < threshold))
                return 0;
            else
                return 1;
        };

        JSONParser parser = new JSONParser();
        Comparator<String> jsonComparator = (js1, js2) -> {
            try {
                JSONObject jObj1 = (JSONObject)parser.parse(js1);
                JSONObject jObj2 = (JSONObject)parser.parse(js2);
                return jObj1.equals(jObj2) ? 0 : 1;
            } catch(ParseException pex)
            {
                System.out.println("Error parsing JSON string when asserting comparison");
                pex.printStackTrace();
                return 1;
            }
        };

        Assertions.assertThat(dtccWarehouse).usingRecursiveComparison().withComparatorForType(closeEnough, Double.class)
                .isEqualTo(expectedFinalWarehouse);
        Assertions.assertThat(orderStatusFromQueue).usingRecursiveComparison()
                .withComparatorForType(closeEnough, Double.class)
                .withComparatorForFields(jsonComparator, "statusMessage")
                .isEqualTo(expectedMessageMap);

    }

    private Map<Pair<Integer, String>, List<OrderStatus>> GetClientMessages(String clientMessagesFilePath) {
        List<SessionMessageMapRow> messageMapRows = CsvHelper.GetRowsFromCSV(clientMessagesFilePath, SessionMessageMapRow.class);

        Map<Pair<Integer, String>, List<OrderStatus>> messageMap = new HashMap<>();

        for(SessionMessageMapRow row : messageMapRows){
            Pair<Integer, String> key = new Pair<> (row.sessionID, row.tickerSymbol);
            if(!messageMap.containsKey(key))
                messageMap.put(key, new ArrayList<>());

            OrderStatus status = new OrderStatus(row.orderID, row.orderStatus, row.message);
            messageMap.get(key).add(status);
        }

        return messageMap;
    }


    private Map<Integer, List<TradingOutput>> GetTradingOutputs(int numberOfTradingBins, String transactionDataFileName,
                                                                String pendingDataFileName, String unfilledDataFileName) {

        Map<Integer, List<TradingOutput>> binToTradingOutputs = new HashMap<>();

        Comparator<Transaction> transactComparator = Comparator.comparing(Transaction::getTransactionID)
                .thenComparing(Comparator.comparing(Transaction::getOrderID).reversed());
        List<Transaction> transactions = CsvHelper.GetRowsFromCSV(transactionDataFileName, Transaction.class)
                .stream().sorted(transactComparator).collect(Collectors.toList());

        List<PendingOrder> pendingOrders = CsvHelper.GetRowsFromCSV(pendingDataFileName, PendingOrder.class)
                .stream().sorted(Comparator.comparing(PendingOrder::getOrderID)).collect(Collectors.toList());
        List<UnfilledOrder> unfilledOrders = CsvHelper.GetRowsFromCSV(unfilledDataFileName, UnfilledOrder.class)
                .stream().sorted(Comparator.comparing(UnfilledOrder::getOrderID)).collect(Collectors.toList());

        // Get list of all orderIds
        Set<Integer> orderIds = transactions.stream().map(Transaction::getOrderID).collect(Collectors.toSet());
        orderIds.addAll(pendingOrders.stream().map(PendingOrder::getOrderID).collect(Collectors.toSet()));
        orderIds.addAll(unfilledOrders.stream().map(UnfilledOrder::getOrderID).collect(Collectors.toSet()));
        List<Integer> sortedOrderIds = new ArrayList<>(orderIds);
        Collections.sort(sortedOrderIds);

        // Loop through orderIds and build TradingOutputs
        for (Integer orderId: sortedOrderIds) {
            ArrayList<Transaction> transactionForOutput = new ArrayList<>();
            ArrayList<PendingOrder> pendingOrderForOutput = new ArrayList<>();
            ArrayList<UnfilledOrder> unfilledOrderForOutput = new ArrayList<>();
            String tickerSymbol = "";

            while(transactions.size() > 0 && transactions.get(0).getOrderID() == orderId){
                tickerSymbol = transactions.get(0).getTickerSymbol();
                transactionForOutput.add(transactions.remove(0));
                transactionForOutput.add(transactions.remove(0));
            }

            if(pendingOrders.size() > 0 && pendingOrders.get(0).getOrderID() == orderId) {
                tickerSymbol = pendingOrders.get(0).getTickerSymbol();
                pendingOrderForOutput.add(pendingOrders.remove(0));
            }

            if(unfilledOrders.size() > 0 && unfilledOrders.get(0).getOrderID() == orderId){
                tickerSymbol = unfilledOrders.get(0).getTickerSymbol();
                unfilledOrderForOutput.add(unfilledOrders.remove(0));
            }

            TradingOutput output = new TradingOutput(orderId, transactionForOutput, unfilledOrderForOutput, pendingOrderForOutput);
            int binId = BinSelector.ChooseAlphabeticalBin(tickerSymbol, numberOfTradingBins);

            if(!binToTradingOutputs.containsKey(binId))
                binToTradingOutputs.put(binId, new ArrayList<>());

            binToTradingOutputs.get(binId).add(output);
        }

        return binToTradingOutputs;
    }

    public class MockTradingEngineTask implements Callable<Void> {

        private final ServerQueue centralQueue;
        private final Integer tradingEngineId;
        private final Map<Integer, List<TradingOutput>> tradingOutputStore;

        public MockTradingEngineTask(ServerQueue centralQueue, Integer tradingEngineId,
                                      Map<Integer, List<TradingOutput>> tradingOutputStore)
        {
            this.centralQueue = centralQueue;
            this.tradingEngineId = tradingEngineId;
            this.tradingOutputStore = tradingOutputStore;
        }

        public Void call() throws Exception {
            List<TradingOutput> outputs = tradingOutputStore.get(tradingEngineId);

            for (TradingOutput output : outputs) {
                centralQueue.PutTradingResult(output);
            }

            return null;
        }
    }


    public class MockSessionTask implements Callable<Void> {

        private final ServerQueue centralQueue;
        private final Integer sessionId;
        private final ConcurrentMap<Pair<Integer, String>, List<OrderStatus>> orderStatusBuffer;
        private JSONParser parser = new JSONParser();

        public MockSessionTask(ServerQueue centralQueue, Integer sessionId,
                                      ConcurrentMap<Pair<Integer, String>, List<OrderStatus>> orderStatusBuffer)
        {
            this.centralQueue = centralQueue;
            this.sessionId = sessionId;
            this.orderStatusBuffer = orderStatusBuffer;
        }

        public Void call() throws Exception {

            while(true) {
                OrderStatus status = centralQueue.TakeOrderStatus(sessionId);
                JSONObject jsonObj = (JSONObject)parser.parse(status.getStatusMessage());

                Pair<Integer, String> sessionTickerPair = new Pair<>
                        (sessionId, (String)jsonObj.get(MessageJSONConstants.tickerSymbol));

                orderStatusBuffer.putIfAbsent(sessionTickerPair, new ArrayList<>());
                orderStatusBuffer.get(sessionTickerPair).add(status);
            }
        }
    }

}
