package common;

import commonData.DTO.BookChangeDTO;
import commonData.Order.Direction;
import commonData.limitOrderBook.ChangeOperation;
import commonData.marketData.MarketDataItem;
import session.Session;
import commonData.Order.MarketParticipantOrder;
import commonData.limitOrderBook.BookOperation;
import javafx.util.Pair;
import trading.limitOrderBook.OrderComparator;

import java.awt.print.Book;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

public class SortedOrderList {
    private ArrayList<MarketParticipantOrder> sortedList;
    private ChangeTracker tracker;
    private OrderComparator comparator;
    private Direction direction;
    private ReadWriteLock lock;
    private String tickerSymbol;

    public SortedOrderList(OrderComparator comparator, ReadWriteLock lock, String tickerSymbol, Direction direction) {
        sortedList = new ArrayList<>();
        this.comparator = comparator;
        tracker = new ChangeTracker();
        this.lock = lock;
        this.tickerSymbol = tickerSymbol;
        this.direction = direction;
    }

    public SortedOrderList(OrderComparator comparator, Direction direction) {
        sortedList = new ArrayList<MarketParticipantOrder>();
        this.comparator = comparator;
        tracker = new ChangeTracker();
        this.direction = direction;
    }

    public MarketParticipantOrder get(int index) {
        MarketParticipantOrder order = null;
        if(index >= 0 && index < sortedList.size()) {
            order = sortedList.get(index);
        }
        return order;
    }

    //For testing purpose
    public void set(ArrayList<MarketParticipantOrder> sortedList) {
        this.sortedList = sortedList;
    }

    public void remove(int ithElement) throws Exception {
        lock.writeLock().lock();
        try {
            if(ithElement >=0 && ithElement <= sortedList.size() - 1) {
                sortedList.remove(ithElement);
                tracker.Add(new ChangeOperation(BookOperation.REMOVE, ithElement, null));
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void add(MarketParticipantOrder order) throws Exception{
        //using bisection to determine where to insert the order
        Boolean iterate = true;
        int listStartIndex = 0;
        int listEndIndex = -1;
        if(sortedList.size() != 0)
            listEndIndex = sortedList.size() - 1;

        while(iterate) {
            int listSize = listEndIndex - listStartIndex + 1;
            int partition = listStartIndex + (listEndIndex - listStartIndex) / 2;

            if(listSize == 0) {
                lock.writeLock().lock();
                try {
                    sortedList.add(listStartIndex, order);
                    tracker.Add(new ChangeOperation(BookOperation.INSERT, listStartIndex, new MarketDataItem(order)));
                } finally {
                    lock.writeLock().unlock();
                }
                break;
            }

            int compareIndicator = comparator.compare(order, sortedList.get(partition));
            if (compareIndicator > 0) {
                listEndIndex = partition - 1;
            } else {
                listStartIndex = partition + 1;
            }
        }
    }

    public <U> void modify(int index, String fieldName, U value) throws Exception {

        MarketParticipantOrder targetObject = null;

        if(index >= 0 && index < sortedList.size()){
            targetObject = sortedList.get(index);

            Class metaClass = targetObject.getClass();
            Field field = metaClass.getDeclaredField(fieldName);
            field.setAccessible(true);

            lock.writeLock().lock();
            try {
                field.set(targetObject, value);
                MarketDataItem modifiedItem = new MarketDataItem(tickerSymbol,targetObject.getSize(),targetObject.getPrice());
                tracker.Add(new ChangeOperation(BookOperation.MODIFY, index, modifiedItem));
            } catch (IllegalAccessException e) {
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    public int size() {
        return sortedList.size();
    }

    public void RegisterSessionForContinuousData(Session session) {
        tracker.AttachObserver(session);
    }

    public ArrayList<MarketParticipantOrder> getSortedList() {
        return sortedList;
    }

    //subclass
    private class ChangeTracker {
        private List<ChangeOperation> bookChanges;
        private List<Session> observers;

        private ChangeTracker() {
            /*observers = Collections.synchronizedList(new ArrayList<Session>());
            bookChanges = Collections.synchronizedList(new ArrayList<ChangeOperation>());*/

            observers = new ArrayList<Session>();
            bookChanges = new ArrayList<ChangeOperation>();
        }

        private void Add(ChangeOperation change) throws Exception {
            bookChanges.add(change);
            BookChangeDTO clonedBookChangeDTO = DeepCloneBookChanges(bookChanges);
            NotifyObservers(clonedBookChangeDTO);
            ClearChanges();
        }

        private void ClearChanges() {
            bookChanges.clear();
        }

        private void AttachObserver(Session session) {
            observers.add(session);
        }

        private void NotifyObservers(BookChangeDTO bookChangeDTO) throws Exception{
            for(Session session : observers) {
                session.On_ReceivingLevel3DataChanges(bookChangeDTO);
            }
        }

        private BookChangeDTO DeepCloneBookChanges(List<ChangeOperation> bookChanges) throws Exception {
            //serialize and deserialize to deep clone BookChangeDTO
            BookChangeDTO dto = new BookChangeDTO(tickerSymbol,direction,bookChanges);
            byte[] dtoByteArray = dto.Serialize();
            BookChangeDTO deepClonedDTO = BookChangeDTO.Deserialize(dtoByteArray);
            return deepClonedDTO;
        }
    }
}
