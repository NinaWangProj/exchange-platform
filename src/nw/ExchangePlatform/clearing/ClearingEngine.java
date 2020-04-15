package nw.ExchangePlatform.clearing;

import nw.ExchangePlatform.data.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ClearingEngine {
    //field
    ArrayList<Transaction> transactions;
    DTCCWarehouse dtccWarehouse;

    //constructors
    public ClearingEngine(ArrayList<Transaction> transactions,DTCCWarehouse dtccWarehouse) {
        this.transactions = transactions;
        this.dtccWarehouse = dtccWarehouse;
    }

    //public methods
    public ClearingStatus ClearTrade(){
        HashMap<String, HashMap<String,SecurityCertificate>> certificatesMap = dtccWarehouse.certificatesMap;

        for(Transaction transaction : transactions){
            ClearTransactionWithDTCCWarehouse(transaction, certificatesMap);
            UpdateMarketParticipantPortfolio(transaction);
        }

        return ClearingStatus.CLEARED;
    }

    private void UpdateMarketParticipantPortfolio(Transaction transaction) {
        //userID, HashMap<tickerSymbol, SecurityCertificate>
        HashMap<String, MarketParticipantPortfolio> portfoliosMap = dtccWarehouse.portfoliosMap;
        MarketParticipantPortfolio portfolio = portfoliosMap.get(transaction.userID);
        switch(transaction.direction) {
            case BUY:
                boolean foundTicker = portfoliosMap.get(transaction.userID).securities.containsKey(transaction.tickerSymbol);
                if(foundTicker) {
                    portfolio.securities.get(transaction.tickerSymbol).quantity += transaction.size;
                } else {
                    SecurityCertificate certificate = new SecurityCertificate(transaction.name, transaction.tickerSymbol, transaction.size, new Date());
                    portfolio.securities.put(transaction.tickerSymbol, certificate);
                }
                portfolio.cash -= transaction.tradePrice;
            case SELL:
                portfolio.securities.get(transaction.tickerSymbol).quantity -= transaction.size;
                portfolio.cash += transaction.tradePrice;
        }
    }

    private void ClearTransactionWithDTCCWarehouse(Transaction transaction,HashMap<String, HashMap<String,SecurityCertificate>> certificatesMap){
        switch (transaction.direction) {
            case BUY:
                boolean foundTicker = certificatesMap.containsKey(transaction.tickerSymbol);
                boolean foundUser = foundTicker && certificatesMap.get(transaction.tickerSymbol).containsKey(transaction.userID);
                if(foundTicker) {
                    if(!foundUser) {
                        SecurityCertificate certificate = new SecurityCertificate(transaction.name, transaction.tickerSymbol, 0, new Date());
                        certificatesMap.get(transaction.tickerSymbol).put(transaction.userID,certificate);
                    }
                    certificatesMap.get(transaction.tickerSymbol).get(transaction.userID).quantity += transaction.size;
                } else {
                    SecurityCertificate certificate = new SecurityCertificate(transaction.name, transaction.tickerSymbol, transaction.size, new Date());
                    certificatesMap.put(transaction.tickerSymbol, new HashMap<>(){{put(transaction.userID,certificate);}});
                }

            case SELL:
                //locate previous certificate for the seller
                SecurityCertificate certificate =  certificatesMap.get(transaction.tickerSymbol).get(transaction.userID);
                //modify certificate to reflect the new transaction
                certificate.quantity -= transaction.size;
        }
    }

}
