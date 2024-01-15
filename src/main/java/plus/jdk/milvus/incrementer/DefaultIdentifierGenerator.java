package plus.jdk.milvus.incrementer;

import plus.jdk.milvus.toolkit.Snowflake;
import plus.jdk.milvus.toolkit.SystemClock;

/**
 * 默认生成器
 */
public class DefaultIdentifierGenerator implements IdentifierGenerator {

    private final Snowflake snowflake;

    public DefaultIdentifierGenerator(long workerId) {
        this.snowflake = new Snowflake(workerId % 1024);
    }

    public DefaultIdentifierGenerator(long workerId, long dataCenterId) {
        this.snowflake = new Snowflake((workerId / 2 + dataCenterId / 2) % 1024);
    }

    public DefaultIdentifierGenerator(Snowflake snowflake) {
        this.snowflake = snowflake;
    }

    public static DefaultIdentifierGenerator getInstance() {
        return DefaultInstance.INSTANCE;
    }

    @Override
    public Long nextId(Object entity) {
        return snowflake.nextId();
    }

    private static class DefaultInstance {

        public static final DefaultIdentifierGenerator INSTANCE = new DefaultIdentifierGenerator(SystemClock.now());

    }

}
