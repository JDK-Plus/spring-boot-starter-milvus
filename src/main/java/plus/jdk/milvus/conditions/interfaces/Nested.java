/*
 * Copyright (c) 2011-2023, baomidou (jobob@qq.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package plus.jdk.milvus.conditions.interfaces;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * 查询条件封装
 * <p>嵌套</p>
 * <ul>
 *     <li>泛型 Param 是具体需要运行函数的类(也是 wrapper 的子类)</li>
 * </ul>
 */
public interface Nested<Param, Children> extends Serializable {

    /**
     * AND 嵌套
     * <p>
     * 例: and(i -&gt; i.eq("name", "李白").ne("status", "活着"))
     * </p>
     *
     * @param consumer 消费函数
     * @return children
     */
    default Children and(Consumer<Param> consumer) {
        return and(true, consumer);
    }

    /**
     * AND 嵌套
     * <p>
     * 例: and(i -&gt; i.eq("name", "李白").ne("status", "活着"))
     * </p>
     *
     * @param condition 执行条件
     * @param consumer  消费函数
     * @return children
     */
    Children and(boolean condition, Consumer<Param> consumer);

    /**
     * OR 嵌套
     * <p>
     * 例: or(i -&gt; i.eq("name", "李白").ne("status", "活着"))
     * </p>
     *
     * @param consumer 消费函数
     * @return children
     */
    default Children or(Consumer<Param> consumer) {
        return or(true, consumer);
    }

    /**
     * OR 嵌套
     * <p>
     * 例: or(i -&gt; i.eq("name", "李白").ne("status", "活着"))
     * </p>
     *
     * @param condition 执行条件
     * @param consumer  消费函数
     * @return children
     */
    Children or(boolean condition, Consumer<Param> consumer);

    /**
     * not嵌套
     * <p>
     * 例: not(i -&gt; i.eq("name", "李白").ne("status", "活着"))
     * </p>
     *
     * @param consumer 消费函数
     * @return children
     */
    default Children not(Consumer<Param> consumer) {
        return not(true, consumer);
    }

    /**
     * not嵌套
     * <p>
     * 例: not(i -&gt; i.eq("name", "李白").ne("status", "活着"))
     * </p>
     *
     * @param condition 执行条件
     * @param consumer  消费函数
     * @return children
     */
    Children not(boolean condition, Consumer<Param> consumer);
}