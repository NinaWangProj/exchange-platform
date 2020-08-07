package nw.ExchangePlatform.clearing;

import nw.ExchangePlatform.clearing.data.DTCCWarehouse;
import nw.ExchangePlatform.clearing.data.MarketParticipantPortfolio;
import nw.ExchangePlatform.clearing.data.SecurityCertificate;
import nw.ExchangePlatform.trading.data.Transaction;

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
        //userID, HashMap<tickerSymbol, SecurityCertificate>
        HashMap<Integer, MarketParticipantPortfolio> portfoliosMap = dtccWarehouse.portfoliosMap;
        MarketParticipantPortfolio portfolio = portfoliosMap.get(transaction.getUserID());
        switch(transaction.getDirection()) {
            case BUY:
                boolean foundTicker = portfoliosMap.get(transaction.getUserID()).getSecurities().containsKey(transaction.getTickerSymbol());
                if(foundTicker) {
                    portfolio.getSecurities().get(transaction.getTickerSymbol()).quantity += transaction.getSize();
                } else {
                    SecurityCertificate certificate = new SecurityCertificate(transaction.getName(), transaction.getTickerSymbol(), transaction.getSize(), new Date());
                    portfolio.getSecurities().put(transaction.getTickerSymbol(), certificate);
                }
                portfolio.DepositCash(-transaction.getPrice());
            case SELL:
                portfolio.getSecurities().get(transaction.getTickerSymbol()).quantity -= transaction.getSize();
                portfolio.DepositCash(transaction.getPrice());
        }
    }

    private void ClearTransactionWithDTCCWarehouse(final Transaction transaction, HashMap<String, HashMap<Integer,SecurityCertificate>> certificatesMap){
        switch (transaction.getDirection()) {
            case BUY:
                boolean foundTicker = certificatesMap.containsKey(transaction.getTickerSymbol());
                boolean foundUser = foundTicker && certificatesMap.get(transaction.getTickerSymbol()).containsKey(transaction.getUserID());
                if(foundTicker) {
                    if(!foundUser) {
                        SecurityCertificate certificate = new SecurityCertificate(transaction.getName(), transaction.getTickerSymbol(), 0, new Date());
                        certificatesMap.get(transaction.getTickerSymbol()).put(transaction.getUserID(),certificate);
                    }
                    certificatesMap.get(transaction.getTickerSymbol()).get(transaction.getUserID()).quantity += transaction.getSize();
                } else {
                    final SecurityCertificate certificate = new SecurityCertificate(transaction.getName(), transaction.getTickerSymbol(), transaction.getSize(), new Date());
                    certificatesMap.put(transaction.getTickerSymbol(), new HashMap<Integer,SecurityCertificate>(){{put(transaction.getUserID(),certificate);}});
                }

            case SELL:
                //locate previous certificate for the seller
                SecurityCertificate certificate =  certificatesMap.get(transaction.getTickerSymbol()).get(transaction.getUserID());
                //modify certificate to reflect the new transaction
                certificate.quantity -= transaction.getSize();
        }
    }

}
