package nw.ExchangePlatform.commonData.limitOrderBook;

import nw.ExchangePlatform.trading.data.MarketParticipantOrder;

import java.util.Comparator;

public interface OrderComparator{
    public int compare(MarketParticipantOrder order1, MarketParticipantOrder order2);
}
