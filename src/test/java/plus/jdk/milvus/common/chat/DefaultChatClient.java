package plus.jdk.milvus.common.chat;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.serializer.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.fastjson.JSON.toJSONString;
import static com.azure.core.http.HttpHeaderName.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
@RequiredArgsConstructor
public class DefaultChatClient implements ChatClient {

    private final HttpClient httpClient;

    @Override
    public List<List<Float>> getEmbedding(List<String> texts) {
        Map<String, List<String>> request = new HashMap<>();
        request.put("sentences", texts);
        String DEFAULT_EMBEDDING_URL = "http://192.168.1.193:8084/m3e_encode";
        HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, DEFAULT_EMBEDDING_URL);
        httpRequest.setBody(toJSONString(request))
                .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE);
        HttpResponse block = httpClient.send(httpRequest).retry(5).block(Duration.ofSeconds(20));
        if (block == null) return null;
        List<List<Float>> embeddings = block.getBodyAsBinaryData().toObject(new TypeReference<List<List<Float>>>() {
        });
        block.close();
        if (embeddings == null) return new ArrayList<>();
        return embeddings;
    }
}
