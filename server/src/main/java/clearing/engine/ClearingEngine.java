package clearing.engine;

import clearing.data.ClearingStatus;
import clearing.data.DTCCWarehouse;
import commonData.clearing.MarketParticipantPortfolio;
import commonData.clearing.SecurityCertificate;
import common.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ClearingEngine {
    //field
    DTCCWarehouse dtccWarehouse;

    //constructors
    public ClearingEngine(DTCCWarehouse dtccWarehouse) {
        this.dtccWarehouse = dtccWarehouse;
    }

    //public methods
    public ClearingStatus ClearTrade(ArrayList<Transaction> transactions){
        HashMap<String, HashMap<Integer, SecurityCertificate>> certificatesMap = dtccWarehouse.certificatesMap;

        for(Transaction transaction : transactions){
            ClearTransactionWithDTCCWarehouse(transaction, certificatesMap);
            UpdateMarketParticipantPortfolio(transaction);
        }

        return ClearingStatus.CLEARED;
    }

    private void UpdateMarketParticipantPortfolio(Transaction transaction) {
        HashMap<Integer, MarketParticipantPortfolio> portfoliosMap = dtccWarehouse.portfoliosMap;
        MarketParticipantPortfolio portfolio = portfoliosMap.get(transaction.getUserID());

        int transactionQuantity = 0;
        switch (transaction.getDirection()) {
            case BUY:
                transactionQuantity = transaction.getSize();
                break;
            case SELL:
                transactionQuantity = -transaction.getSize();
                break;
        }

        boolean foundTicker = portfoliosMap.get(transaction.getUserID()).getSecurities().containsKey(transaction.getTickerSymbol());
        if(foundTicker) {
            portfolio.getSecurities().get(transaction.getTickerSymbol()).quantity += transactionQuantity;
        } else {
            SecurityCertificate certificate = new SecurityCertificate(transaction.getName(), transaction.getTickerSymbol(), transactionQuantity, new Date());
            portfolio.getSecurities().put(transaction.getTickerSymbol(), certificate);
        }
        portfolio.DepositCash(-transaction.getPrice() * transactionQuantity);
    }

    private void ClearTransactionWithDTCCWarehouse(final Transaction transaction, HashMap<String, HashMap<Integer,SecurityCertificate>> certificatesMap){
        int transactionQuantity = 0;
        switch (transaction.getDirection()) {
            case BUY:
                transactionQuantity = transaction.getSize();
                break;
            case SELL:
                transactionQuantity = -transaction.getSize();
                break;
        }

        boolean foundTicker = certificatesMap.containsKey(transaction.getTickerSymbol());
        boolean foundUser = foundTicker && certificatesMap.get(transaction.getTickerSymbol()).containsKey(transaction.getUserID());
        if(foundTicker) {
            if(!foundUser) {
                SecurityCertificate certificate = new SecurityCertificate(transaction.getName(), transaction.getTickerSymbol(), 0, new Date());
                certificatesMap.get(transaction.getTickerSymbol()).put(transaction.getUserID(),certificate);
            }
            certificatesMap.get(transaction.getTickerSymbol()).get(transaction.getUserID()).quantity += transactionQuantity;
        } else {
            final SecurityCertificate certificate = new SecurityCertificate(transaction.getName(), transaction.getTickerSymbol(), transactionQuantity, new Date());
            certificatesMap.put(transaction.getTickerSymbol(), new HashMap<Integer,SecurityCertificate>(){{put(transaction.getUserID(),certificate);}});
        }
    }
}
