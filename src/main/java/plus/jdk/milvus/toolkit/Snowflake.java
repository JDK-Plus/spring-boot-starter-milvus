package plus.jdk.milvus.toolkit;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
public class Snowflake {
    private static final Random RANDOM = new Random();
    private final long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public Snowflake(long workerId) {
        if (workerId <= 1023L && workerId >= 0L) {
            this.workerId = workerId;
        } else {
            String message = String.format("worker Id can't be greater than %d or less than 0", 1023L);
            throw new IllegalArgumentException(message);
        }
    }

    public static Snowflake create(long workerId) {
        return new Snowflake(workerId);
    }

    public long[] nextId(int size) {
        if (size > 0 && size <= 100000) {
            long[] ids = new long[size];

            for (int i = 0; i < size; ++i) {
                ids[i] = this.nextId();
            }

            return ids;
        } else {
            String message = String.format("Size can't be greater than %d or less than 0", 100000);
            throw new IllegalArgumentException(message);
        }
    }

    public synchronized long nextId() {
        long timestamp = this.timeGen();
        if (this.lastTimestamp == timestamp) {
            this.sequence = this.sequence + 1L & 4095L;
            if (this.sequence == 0L) {
                this.sequence = RANDOM.nextInt(100);
                timestamp = this.tilNextMillis(this.lastTimestamp);
            }
        } else {
            this.sequence = RANDOM.nextInt(100);
        }

        if (timestamp < this.lastTimestamp) {
            String message = String.format("Clock moved backwards. Refusing to generate id for %d milliseconds.", this.lastTimestamp - timestamp);
            log.error(message);
            throw new RuntimeException(message);
        } else {
            this.lastTimestamp = timestamp;
            return timestamp - 1483200000000L << 22 | this.workerId << 12 | this.sequence;
        }
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp;
        timestamp = this.timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = this.timeGen();
        }

        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }
}