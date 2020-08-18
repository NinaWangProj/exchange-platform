package server.common;

import session.Session;
import commonData.Order.MarketParticipantOrder;
import commonData.limitOrderBook.BookOperation;
import javafx.util.Pair;
import trading.limitOrderBook.OrderComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

public class sortedOrderList {
    public ArrayList<MarketParticipantOrder> sortedList;
    private ChangeTracker tracker;
    private OrderComparator comparator;
    private ReadWriteLock lock;
    private String tickerSymbol;

    public sortedOrderList(OrderComparator comparator, ReadWriteLock lock, String tickerSymbol) {
        sortedList = new ArrayList<MarketParticipantOrder>();
        this.comparator = comparator;
        tracker = new ChangeTracker();
        this.lock = lock;
        this.tickerSymbol = tickerSymbol;
    }

    public sortedOrderList(OrderComparator comparator) {
        sortedList = new ArrayList<MarketParticipantOrder>();
        this.comparator = comparator;
        tracker = new ChangeTracker();
    }

    public MarketParticipantOrder get(int ithElement) {
        MarketParticipantOrder order = sortedList.get(ithElement);
        return order;
    }

    public void remove(int ithElement) throws Exception {
        lock.writeLock().lock();
        try {
            sortedList.remove(ithElement);
            tracker.Add(new Pair<BookOperation, Object[]>(BookOperation.REMOVE, new Object[]{ithElement}));
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
                    tracker.Add(new Pair<BookOperation, Object[]>(BookOperation.INSERT, new Object[]{0, order}));
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
                    tracker.Add(new Pair<BookOperation, Object[]>(BookOperation.INSERT, new Object[]{index, order}));
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

    public int size() {
        return sortedList.size();
    }

    public void RegisterSessionForContinuousData(Session session) {
        tracker.AttachObserver(session);
    }

    //subclass
    private class ChangeTracker {
        private List<Pair<BookOperation, Object[]>> bookChanges;
        private List<Session> observers;
        private String tickerSymbol;

        private ChangeTracker() {
            observers = Collections.synchronizedList(new ArrayList<Session>());
            bookChanges = Collections.synchronizedList(new ArrayList<Pair<BookOperation, Object[]>>());
        }

        private void Add(Pair<BookOperation, Object[]> change) throws Exception {
            bookChanges.add(change);
            NotifyObservers(bookChanges);
            ClearChanges();
        }

        private void ClearChanges() {
            bookChanges.clear();
        }

        private void AttachObserver(Session session) {
            observers.add(session);
        }

        private void NotifyObservers(List<Pair<BookOperation, Object[]>> bookChanges) throws Exception{
            for(Session session : observers) {
                session.On_ReceivingLevel3DataChanges(tickerSymbol, bookChanges);
            }
        }
    }
}
