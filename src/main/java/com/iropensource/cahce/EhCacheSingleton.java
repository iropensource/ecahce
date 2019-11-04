package com.iropensource.cahce;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import java.time.Duration;
import java.util.Map;

public class EhCacheSingleton {

    //یه آبجکت مدیریت کننده کش برام بساز
    private static CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
    private static EhCacheSingleton instance;

    private Cache<Long, Double> cache;

    /**
     * بحالت سینگلتن برام این کش رو بساز و بهش زمان اکتیو بودن اختصاص بده
     * @param discounts به ازای آی دی هر جنس چند درصد تخفیف باید در نظر گرفته شود
     * @param duration زمانی که تخفیف را میشود استفاده کرد
     */
    private EhCacheSingleton(final Map<Long, Double> discounts, final Duration duration) {
        cache = cacheManager.createCache("productOfferCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, Double.class,
                        ResourcePoolsBuilder.heap(100))
                        .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(duration))
                        .withEvictionAdvisor((aLong, aDouble) -> true) // وقتی زمان تخفیف به پایان رسید از تو کش بندازشون بیرون
                        .build());

        discounts.forEach((productId, discountPercentage) -> cache.put(productId, discountPercentage));
    }

    /**
     * یه پیشنهاد ویژه ی جدید بهم بده
     * @param discounts
     * @param duration
     * @return
     */
    public static EhCacheSingleton newOffer(final Map<Long, Double> discounts, final Duration duration) {
        if (instance == null) {
            synchronized (EhCacheSingleton.class) {
                if (instance == null) {
                    instance = new EhCacheSingleton(discounts, duration);
                }
            }
        }

        return instance;
    }

    /**
     * حتی اگر پیشنهاد ویژه ای موجود هستش بازم یه اینستنس جدید با cache عه جدید برام درست کن
     * @param discounts
     * @param duration
     * @return
     */
    public static EhCacheSingleton forceRefresh(final Map<Long, Double> discounts, final Duration duration) {

        synchronized (EhCacheSingleton.class) {
            instance = new EhCacheSingleton(discounts, duration);
        }

        return instance;
    }

    public Cache<Long, Double> getCache() {
        return cache;
    }
}
