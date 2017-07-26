package com.liemily.hibernateexample.stock;

import com.liemily.hibernateexample.dataaccess.HibernateDAL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Emily Li on 23/07/2017.
 */
@Component
public class StockService {
    private HibernateDAL hibernateDAL;

    @Autowired
    public StockService(HibernateDAL hibernateDAL) {
        this.hibernateDAL = hibernateDAL;
    }

    public boolean save(Stock stock) {
        hibernateDAL.save(stock);
        return true;
    }

    public void save(Set<Stock> stocks) {
        stocks.forEach(stock -> save(stock));
    }

    public Stock findOne(String stockSymbol) throws InterruptedException {
        List<Stock> stock = hibernateDAL.query("FROM Stock WHERE symbol='" + stockSymbol + "'");
        return stock.size() == 1 ? stock.get(0) : null;
    }

    public List<Stock> findAll() throws InterruptedException {
        List<Stock> stock = hibernateDAL.query("FROM Stock");
        return stock == null ? new ArrayList<>() : stock;
    }

    public void delete(String stockSymbol) {
        hibernateDAL.delete("com.liemily.hibernateexample.stock.Stock", stockSymbol);
    }

    public void delete(Set<String> stockSymbols) {
        final String DELIM = ", ";
        StringBuilder query = new StringBuilder("DELETE FROM Stock WHERE symbol IN (");
        stockSymbols.forEach(symbol -> query.append("'").append(symbol).append("'").append(DELIM));
        query.setLength(query.length() - DELIM.length());
        query.append(")");

        hibernateDAL.execute(query.toString());
    }
}
