package nw.ExchangePlatform.clearing;


import nw.ExchangePlatform.data.DTCCWarehouse;
import nw.ExchangePlatform.data.MarketParticipantPortfolio;
import nw.ExchangePlatform.data.SecurityCertificate;
import nw.ExchangePlatform.data.Transaction;

import java.util.ArrayList;
import java.util.HashMap;

public class ClearingEngine {
    //field
    ArrayList<Transaction> transactions;
    DTCCWarehouse dtccWarehouse;

    //constructors
    public ClearingEngine(ArrayList<Transaction> Transactions,DTCCWarehouse dtccWarehouse) {
        this.transactions = Transactions;
        this.dtccWarehouse = dtccWarehouse;
    }

    //public methods
    public ClearingStatus ClearTrade(){
        for(Transaction transaction : transactions){
            switch (transaction.direction) {
                case BUY:

                case SELL:
                    //locate previous certificate for the seller
                    SecurityCertificate certificate =  dtccWarehouse.certificatesMap.get(transaction.tickerSymbol).get(transaction.userID);
                    //modify certificate to reflect the new transaction
                    certificate.quantity -= transaction.size;
            }
        }


        return ClearingStatus.CLEARED;
    }


}
