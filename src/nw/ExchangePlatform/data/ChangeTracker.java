package nw.ExchangePlatform.data;

import javafx.util.Pair;

import java.util.ArrayList;

public class ChangeTracker {

    private ArrayList<Pair<BookOperation, Object>> bookChanges;

    public ChangeTracker() {
        bookChanges = new ArrayList<Pair<BookOperation, Object>>();
    }

    public void SaveChanges(BookOperation operation,Object inputArg) {
        bookChanges.add(new Pair<>(operation,inputArg));
    }

    public ArrayList<Pair<BookOperation, Object>> getBookChanges() {
        return bookChanges;
    }
}
