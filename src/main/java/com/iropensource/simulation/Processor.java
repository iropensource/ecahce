package com.iropensource.simulation;

import com.iropensource.model.Product;
import org.ehcache.Cache;

import java.util.List;
import java.util.Optional;

public class Processor {

    private Cache<Long, Double> cache;
    private List<Product> products;

    public Processor(Cache<Long, Double> cache, List<Product> products) {
        this.cache = cache;
        this.products = products;
    }

    public void sell(Long productId) {

        synchronized (products) {
            Optional<Product> op = products.stream().filter(product -> product.getId() == productId).findFirst();
            if (!op.isPresent())
                return;

            Product product = op.get();
            String productName = product.getName();
            System.out.println(String.format("مشتری %s :", Thread.currentThread().getName()));

            if (product.getAvailability() < 1) {
                System.out.format("متاسفانه دیگه محصول %s بطور کامل به فروش رسیده\n\n", productName);
                return;
            }


            Double discount = cache.get(productId);
            Double actualPrice = product.getPrice();
            int remaining = product.getAvailability();

            if (discount != null) {
                Double discountedPrice = computeDiscountedPrice(actualPrice, discount);
                System.out.format("شما موفق به خریداری آیتم %s در پیشنهاد ویژه ی ما شدین. قیمت فروش : %s -- قیمت واقعی جنس : %s -- تعداد باقی مانده %d\n\n", productName, String.valueOf(discountedPrice), String.valueOf(actualPrice), remaining);

            } else {
                System.out.format("متاسفانه پیشنهاد ویژه ی فروش آیتم %s به پایان رسیده و شما آن را با قیمت واقعی %s خریداری کردید -- تعداد باقی مانده %d\n\n", productName, String.valueOf(actualPrice), remaining);
            }

            product.setAvailability(product.getAvailability() - 1);

            // ۲ ثانیه بعد از هر فروش صبر کن
            try {
                Thread.currentThread().sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private Double computeDiscountedPrice(Double price, Double discountAmount) {
        price = price - (price * (discountAmount / 100));
        return price;
    }
}
