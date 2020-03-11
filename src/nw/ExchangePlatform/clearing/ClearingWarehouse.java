package nw.ExchangePlatform.clearing;

import nw.ExchangePlatform.data.DTCCWarehouse;
import nw.ExchangePlatform.data.Transaction;

import java.util.ArrayList;

public class ClearingWarehouse {

    public DTCCWarehouse dtccWarehouse;


    //need to persist DTCCWarehouse info & market participant portfolio later

    public void ClearTransactions(ArrayList<Transaction> transactions, DTCCWarehouse dtccWarehouse) {
        ClearingEngine clearingEngine = new ClearingEngine(transactions, dtccWarehouse);
        clearingEngine.ClearTrade();
    }

}
