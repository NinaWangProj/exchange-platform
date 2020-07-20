package nw.ExchangePlatform.trading.limitOrderBook;

import javafx.util.Pair;
import nw.ExchangePlatform.trading.data.MarketParticipantOrder;

import java.util.ArrayList;

public class sortedOrderList {
    public ArrayList<MarketParticipantOrder> sortedList;
    public ArrayList<Pair<BookOperation, Object[]>> bookChanges;
    private OrderComparator comparator;

    public sortedOrderList(OrderComparator comparator) {
        sortedList = new ArrayList<MarketParticipantOrder>();
        bookChanges = new ArrayList<Pair<BookOperation, Object[]>>();
        this.comparator = comparator;
    }

    public sortedOrderList() {
        sortedList = new ArrayList<MarketParticipantOrder>();
        bookChanges = new ArrayList<Pair<BookOperation, Object[]>>();
    }

    public MarketParticipantOrder get(int ithElement) {
        MarketParticipantOrder order = sortedList.get(ithElement);
        return order;
    }

    public void remove(int ithElement) {
        sortedList.remove(ithElement);
        bookChanges.add(new Pair<BookOperation, Object[]>(BookOperation.REMOVE,new Object[]{ithElement}));
    }

    public void add(MarketParticipantOrder order) {
        //using bisection to determine where to insert the order
        Boolean iterate = true;
        int listStartIndex = 0;
        int listEndIndex = sortedList.size() - 1;

        while(iterate) {
            int listSize = listEndIndex - listStartIndex + 1;
            int partition = listStartIndex + (listEndIndex - listStartIndex) / 2;

            if(listSize == 0) {
                sortedList.add(0,order);
                bookChanges.add(new Pair<BookOperation, Object[]>(BookOperation.INSERT,new Object[]{0,order}));
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
                sortedList.add(index, order);
                bookChanges.add(new Pair<BookOperation, Object[]>(BookOperation.INSERT,new Object[]{index,order}));
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

    public ArrayList<Pair<BookOperation, Object[]>> GetChanges() {
        return bookChanges;
    }

    public void ClearTrackedChanges() {
        bookChanges.clear();
    }

    public int size() {
        return sortedList.size();
    }
}
