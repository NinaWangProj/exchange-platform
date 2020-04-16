package nw.ExchangePlatform.utility;

import java.util.ArrayList;

public class TransactionMessenger extends Messenger{
    public void AddMessages(String userName,int orderID,String tickerSymbol,int size,
                            double tradePrice, String reason) {
        String message = "Congradulation!  " + userName + " Your order with orderID: " + orderID
                + " has been filled with " + size + " shares, @$" + tradePrice + " per share.";
        messages.add(message);
    }
}
