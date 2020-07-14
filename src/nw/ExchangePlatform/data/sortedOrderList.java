package nw.ExchangePlatform.data;

import javafx.util.Pair;

import java.util.ArrayList;

public class sortedOrderList {
    public ArrayList<MarketParticipantOrder> sortedList;
    public ChangeTracker tracker;

    public sortedOrderList() {
        sortedList = new ArrayList<MarketParticipantOrder>();
        tracker = new ChangeTracker();
    }

    public MarketParticipantOrder get(int ithElement) {
        MarketParticipantOrder order = sortedList.get(ithElement);
        tracker.SaveChanges(BookOperation.GET,ithElement);
        return order;
    }

    public void remove(int ithElement) {
        sortedList.remove(ithElement);
        tracker.SaveChanges(BookOperation.REMOVE,ithElement);
    }

    public void add(MarketParticipantOrder order) {


    }

    public ArrayList<Pair<BookOperation, Object>> GetChanges() {
        return tracker.getBookChanges();
    }
}
