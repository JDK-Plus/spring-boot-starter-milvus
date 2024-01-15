package plus.jdk.milvus.annotation;

import org.springframework.context.annotation.Import;
import plus.jdk.milvus.selector.MilvusSelector;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Import(MilvusSelector.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableMilvusPlus {

}
