package com.liemily.hibernateexample.stock;

import com.liemily.hibernateexample.dataaccess.config.DataAccessConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Created by Emily Li on 23/07/2017.
 */
@SpringBootApplication
@Import(DataAccessConfig.class)
public class StockApplication {
    public static void main(String[] args) {
        SpringApplication.run(StockApplication.class, args);
    }
}
