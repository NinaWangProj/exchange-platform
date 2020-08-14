package trading.limitOrderBook;


import commonData.Order.MarketParticipantOrder;

public interface OrderComparator{
    public int compare(MarketParticipantOrder order1, MarketParticipantOrder order2);
}
