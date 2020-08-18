package clearing.data;

import clearing.engine.ClearingEngine;
import server.common.Transaction;


import java.util.ArrayList;

public class ClearingWarehouse {

    public DTCCWarehouse dtccWarehouse;

    public ClearingWarehouse(DTCCWarehouse dtccWarehouse) {
        this.dtccWarehouse = dtccWarehouse;
    }

    //need to persist DTCCWarehouse info & market participant portfolio later
    public void ClearTransactions(ArrayList<Transaction> transactions, DTCCWarehouse dtccWarehouse) {
        ClearingEngine clearingEngine = new ClearingEngine(dtccWarehouse);

        //update DTCCWarehouse certificates & MarketParticipantPortfolio (securities & cash)
        clearingEngine.ClearTrade(transactions);
    }

}
