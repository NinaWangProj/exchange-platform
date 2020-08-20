package commonData.limitOrderBook;

import commonData.Order.MarketParticipantOrder;
import commonData.marketData.MarketDataItem;

public class ChangeOperation {
    public final BookOperation Operation;
    public final int IndexOfChange;
    public final MarketDataItem AddedRow;

    public ChangeOperation(BookOperation operation, int indexOfChange, MarketDataItem addedRow)
    {
        this.Operation = operation;
        this.IndexOfChange = indexOfChange;
        this.AddedRow = addedRow;
    }
}
