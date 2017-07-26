package com.liemily.hibernateexample.stock;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

/**
 * Created by Emily Li on 23/07/2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class StockServiceTest {
    private static final Logger logger = LogManager.getLogger(StockServiceTest.class);
    private final int AVG_RUN_COUNT = 1000;
    private final int BATCH_COUNT = 100;

    @Autowired
    private StockService stockService;
    private String stockSymbol;

    @Before
    public void setup() {
        stockSymbol = "SYM" + UUID.randomUUID();
    }

    @After
    public void tearDown() throws Exception {
        try {
            stockService.delete(stockSymbol);
        } catch (EntityNotFoundException e) {
            logger.info("Stock " + stockSymbol + " was not present in the database");
        }
    }

    @Test
    public void testWriteStock() throws Exception {
        Stock stock = new Stock(stockSymbol, new BigDecimal(1.5), 1);
        stockService.save(stock);
        Stock foundStock = stockService.findOne(stockSymbol);
        Assert.assertEquals(stock, foundStock);
    }

    @Test
    public void testDeleteStock() throws Exception {
        Stock stock = new Stock(stockSymbol, new BigDecimal(1.5), 1);
        stockService.save(stock);
        Stock foundStock = stockService.findOne(stockSymbol);
        Assert.assertEquals(stock, foundStock);

        stockService.delete(stockSymbol);
        Stock deletedStock = stockService.findOne(stockSymbol);
        Assert.assertNull(deletedStock);
    }

    @Test
    public void testWriteSingleStockAvgTime() throws Exception {
        Set<Stock> stocks = generateStocks(AVG_RUN_COUNT);
        Set<String> stockSymbols = new HashSet<>();
        stocks.forEach(stock -> stockSymbols.add(stock.getSymbol()));
        try {
            long totalTimeMs = timeWriteStocks(stocks, 1);
            logger.info("Average run time for writing a single stock was " + (totalTimeMs / AVG_RUN_COUNT) + "ms");
            logger.info("Total time taken to write " + AVG_RUN_COUNT + " stocks was " + totalTimeMs);
        } finally {
            stockService.delete(stockSymbols);
        }
    }

    @Test
    public void testWriteMultipleStocksAvgTime() throws Exception {
        Set<Stock> stocks = generateStocks(AVG_RUN_COUNT);
        Set<String> stockSymbols = new HashSet<>();
        stocks.forEach(stock -> stockSymbols.add(stock.getSymbol()));
        try {
            long totalTimeMs = timeWriteStocks(stocks, BATCH_COUNT);
            logger.info("Average run time for writing a stock batch of " + BATCH_COUNT + " was " + (totalTimeMs / BATCH_COUNT) + "ms");
            logger.info("Total time taken to write " + AVG_RUN_COUNT + " stocks was " + totalTimeMs);
        } finally {
            stockService.delete(stockSymbols);
        }
    }

    @Test
    public void testFindStockAvgTime() throws Exception {
        Stock stock = new Stock(stockSymbol, new BigDecimal(1.5), 1);
        stockService.save(stock);

        long totalTimeMs = 0;

        for (int i = 0; i < AVG_RUN_COUNT; i++) {
            totalTimeMs += timeFindStock(stockSymbol);
        }

        logger.info("Average run time to find a stock was " + (totalTimeMs / AVG_RUN_COUNT) + "ms");
        logger.info("Total time taken to find a stock with " + AVG_RUN_COUNT + " individual requests was " + totalTimeMs);
    }

    @Test
    public void testParallelFindStockAvgTime() throws Exception {
        Stock stock = new Stock(stockSymbol, new BigDecimal(1.5), 1);
        stockService.save(stock);

        ExecutorService executorService = Executors.newFixedThreadPool(AVG_RUN_COUNT);
        Collection<FindOneTask> findTasks = new ArrayList<>();
        IntStream.range(0, AVG_RUN_COUNT).forEach(i -> findTasks.add(new FindOneTask(stockSymbol)));

        List<Future<Long>> times = executorService.invokeAll(findTasks);

        long totalTimeMs = 0;
        for (Future<Long> time : times) {
            totalTimeMs += time.get();
        }

        logger.info("Average run time to find a stock was " + (totalTimeMs / AVG_RUN_COUNT) + "ms");
        logger.info("Total time taken to find a stock with " + AVG_RUN_COUNT + " individual requests was " + totalTimeMs);
    }

    @Test
    public void testFindAllStocksAvgTime() throws Exception {
        Set<Stock> stocks = generateStocks(AVG_RUN_COUNT);
        Set<String> stockSymbols = new HashSet<>();
        stocks.forEach(stock -> stockSymbols.add(stock.getSymbol()));

        try {
            stockService.save(stocks);

            long totalTimeMs = 0;
            for (int i = 0; i < AVG_RUN_COUNT; i++) {
                totalTimeMs += timeFindStocks();
            }

            logger.info("Average run time to find all stocks was " + (totalTimeMs / AVG_RUN_COUNT) + "ms");
            logger.info("Total time taken to find all stocks with " + AVG_RUN_COUNT + " individual requests was " + totalTimeMs);
        } finally {
            stockService.delete(stockSymbols);
        }
    }

    @Test
    public void testParallelFindAllStocksAvgTime() throws Exception {
        Set<Stock> stocks = generateStocks(AVG_RUN_COUNT);
        Set<String> stockSymbols = new HashSet<>();
        stocks.forEach(stock -> stockSymbols.add(stock.getSymbol()));
        try {
            stockService.save(stocks);

            ExecutorService executorService = Executors.newFixedThreadPool(AVG_RUN_COUNT);
            Collection<FindAllTask> findTasks = new ArrayList<>();
            IntStream.range(0, AVG_RUN_COUNT).forEach(i -> findTasks.add(new FindAllTask()));

            List<Future<Long>> times = executorService.invokeAll(findTasks);

            long totalTimeMs = 0;
            for (Future<Long> time : times) {
                totalTimeMs += time.get();
            }

            logger.info("Average run time to find all stocks was " + (totalTimeMs / AVG_RUN_COUNT) + "ms");
            logger.info("Total time taken to find all stocks with " + AVG_RUN_COUNT + " individual requests was " + totalTimeMs);
        } finally {
            stockService.delete(stockSymbols);
        }
    }

    Set<Stock> generateStocks(int numStocks) {
        Set<Stock> stocks = new HashSet<>();
        String id = UUID.randomUUID().toString();
        IntStream.range(0, numStocks).forEach(i -> stocks.add(new Stock(id + i, new BigDecimal(1.5), 1)));
        return stocks;
    }

    long timeWriteStocks(Set<Stock> stocks, int batchCount) throws Exception {
        long totalTimeMs = 0;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < stocks.size(); i += batchCount) {
            stockService.save(new HashSet<>(new ArrayList<>(stocks).subList(i, i + batchCount)));
            long endTime = System.currentTimeMillis();
            totalTimeMs += endTime - startTime;
            startTime = endTime;
        }
        return totalTimeMs;
    }

    long timeFindStock(String stockSymbol) throws Exception {
        long startTimeMs = System.currentTimeMillis();
        stockService.findOne(stockSymbol);
        long endTimeMs = System.currentTimeMillis();
        return endTimeMs - startTimeMs;
    }

    long timeFindStocks() throws Exception {
        long startTimeMs = System.currentTimeMillis();
        stockService.findAll();
        long endTimeMs = System.currentTimeMillis();
        return endTimeMs - startTimeMs;
    }

    private class FindOneTask implements Callable<Long> {
        private String id;

        public FindOneTask(String id) {
            this.id = id;
        }

        @Override
        public Long call() throws Exception {
            return timeFindStock(id);
        }
    }

    private class FindAllTask implements Callable<Long> {
        @Override
        public Long call() throws Exception {
            return timeFindStocks();
        }
    }
}
