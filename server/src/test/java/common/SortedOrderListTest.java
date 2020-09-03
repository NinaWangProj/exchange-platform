package common;

import commonData.Order.Direction;
import commonData.Order.MarketParticipantOrder;
import commonData.limitOrderBook.BookOperation;
import commonData.limitOrderBook.ChangeOperation;
import commonData.marketData.MarketDataItem;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import session.Session;
import trading.limitOrderBook.AskPriceTimeComparator;
import trading.limitOrderBook.OrderComparator;
import utility.OrderFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SortedOrderListTest {
    @Captor

    private SortedOrderList orderList;
    private String tickerSymbol;
    private Direction direction;
    private Session serverSession;

    @Test
    public void add_test() throws Exception {
        //test adding one order to an empty sorted order list;
        Prepare();

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
        ArgumentCaptor<List> arg3Captor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Direction> arg2Captor = ArgumentCaptor.forClass(Direction.class);
        ArgumentCaptor<String> arg1Captor = ArgumentCaptor.forClass(String.class);
        verify(serverSession).On_ReceivingLevel3DataChanges(arg1Captor.capture(), arg2Captor.capture(),(List<ChangeOperation>)arg3Captor.capture());
    }

   /* @Test
    public void remove_test() throws Exception {
        Prepare();

        orderList.

    }

    @Test
    public void modify_test() throws Exception {
        Prepare();

        orderList.

    }*/

    private void Prepare() throws Exception {
        //mock session: implement void method of On_ReceivingLevel3DataChanges
        serverSession = mock(Session.class);



        doNothing().when(serverSession).On_ReceivingLevel3DataChanges(any(String.class), any(Direction.class),
                any(ArrayList.class));


       /* doAnswer((i) -> {
            Object[] args = i.getArguments();
            ArrayList<ChangeOperation> changes = (ArrayList<ChangeOperation>) args[2];
            changeOperations = changes;
            return null;
        }).when(serverSession).On_ReceivingLevel3DataChanges(any(String.class),
                any(Direction.class), any(ArrayList.class));*/

        //instantiate order list
        OrderComparator askComparator = new AskPriceTimeComparator();
        tickerSymbol = "FB";
        direction = Direction.SELL;
        ReadWriteLock lock = new ReentrantReadWriteLock();
        orderList = new SortedOrderList(askComparator,lock,tickerSymbol,direction);

        orderList.RegisterSessionForContinuousData(serverSession);
    }
}