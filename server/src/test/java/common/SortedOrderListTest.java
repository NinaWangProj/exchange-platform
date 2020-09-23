package common;

import commonData.DTO.BookChangeDTO;
import commonData.Order.Direction;
import commonData.Order.MarketParticipantOrder;
import commonData.limitOrderBook.BookOperation;
import commonData.limitOrderBook.ChangeOperation;
import commonData.marketData.MarketDataItem;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import session.Session;
import trading.limitOrderBook.AskPriceTimeComparator;
import trading.limitOrderBook.OrderComparator;
import utility.OrderFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SortedOrderListTest {
    private String tickerSymbol;
    private Direction direction;
    private Session serverSession;

    @Test
    public void addToEmptyList_test() throws Exception {
        //test adding one order to an empty sorted order list;
        SortedOrderList orderList = Prepare();

        MarketParticipantOrder order = OrderFactory.ProduceOrder(1,tickerSymbol,direction);
        orderList.add(order);

        //baseline
        ArrayList<MarketParticipantOrder> expectedSortedList = new ArrayList<>();
        expectedSortedList.add(order);
        List<ChangeOperation> changes = new ArrayList<>();
        ChangeOperation change = new ChangeOperation(BookOperation.INSERT,0,
                new MarketDataItem(tickerSymbol,order.getSize(),order.getPrice()));
        changes.add(change);

        //compare sorted list
        Assertions.assertThat(expectedSortedList).
                usingRecursiveComparison().isEqualTo(orderList.getSortedList());
        //compare tracker changes
        ArgumentCaptor<BookChangeDTO> argCaptor = ArgumentCaptor.forClass(BookChangeDTO.class);
        verify(serverSession).On_ReceivingLevel3DataChanges((BookChangeDTO)argCaptor.capture());
        Assertions.assertThat(changes).
                usingRecursiveComparison().isEqualTo(argCaptor.getValue().getBookChanges());
    }

    @Test
    public void addToOddSizeList_test() throws Exception {
        //test adding one order to an odd size list; insert to middle index
        SortedOrderList orderList = Prepare();

        orderList.set(GenerateSampleList(5));


        MarketParticipantOrder order = OrderFactory.ProduceOrder(6,tickerSymbol,direction);
        orderList.add(order);

        //baseline
        ArrayList<MarketParticipantOrder> expectedSortedList = GenerateSampleList(5);
        expectedSortedList.add(1,order);
        List<ChangeOperation> changes = new ArrayList<>();
        ChangeOperation change = new ChangeOperation(BookOperation.INSERT,1,
                new MarketDataItem(tickerSymbol,order.getSize(),order.getPrice()));
        changes.add(change);

        //compare sorted list
        Assertions.assertThat(expectedSortedList).
                usingRecursiveComparison().isEqualTo(orderList.getSortedList());
        //compare tracker changes
        ArgumentCaptor<BookChangeDTO> argCaptor = ArgumentCaptor.forClass(BookChangeDTO.class);
        verify(serverSession).On_ReceivingLevel3DataChanges((BookChangeDTO)argCaptor.capture());
        Assertions.assertThat(changes).
                usingRecursiveComparison().isEqualTo(argCaptor.getValue().getBookChanges());
    }

    @Test
    public void addToEvenSizeList_test() throws Exception {
        //test adding one order to an odd size list; insert to middle index
        SortedOrderList orderList = Prepare();

        orderList.set(GenerateSampleList(4));

        MarketParticipantOrder order = OrderFactory.ProduceOrder(6,tickerSymbol,direction);
        orderList.add(order);

        //baseline
        ArrayList<MarketParticipantOrder> expectedSortedList = GenerateSampleList(4);
        expectedSortedList.add(1,order);
        List<ChangeOperation> changes = new ArrayList<>();
        ChangeOperation change = new ChangeOperation(BookOperation.INSERT,1,
                new MarketDataItem(tickerSymbol,order.getSize(),order.getPrice()));
        changes.add(change);

        //compare sorted list
        Assertions.assertThat(expectedSortedList).
                usingRecursiveComparison().isEqualTo(orderList.getSortedList());
        //compare tracker changes
        ArgumentCaptor<BookChangeDTO> argCaptor = ArgumentCaptor.forClass(BookChangeDTO.class);
        verify(serverSession).On_ReceivingLevel3DataChanges((BookChangeDTO)argCaptor.capture());
        Assertions.assertThat(changes).
                usingRecursiveComparison().isEqualTo(argCaptor.getValue().getBookChanges());
    }

    @Test
    public void addToEndOfList_test() throws Exception {
        //test adding one order to an odd size list; insert to middle index
        SortedOrderList orderList = Prepare();

        orderList.set(GenerateSampleList(4));

        MarketParticipantOrder order = OrderFactory.ProduceOrder(5,tickerSymbol,direction);
        orderList.add(order);

        //baseline
        ArrayList<MarketParticipantOrder> expectedSortedList = GenerateSampleList(5);
        List<ChangeOperation> changes = new ArrayList<>();
        ChangeOperation change = new ChangeOperation(BookOperation.INSERT,4,
                new MarketDataItem(tickerSymbol,order.getSize(),order.getPrice()));
        changes.add(change);

        //compare sorted list
        Assertions.assertThat(expectedSortedList).
                usingRecursiveComparison().isEqualTo(orderList.getSortedList());
        //compare tracker changes
        ArgumentCaptor<BookChangeDTO> argCaptor = ArgumentCaptor.forClass(BookChangeDTO.class);
        verify(serverSession).On_ReceivingLevel3DataChanges((BookChangeDTO)argCaptor.capture());
        Assertions.assertThat(changes).
                usingRecursiveComparison().isEqualTo(argCaptor.getValue().getBookChanges());
    }

    @Test
    public void remove_test() throws Exception {
        SortedOrderList orderList = Prepare();

        orderList.set(GenerateSampleList(4));

        orderList.remove(0);

        //baseline
        ArrayList<MarketParticipantOrder> expectedSortedList = new ArrayList<>();
        expectedSortedList.add(OrderFactory.ProduceOrder(2,tickerSymbol,direction));
        expectedSortedList.add(OrderFactory.ProduceOrder(3,tickerSymbol,direction));
        expectedSortedList.add(OrderFactory.ProduceOrder(4,tickerSymbol,direction));
        List<ChangeOperation> changes = new ArrayList<>();
        ChangeOperation change = new ChangeOperation(BookOperation.REMOVE,0, null);
        changes.add(change);

        //compare sorted list
        Assertions.assertThat(expectedSortedList).
                usingRecursiveComparison().isEqualTo(orderList.getSortedList());
        //compare tracker changes
        ArgumentCaptor<BookChangeDTO> argCaptor = ArgumentCaptor.forClass(BookChangeDTO.class);
        verify(serverSession).On_ReceivingLevel3DataChanges((BookChangeDTO)argCaptor.capture());
        Assertions.assertThat(changes).
                usingRecursiveComparison().isEqualTo(argCaptor.getValue().getBookChanges());

    }

    @Test
    public void removeFromEmptyList_test() throws Exception {
        SortedOrderList orderList = Prepare();

        orderList.remove(1);

        //baseline
        ArrayList<MarketParticipantOrder> expectedSortedList = new ArrayList<>();
        List<ChangeOperation> changes = null;

        //compare sorted list
        Assertions.assertThat(expectedSortedList).
                usingRecursiveComparison().isEqualTo(orderList.getSortedList());
    }

    @Test
    public void modify_test() throws Exception {
        Prepare();

        //test adding one order to an odd size list; insert to middle index
        SortedOrderList orderList = Prepare();
        orderList.set(GenerateSampleList(3));

        orderList.modify(1, "size", 500);

        //baseline
        ArrayList<MarketParticipantOrder> expectedSortedList = new ArrayList<>();
        expectedSortedList.add(OrderFactory.ProduceOrder(1,tickerSymbol,direction));
        MarketParticipantOrder expected2ndOrder = OrderFactory.ProduceOrder(7,tickerSymbol,direction);
        expectedSortedList.add(expected2ndOrder);
        expectedSortedList.add(OrderFactory.ProduceOrder(3,tickerSymbol,direction));
        ChangeOperation change = new ChangeOperation(BookOperation.MODIFY,1,
                new MarketDataItem(tickerSymbol,expected2ndOrder.getSize(),expected2ndOrder.getPrice()));
        List<ChangeOperation> changes = new ArrayList<>();
        changes.add(change);

        //compare sorted list
        Assertions.assertThat(expectedSortedList).
                usingRecursiveComparison().isEqualTo(orderList.getSortedList());
        //compare tracker changes
        ArgumentCaptor<BookChangeDTO> argCaptor = ArgumentCaptor.forClass(BookChangeDTO.class);
        verify(serverSession).On_ReceivingLevel3DataChanges((BookChangeDTO)argCaptor.capture());
        Assertions.assertThat(changes).
                usingRecursiveComparison().isEqualTo(argCaptor.getValue().getBookChanges());
    }

    @Test
    public void modifyIndexOutsideOfBound_test() throws Exception {
        Prepare();

        //test adding one order to an odd size list; insert to middle index
        SortedOrderList orderList = Prepare();
        orderList.set(GenerateSampleList(3));

        orderList.modify(10, "size", 500);

        //baseline
        ArrayList<MarketParticipantOrder> expectedSortedList = new ArrayList<>();
        expectedSortedList.add(OrderFactory.ProduceOrder(1,tickerSymbol,direction));
        MarketParticipantOrder expected2ndOrder = OrderFactory.ProduceOrder(2,tickerSymbol,direction);
        expectedSortedList.add(expected2ndOrder);
        expectedSortedList.add(OrderFactory.ProduceOrder(3,tickerSymbol,direction));

        //compare sorted list
        Assertions.assertThat(expectedSortedList).
                usingRecursiveComparison().isEqualTo(orderList.getSortedList());
    }

    private SortedOrderList Prepare() throws Exception {
        //mock session: implement void method of On_ReceivingLevel3DataChanges
        serverSession = mock(Session.class);

        doNothing().when(serverSession).On_ReceivingLevel3DataChanges(any(BookChangeDTO.class));

        //instantiate order list
        OrderComparator askComparator = new AskPriceTimeComparator();
        tickerSymbol = "FB";
        direction = Direction.SELL;
        ReadWriteLock lock = new ReentrantReadWriteLock();
        SortedOrderList orderList = new SortedOrderList(askComparator,lock,tickerSymbol,direction);

        orderList.RegisterSessionForContinuousData(serverSession);

        return orderList;
    }

    private ArrayList<MarketParticipantOrder> GenerateSampleList(int listSize) throws Exception {

        ArrayList<MarketParticipantOrder> sortedList = new ArrayList<>();
        for(int i = 1; i <=listSize; i ++)
        {
            sortedList.add(OrderFactory.ProduceOrder(i,tickerSymbol,direction));
        }

        return sortedList;
    }
}