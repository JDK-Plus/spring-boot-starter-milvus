package plus.jdk.milvus.config;

import io.milvus.param.ConnectParam;
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

    /**
     * 链接的uri
     */
    private String connectUri;

    /**
     * 链接超时时间
     */
    private Long connectTimeout;

    /**
     * Enables the secure for client channel.
     * enable – true keep-alive
     */
    private Boolean secure = false;

    /**
     * Sets the idle timeout value of client channel. The timeout value must be larger than zero.
     */
    private Long idleTimeout;

    /**
     * Sets the database name.
     */
    private String database;

    /**
     * Sets the token
     */
    private String token;

    /**
     * Set a deadline for how long you are willing to wait for a reply from the server.
     * With a deadline setting, the client will wait when encounter fast RPC fail caused by network fluctuations.
     * The deadline value must be larger than or equal to zero. Default value is 0, deadline is disabled.
     */
    private Long rpcDeadline;

    /**
     * Sets the keep-alive time value of client channel.
     * The keep-alive value must be greater than zero.
     */
    private Long keepAliveTime;
}
