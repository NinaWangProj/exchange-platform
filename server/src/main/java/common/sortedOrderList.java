package common;

import commonData.Order.Direction;
import commonData.limitOrderBook.ChangeOperation;
import commonData.marketData.MarketDataItem;
import session.Session;
import commonData.Order.MarketParticipantOrder;
import commonData.limitOrderBook.BookOperation;
import javafx.util.Pair;
import trading.limitOrderBook.OrderComparator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

public class sortedOrderList {
    public ArrayList<MarketParticipantOrder> sortedList;
    private ChangeTracker tracker;
    private OrderComparator comparator;
    private Direction direction;
    private ReadWriteLock lock;
    private String tickerSymbol;

    public sortedOrderList(OrderComparator comparator, ReadWriteLock lock, String tickerSymbol, Direction direction) {
        sortedList = new ArrayList<>();
        this.comparator = comparator;
        tracker = new ChangeTracker();
        this.lock = lock;
        this.tickerSymbol = tickerSymbol;
        this.direction = direction;
    }

    public sortedOrderList(OrderComparator comparator, Direction direction) {
        sortedList = new ArrayList<MarketParticipantOrder>();
        this.comparator = comparator;
        tracker = new ChangeTracker();
        this.direction = direction;
    }

    public MarketParticipantOrder get(int ithElement) {
        MarketParticipantOrder order = sortedList.get(ithElement);
        return order;
    }

    public void remove(int ithElement) throws Exception {
        lock.writeLock().lock();
        try {
            sortedList.remove(ithElement);
            tracker.Add(new ChangeOperation(BookOperation.REMOVE, ithElement, null));
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void add(MarketParticipantOrder order) throws Exception{
        //using bisection to determine where to insert the order
        Boolean iterate = true;
        int listStartIndex = 0;
        int listEndIndex = sortedList.size() - 1;

        while(iterate) {
            int listSize = listEndIndex - listStartIndex + 1;
            int partition = listStartIndex + (listEndIndex - listStartIndex) / 2;

            if(listSize == 0) {
                lock.writeLock().lock();
                try {
                    sortedList.add(0, order);
                    tracker.Add(new ChangeOperation(BookOperation.INSERT, 0, new MarketDataItem(order)));
                } finally {
                    lock.writeLock().unlock();
                }
                break;
            }

            if(listSize == 1) {
                int compareIndicator = comparator.compare(order, sortedList.get(partition));
                int index;
                if (compareIndicator > 0) {
                    index = partition;
                } else {
                    index = partition + 1;
                }
                lock.writeLock().lock();
                try {
                    sortedList.add(index, order);
                    tracker.Add(new ChangeOperation(BookOperation.INSERT, index, new MarketDataItem(order)));
                } finally {
                    lock.writeLock().unlock();
                }
                break;
            }

            int compareIndicator = comparator.compare(order, sortedList.get(partition));
            if (compareIndicator > 0) {
                listEndIndex = partition - 1;
            } else {
                listStartIndex = partition;
            }
        }
    }

    public <U> void modify(int index, String fieldName, U value) throws Exception {
        MarketParticipantOrder targetObject = sortedList.get(index);

        Class metaClass = targetObject.getClass();
        Field field = metaClass.getDeclaredField(fieldName);
        field.setAccessible(true);

        lock.writeLock().lock();
        try {
            field.set(targetObject, value);
            MarketDataItem modifiedItem = new MarketDataItem(tickerSymbol,targetObject.getSize(),targetObject.getPrice());
            tracker.Add(new ChangeOperation(BookOperation.MODIFY, index, modifiedItem));
        } catch (IllegalAccessException e) {
        }
    }

    public int size() {
        return sortedList.size();
    }

    public void RegisterSessionForContinuousData(Session session) {
        tracker.AttachObserver(session);
    }

    //subclass
    private class ChangeTracker {
        private List<ChangeOperation> bookChanges;
        private List<Session> observers;

        private ChangeTracker() {
            observers = Collections.synchronizedList(new ArrayList<Session>());
            bookChanges = Collections.synchronizedList(new ArrayList<ChangeOperation>());
        }

        private void Add(ChangeOperation change) throws Exception {
            bookChanges.add(change);
            NotifyObservers(direction, bookChanges);
            ClearChanges();
        }

        private void ClearChanges() {
            bookChanges.clear();
        }

        private void AttachObserver(Session session) {
            observers.add(session);
        }

        private void NotifyObservers(Direction direction, List<ChangeOperation> bookChanges) throws Exception{
            for(Session session : observers) {
                session.On_ReceivingLevel3DataChanges(tickerSymbol, direction, bookChanges);
            }
        }
    }
}
