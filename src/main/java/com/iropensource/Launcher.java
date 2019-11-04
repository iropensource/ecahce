package com.iropensource;

import com.iropensource.cahce.EhCacheSingleton;
import com.iropensource.model.Product;
import com.iropensource.simulation.Processor;
import org.ehcache.Cache;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Launcher {
    public static List<Product> products4Sales = new ArrayList<>();

    // تعداد مشتری ها
    public static final int CUSTOMERS = 100;
    //مدت زمان پیشنهاد ویژه به ثانیه
    public static final int DURATION_IN_SEC = 40;

    static {
        // لیست آیتم ها با شماره کالا، اسم، قیمت واقعی و تعداد موجود
        products4Sales.add(new Product(1L, "Shampoo", 10000.0, 5));
        products4Sales.add(new Product(2L, "Soap", 2000.0, 10));
        products4Sales.add(new Product(3L, "Toothpaste", 20000.0, 8));
        products4Sales.add(new Product(4L, "Perfume", 50000.0, 14));
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        // لیست تخفیف را برای هر آیتم در نظر بگیر
        Map<Long, Double> discountList = new HashMap<Long, Double>() {{
            put(1L, 10.0);
            put(2L, 20.0);
            put(3L, 30.0);
            put(4L, 40.0);
        }};

        //به مدت ۲ دقیقه پیشنهاد ویژه
        EhCacheSingleton ehCacheSingleton = EhCacheSingleton.newOffer(discountList, Duration.ofSeconds(DURATION_IN_SEC));
        Cache<Long, Double> cache = ehCacheSingleton.getCache();


        Random random = new Random();

        // به تعداد مشتری هام برام ترد بساز - شبیه سازی
        ExecutorService executor = Executors.newFixedThreadPool(CUSTOMERS);
        List<Future<?>> list = new ArrayList<>();
        for (int i = 0; i < CUSTOMERS; i++) {
            Future<?> future = executor.submit(() -> {
                Processor proc = new Processor(cache, products4Sales);
                //  از آیتم با شماره ی ۰ تا ۴ رو برام رندوم بگیر و بفروش
                proc.sell(Long.valueOf(random.nextInt(products4Sales.size()+1)));
            });

            list.add(future);
        }

        for (Future<?> fut : list) {
            Object s = fut.get();
        }

        executor.shutdown();

    }
}
