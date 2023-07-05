
### 一、如何引入

```xml
<dependency>
    <groupId>plus.jdk</groupId>
    <artifactId>spring-boot-starter-milvus</artifactId>
    <version>1.0.3</version>
</dependency>
```

### 二、milvus的引用配置

```bash
plus.jdk.milvus.enabled=true
plus.jdk.milvus.user-name=admin
plus.jdk.milvus.password=123456
plus.jdk.milvus.endpoints=http://ip1:2379,http://ip2:2379,http://ip3:2379
```

> java 使用milvus不建议使用证书,因为`SslContext Builder`只支持PEM格式的[PKCS#8](https://github.com/milvus-io/jmilvus/blob/main/docs/SslConfig.md)私钥文件。

### 三、在项目中引用

下面给出一个使用示例。

```java
import lombok.extern.slf4j.Slf4j;
import plus.jdk.milvus.annotation.milvusNode;
import plus.jdk.milvus.global.milvusClient;
import plus.jdk.scheduled.annotation.Scheduled;
import plus.jdk.scheduled.global.IScheduled;

import javax.annotation.Resource;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Scheduled(expr = "0/1 * * * * *")
public class LoadAverageScheduledTask extends IScheduled {


    @milvusNode(path = "/example/system/aim/loadAverage")
    private volatile double maxLoadAverage = 2.0;

    @milvusNode(path = "/example/system/aim/random/length")
    private volatile int maxRandomLen = 500000;

    @Resource
    private milvusClient milvusClient;

    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 200,
            0, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(200));

    @Override
    protected void doInCronJob() {
        String data = milvusClient.getFirstKV("/example", String.class);
    }
}

```