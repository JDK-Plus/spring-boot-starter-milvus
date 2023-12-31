package plus.jdk.milvus.annotation;

import org.springframework.context.annotation.Import;
import plus.jdk.milvus.selector.MilvusSelector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Import(MilvusSelector.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableMilvusPlus {

}
