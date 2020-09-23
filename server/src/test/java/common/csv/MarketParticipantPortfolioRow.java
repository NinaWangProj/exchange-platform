package common.csv;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;

import java.util.Date;

public class MarketParticipantPortfolioRow {
    @CsvBindByName
    public int userID;
    @CsvBindByName
    public double cash;
    @CsvBindByName
    public double cash_available_to_trade;
    @CsvBindByName
    public String tickerSymbol;
    @CsvBindByName
    public String shareHolderName;
    @CsvBindByName
    public int quantity;
    @CsvBindByName
    public int quantity_available_to_trade;
    @CsvBindByName
    @CsvDate("yyyy-MM-dd HH:mm:ss")
    public Date issuedDate;
}
