package nw.ExchangePlatform.wrapper;

import nw.ExchangePlatform.data.MarketParticipantOrder;

import java.util.ArrayList;

public class OrderQueue {
    private ArrayList<MarketParticipantOrder> orders;

    public OrderQueue() {
        orders = new ArrayList<>();
    }

    public void AddOrderToQueue(MarketParticipantOrder order) {
        synchronized (this) {
            orders.add(order);
        }
    }

    public ArrayList<MarketParticipantOrder> GetQueue() {
        return orders;
    }


}
