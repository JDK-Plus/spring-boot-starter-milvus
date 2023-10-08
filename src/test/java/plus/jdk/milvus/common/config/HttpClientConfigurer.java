package plus.jdk.milvus.common.config;

import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientProvider;
import com.azure.core.util.HttpClientOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.time.Duration.ZERO;
import static java.time.Duration.ofSeconds;

@Configuration
public class HttpClientConfigurer {

    @Bean
    public HttpClient httpClient() {
        return HttpClient.createDefault(new HttpClientOptions()
                .setConnectTimeout(ofSeconds(5))
                .setConnectionIdleTimeout(ZERO)
                .setMaximumConnectionPoolSize(128)
                .setHttpClientProvider(NettyAsyncHttpClientProvider.class)
        );
    }
}
