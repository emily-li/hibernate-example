package com.liemily.hibernateexample.stock;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;

/**
 * Created by Emily Li on 23/07/2017.
 */
@Entity
public class Stock {
    @Id
    private String symbol;
    private BigDecimal value;
    private int volume;

    // Empty constructor required for JPA
    protected Stock() {
    }

    public Stock(String symbol, BigDecimal value, int volume) {
        this.symbol = symbol;
        this.value = value;
        this.volume = volume;
    }

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getValue() {
        return value;
    }

    public int getVolume() {
        return volume;
    }

    @Override
    public String toString() {
        return "Stock{" +
                "symbol='" + symbol + '\'' +
                ", value=" + value +
                ", volume=" + volume +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Stock stock = (Stock) o;

        if (volume != stock.volume) return false;
        if (symbol != null ? !symbol.equals(stock.symbol) : stock.symbol != null) return false;
        return value != null ? value.doubleValue() == (stock.value.doubleValue()) : stock.value == null;
    }

    @Override
    public int hashCode() {
        int result = symbol != null ? symbol.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + volume;
        return result;
    }
}
