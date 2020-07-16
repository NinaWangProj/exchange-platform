package nw.ExchangePlatform.commonData.limitOrderBook;

import nw.ExchangePlatform.trading.data.MarketParticipantOrder;

import java.util.Comparator;

public class BidPriceTimeComparator implements Comparator<MarketParticipantOrder>, OrderComparator {
    public int compare(MarketParticipantOrder order1, MarketParticipantOrder order2) {
        if(order1.getPrice() > order2.getPrice()) {
            return 1;
        } else if (order1.getPrice() < order2.getPrice()) {
            return -1;
        } else {
            if(order1.getTime().before(order2.getTime())) {
                return 1;
            } else {
                return -1;
            }
        }
    }
}
