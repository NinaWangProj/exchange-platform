package clearing.engine;

import clearing.data.DTCCWarehouse;
import common.*;
import common.csv.CsvHelper;
import common.csv.DtccFromCsv;
import common.utility.BinSelector;
import org.junit.jupiter.api.Test;
import trading.data.PendingOrder;
import trading.data.UnfilledOrder;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public class ClearingEngineManagerTest {
    @Test
    public void SmallOrderFlowTest() throws Exception {
        String orderDataFileName = "data/orderflow/multistock/small/Order Data4.csv";
        String transactionDataFileName = "data/orderflow/multistock/small/Transactions Data4.csv";
        String pendingDataFileName = "data/orderflow/multistock/small/Pending Order Data4.csv";
        String unfilledDataFileName = "data/orderflow/multistock/small/Unfilled Order Data4.csv";

        int numSimulatedTradingEngines = 3;
        int numInputOrderQueues = 3;
        int numSessions = 3;
        RunTest(numSimulatedTradingEngines, numInputOrderQueues, numSessions, orderDataFileName, transactionDataFileName,
                pendingDataFileName, unfilledDataFileName);
    }

    @Test
    public void MediumOrderFlowTest() throws Exception {
        String orderDataFileName = "data/orderflow/multistock/medium/Order Data5.csv";
        String transactionDataFileName = "data/orderflow/multistock/medium/Transactions Data5.csv";
        String pendingDataFileName = "data/orderflow/multistock/medium/Pending Order Data5.csv";
        String unfilledDataFileName = "data/orderflow/multistock/medium/Unfilled Order Data5.csv";

        int numSimulatedTradingEngines = 3;
        int numInputOrderQueues = 3;
        int numSessions = 3;
        RunTest(numSimulatedTradingEngines, numInputOrderQueues, numSessions, orderDataFileName, transactionDataFileName,
                pendingDataFileName, unfilledDataFileName);
    }

    @Test
    public void LargeOrderFlowTest() throws Exception {
        String orderDataFileName = "data/orderflow/multistock/large/Order Data6.csv";
        String transactionDataFileName = "data/orderflow/multistock/large/Transactions Data6.csv";
        String pendingDataFileName = "data/orderflow/multistock/large/Pending Order Data6.csv";
        String unfilledDataFileName = "data/orderflow/multistock/large/Unfilled Order Data6.csv";

        int numSimulatedTradingEngines = 3;
        int numInputOrderQueues = 3;
        int numSessions = 3;
        RunTest(numSimulatedTradingEngines, numInputOrderQueues, numSessions, orderDataFileName, transactionDataFileName,
                pendingDataFileName, unfilledDataFileName);
    }


    private void RunTest(int numSimulatedTradingGroups, int numTradingOutputQueues, int numSessions, String transactionDataFileName,
                         String pendingDataFileName, String unfilledDataFileName, String initialPortfoliosFileName) throws Exception {

        ServerQueue centralQueue = new ServerQueue(0, numTradingOutputQueues);
        DTCCWarehouse dtccWarehouse  = DtccFromCsv.Get(initialPortfoliosFileName);
        ClearingEngineManager clearingEngineManager = new ClearingEngineManager(centralQueue, dtccWarehouse);
        clearingEngineManager.Start();

        // Create Mock Session tasks to collect OrderStatus from queues
        ConcurrentMap<Integer, List<OrderStatus>> orderStatusFromQueue = new ConcurrentHashMap<>();
        ExecutorService executorService = Executors.newFixedThreadPool(numSessions);
        List<Future<Void>> sessionResultStates = new ArrayList<Future<Void>>();
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
            catch(CancellationException csex){}
        }

        //Collect any errors from Mock Session Tasks
        for(int i= 0; i < numSessions; i++){
            try {
                sessionResultStates.get(0).get(100, TimeUnit.MILLISECONDS);
            } catch(TimeoutException tex) {}
            catch(CancellationException csex){}
        }


        // get expected outputs


    }

    private Map<Integer, List<TradingOutput>>
    GetTradingOutputs(int numberOfTradingBins, String transactionDataFileName, String pendingDataFileName, String unfilledDataFileName) {

        Map<Integer, Map<Integer, TradingOutput>> outputByBinByOrder = new HashMap<>();

        List<Transaction> transactions = CsvHelper.GetRowsFromCSV(transactionDataFileName, Transaction.class);
        List<PendingOrder> pendingOrders = CsvHelper.GetRowsFromCSV(pendingDataFileName, PendingOrder.class);
        List<UnfilledOrder> unfilledOrders = CsvHelper.GetRowsFromCSV(unfilledDataFileName, UnfilledOrder.class);

        // group by bin and then orderID
        Map<Integer, Map<Integer, List<Transaction>>> groupedTransactions = transactions.stream()
                .collect(groupingBy(t -> BinSelector.ChooseAlphabeticalBin(t.getTickerSymbol(), numberOfTradingBins),
                        groupingBy(Transaction::getOrderID)));

        Map<Integer, Map<Integer, List<PendingOrder>>> groupedPendingOrders = pendingOrders.stream()
                .collect(groupingBy(p -> BinSelector.ChooseAlphabeticalBin(p.getTickerSymbol(), numberOfTradingBins),
                        groupingBy(PendingOrder::getOrderID)));

        Map<Integer, Map<Integer, List<UnfilledOrder>>> groupedUnfilledOrders = unfilledOrders.stream()
                .collect(groupingBy(u -> BinSelector.ChooseAlphabeticalBin(u.getTickerSymbol(), numberOfTradingBins),
                        groupingBy(UnfilledOrder::getOrderID)));

        TestingMapUtils.MergeNested(groupedTransactions, outputByBinByOrder,
                (orderId, tList) ->
                        new TradingOutput(orderId, (ArrayList<Transaction>)tList, new ArrayList<>(), new ArrayList<>()),
                (tList, tradeOutput) ->
                        new TradingOutput(tradeOutput.OrderID, (ArrayList<Transaction>)tList, tradeOutput.UnfilledOrders, tradeOutput.PendingOrders));

        TestingMapUtils.MergeNested(groupedUnfilledOrders, outputByBinByOrder,
                (orderId, uList) ->
                        new TradingOutput(orderId, new ArrayList<>(), (ArrayList<UnfilledOrder>)uList, new ArrayList<>()),
                (uList, tradeOutput) ->
                        new TradingOutput(tradeOutput.OrderID,  tradeOutput.Transactions, (ArrayList<UnfilledOrder>)uList, tradeOutput.PendingOrders));

        TestingMapUtils.MergeNested(groupedPendingOrders, outputByBinByOrder,
                (orderId, pList) ->
                        new TradingOutput(orderId, new ArrayList<>() , new ArrayList<>(), (ArrayList<PendingOrder>)pList),
                (pList, tradeOutput) ->
                        new TradingOutput(tradeOutput.OrderID,  tradeOutput.Transactions, tradeOutput.UnfilledOrders, (ArrayList<PendingOrder>)pList));


        Map<Integer, List<TradingOutput>> result = outputByBinByOrder.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey(),
                                            entry -> (List)entry.getValue().values()));

        return result;
    }


    private List<Transaction> GetAllTransactions(Map<Integer, List<TradingOutput>> tradingOutput){
        List<Transaction> allTransactions = new ArrayList<>();
        for(List<TradingOutput> outputForGroup : tradingOutput.values()){
            for (TradingOutput output: outputForGroup){
                allTransactions.addAll(output.Transactions);
            }
        }

        return allTransactions;
    }

    private List<UnfilledOrder> GetAllUnfilledOrders(Map<Integer, List<TradingOutput>> tradingOutput){
        List<UnfilledOrder> allUnfilledOrders = new ArrayList<>();
        for(List<TradingOutput> outputForGroup : tradingOutput.values()){
            for (TradingOutput output: outputForGroup){
                allUnfilledOrders.addAll(output.UnfilledOrders);
            }
        }

        return allUnfilledOrders;
    }

    private List<PendingOrder> GetAllPendingOrders(Map<Integer, List<TradingOutput>> tradingOutput){
        List<PendingOrder> allPendingOrders = new ArrayList<>();
        for(List<TradingOutput> outputForGroup : tradingOutput.values()){
            for (TradingOutput output: outputForGroup){
                allPendingOrders.addAll(output.PendingOrders);
            }
        }

        return allPendingOrders;
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
        private final ConcurrentMap<Integer, List<OrderStatus>> orderStatusBuffer;

        public MockSessionTask(ServerQueue centralQueue, Integer sessionId,
                                      ConcurrentMap<Integer, List<OrderStatus>> orderStatusBuffer)
        {
            this.centralQueue = centralQueue;
            this.sessionId = sessionId;
            this.orderStatusBuffer = orderStatusBuffer;
        }

        public Void call() throws Exception {

            while(true) {
                OrderStatus status = centralQueue.TakeOrderStatus(sessionId);
                orderStatusBuffer.putIfAbsent(sessionId, new ArrayList<>());
                orderStatusBuffer.get(sessionId).add(status);
            }
        }
    }

}
