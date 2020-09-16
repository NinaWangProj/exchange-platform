package common.csv;

import clearing.data.DTCCWarehouse;
import clearing.engine.ClearingEngine;
import commonData.clearing.MarketParticipantPortfolio;
import commonData.clearing.SecurityCertificate;

import java.util.HashMap;
import java.util.List;

public class DtccFromCsv {

    public static DTCCWarehouse Get(String initialPortfoliosFileName) {
        List<MarketParticipantPortfolioRow> portfolioRows = CsvHelper.GetRowsFromCSV(initialPortfoliosFileName, MarketParticipantPortfolioRow.class);
        DTCCWarehouse dtccWarehouse = BuildDtcc(portfolioRows);

        return dtccWarehouse;
    }


    private static DTCCWarehouse BuildDtcc(List<MarketParticipantPortfolioRow> portfolioRows) {

        HashMap<String, HashMap<Integer, SecurityCertificate>> certificatesMap = new HashMap<String, HashMap<Integer, SecurityCertificate>>();
        HashMap<Integer, MarketParticipantPortfolio> portfoliosMap = new HashMap<Integer, MarketParticipantPortfolio>();

        for (MarketParticipantPortfolioRow row :portfolioRows){
            if(!portfoliosMap.containsKey(row.userID))
                portfoliosMap.put(row.userID,
                        new MarketParticipantPortfolio(new HashMap<String, SecurityCertificate>(), row.cash));

            MarketParticipantPortfolio port = portfoliosMap.get(row.userID);
            port.getSecurities().put(row.tickerSymbol,
                    new SecurityCertificate(row.shareHolderName, row.shareHolderName, row.quantity, row.issuedDate));

            if(!certificatesMap.containsKey(row.tickerSymbol))
                certificatesMap.put(row.tickerSymbol, new HashMap<Integer, SecurityCertificate>());

            certificatesMap.get(row.tickerSymbol).put(row.userID,
                    new SecurityCertificate(row.shareHolderName, row.shareHolderName, row.quantity, row.issuedDate));
        }

        DTCCWarehouse warehouse = new DTCCWarehouse();
        warehouse.certificatesMap = certificatesMap;
        warehouse.portfoliosMap = portfoliosMap;
        return warehouse;
    }
}
