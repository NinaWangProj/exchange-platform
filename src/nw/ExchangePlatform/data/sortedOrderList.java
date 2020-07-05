package nw.ExchangePlatform.data;

import java.util.ArrayList;

public class sortedOrderList {
    public ArrayList<MarketParticipantOrder> sortedList;
    public ChangeTracker tracker;

    public sortedOrderList() {
        sortedList = new ArrayList<MarketParticipantOrder>();
    }

    public MarketParticipantOrder get(int ithElement) {
        return sortedList.get(ithElement);
    }

    public void remove(int ithElement) {

    }

    public void add(MarketParticipantOrder order) {


    }

    public void PersistChangeRecord() {

    }

    public void GetChanges() {

    }

    public void GetBookSnapShot() {

    }
}
