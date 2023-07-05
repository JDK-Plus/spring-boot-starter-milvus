package plus.jdk.milvus.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
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
     * 主机名
     */
    private String host;

    /**
     * 端口
     */
    private Integer port;
}
