package nw.ExchangePlatform.utility;


public class PendingOrderMessenger extends Messenger{
    public void AddMessages(String userName,int orderID,String tickerSymbol,int size,
                            double tradePrice, String pendingReason) {
        String message = "Dear " + userName + " Your order with orderID: " + orderID
                + " is in pending now for the resason " + pendingReason;
        messages.add(message);
    }
}
