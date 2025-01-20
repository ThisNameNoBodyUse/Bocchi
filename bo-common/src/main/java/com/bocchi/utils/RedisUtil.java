package com.bocchi.utils;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RBucket;
import org.redisson.api.RSet;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {
    private final RedissonClient redissonClient = Redisson.create();

    // ========================== 分布式锁 ==========================

    /**
     * 获取可重入锁
     * @param lockKey 锁的 key
     * @return RLock
     */
    public RLock getLock(String lockKey) {
        return redissonClient.getLock(lockKey);
    }

    /**
     * 尝试获取锁（支持重试，可重入，看门狗）
     * @param lockKey 锁的 key
     * @param waitTime 获取锁的最大等待时间
     * @param leaseTime 持有锁的时间 (如果为-1，则无限续约，一般不设置-1）
     * @param unit 时间单位
     * @return 是否成功获取锁
     */
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 释放锁（确保释放正确）
     * @param lockKey 锁的 key
     */
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {  // 确保只有持有锁的线程才能释放
            lock.unlock();
        }
    }

    // ========================== 缓存操作 ==========================

    /**
     * 缓存数据
     * 缓存String类型的，也就是所有的对象列表、普通键值对等
     * @param key 键
     * @param data 数据
     * @param duration 过期时间
     * @param unit 时间单位
     */
    public <T> void cacheData(String key, T data, long duration, TimeUnit unit) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        bucket.set(data, duration, unit);
    }

    /**
     * 获取缓存数据
     * @param key 键
     * @param type 数据类型 比如 Dish.class
     * @return 缓存的数据
     */
    public <T> T getCachedData(String key, Class<T> type) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    /**
     * 删除缓存数据
     * @param key 键
     */
    public void deleteCachedData(String key) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        bucket.delete();
    }

    /**
     * 将元素加入Set集合中（比如key为博主，元素为点赞的userId）
     * @param key
     * @param element
     * @param <T>
     */
    public <T> void addSetElement(String key,T element) {
        RSet<T> set = redissonClient.getSet(key);
        set.add(element);
    }

    /**
     * 从Set集合中移除元素（取消点赞）
     * @param key
     * @param element
     * @param <T>
     */
    public <T> void removeSetElement(String key,T element) {
        RSet<T> set = redissonClient.getSet(key);
        set.remove(element);
    }

    /**
     * 求两个集合的交集
     * @param key1 第一个集合的key
     * @param key2 第二个集合的key
     * @return
     * @param <T>
     */
    public <T> Set<T> getSetIntersection(String key1,String key2) {
        RSet<T> set1 = redissonClient.getSet(key1);
        return set1.readIntersection(key2);
    }

    /**
     * 清空整个Set集合
     * @param key
     */
    public void deleteSet(String key) {
        RSet<Object> set = redissonClient.getSet(key);
        set.delete();
    }

    // ========================== ZSet排行榜操作 ==========================

    /**
     * 为元素加分
     * @param key ZSet的key
     * @param element 元素
     * @param score 加的分值
     */
    public void addScore(String key, String element, double score) {
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        scoredSortedSet.addScore(element, score);
    }

    /**
     * 从ZSet中移出元素
     * @param key ZSet的key
     * @param element 元素
     */
    public void removeElement(String key, String element) {
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        scoredSortedSet.remove(element);
    }

    /**
     * 获取排行榜前N名
     * @param key ZSet的key
     * @param n 前N名
     * @return 前N名的元素集合
     */
    public Set<String> getTopN(String key, int n) {
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        return new HashSet<>(scoredSortedSet.valueRangeReversed(0, n - 1));
    }

}
