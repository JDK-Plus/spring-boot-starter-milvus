package plus.jdk.milvus.factory;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import lombok.Setter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.Banner;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiStyle;
import org.springframework.core.env.Environment;
import plus.jdk.milvus.autoconfigure.MilvusPlusProperties;
import plus.jdk.milvus.autoconfigure.MilvusPlusVersion;
import plus.jdk.milvus.config.GlobalConfig;

import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

@Setter
public class MilvusPlusFactoryBean implements FactoryBean<MilvusServiceClient>, InitializingBean {

    private GlobalConfig globalConfig;
    private MilvusServiceClient milvusServiceClient;
    private MilvusPlusProperties properties;


    @Override
    public MilvusServiceClient getObject() {
        if (this.milvusServiceClient == null) {
            afterPropertiesSet();
        }
        return this.milvusServiceClient;
    }

    @Override
    public Class<?> getObjectType() {
        return null;
    }

    @Override
    public void afterPropertiesSet() {
        this.milvusServiceClient = buildMilvusServiceClient();
    }

    private MilvusServiceClient buildMilvusServiceClient() {
        if (properties == null) {
            return null;
        }
        ConnectParam.Builder builder = ConnectParam.newBuilder();
        if (properties.getHost() != null) {
            builder.withHost(properties.getHost());
        }
        if (properties.getPort() != null) {
            builder.withPort(properties.getPort());
        }
        if (properties.getUserName() != null) {
            builder.withAuthorization(properties.getUserName(), properties.getPassword());
        }
        if (properties.getConnectUri() != null) {
            builder.withUri(properties.getConnectUri());
        }
        if (properties.getConnectTimeout() != null) {
            builder.withConnectTimeout(properties.getConnectTimeout(), TimeUnit.MILLISECONDS);
        }
        if (properties.getRpcDeadline() != null) {
            builder.withRpcDeadline(properties.getRpcDeadline(), TimeUnit.MILLISECONDS);
        }
        if (properties.getDatabase() != null) {
            builder.withDatabaseName(properties.getDatabase());
        }
        if (properties.getSecure() != null) {
            builder.withSecure(properties.getSecure());
        }
        if (properties.getKeepAliveTime() != null) {
            builder.withKeepAliveTime(properties.getKeepAliveTime(), TimeUnit.MILLISECONDS);
        }
        if (properties.getIdleTimeout() != null) {
            builder.withIdleTimeout(properties.getIdleTimeout(), TimeUnit.MILLISECONDS);
        }
        if (properties.getToken() != null) {
            builder.withToken(properties.getToken());
        }

        if (globalConfig.isBanner()) {
            new MilvusPlusBanner().printBanner(null, null, System.out);
        }

        return new MilvusServiceClient(builder.build());
    }

    static class MilvusPlusBanner implements Banner {

        private static final int STRAP_LINE_SIZE = 66;
        private final String[] bannerLines = {
                "    __  ___ _  __                            ____   __            ",
                "   /  |/  /(_)/ /_   __ __  __ _____        / __ \\ / /__  __ _____",
                "  / /|_/ // // /| | / // / / // ___/______ / /_/ // // / / // ___/",
                " / /  / // // / | |/ // /_/ /(__  )/_____// ____// // /_/ /(__  ) ",
                "/_/  /_//_//_/  |___/ \\__,_//____/       /_/    /_/ \\__,_//____/"
        };
        private final String MILVUS_PLUS = " :: Milvis-Plus :: ";

        @Override
        public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
            for (String line : bannerLines) {
                out.println(line);
            }
            String version = MilvusPlusVersion.getVersion();
            version = (version != null) ? " (v" + version + ")" : "";
            StringBuilder padding = new StringBuilder();
            while (padding.length() < STRAP_LINE_SIZE - (version.length() + MILVUS_PLUS.length())) {
                padding.append(" ");
            }
            out.println(AnsiOutput.toString(AnsiColor.BLUE, MILVUS_PLUS, AnsiColor.DEFAULT, padding.toString(),
                    AnsiStyle.FAINT, version));
            out.println();
        }
    }
}
