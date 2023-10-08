package plus.jdk.milvus.common.chat;

import java.util.List;

public interface ChatClient {
    List<List<Float>> getEmbedding(List<String> texts);
}
