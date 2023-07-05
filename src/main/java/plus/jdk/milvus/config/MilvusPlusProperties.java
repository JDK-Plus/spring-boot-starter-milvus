package plus.jdk.milvus.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * <a href="https://github.com/milvus-io/jmilvus/blob/main/docs/SslConfig.md">...</a>
 */
@Data
@ConfigurationProperties(prefix = "plus.jdk.milvus")
public class MilvusPlusProperties {

    /**
     * 是否启动
     */
    private Boolean enabled = false;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

    /**
     * milvus节点列表
     */
    private String[] endpoints;

    /**
     * watcher核心线程数
     */
    private int watcherCoreThreadPollSize = 10;
}
