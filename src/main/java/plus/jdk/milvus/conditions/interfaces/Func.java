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
import java.util.Collection;
import java.util.function.Consumer;

/**
 * 查询条件封装
 */
public interface Func<Children, R> extends Serializable {

    /**
     * 字段 IN (value.get(0), value.get(1), ...)
     * <p>例: in("id", Arrays.asList(1, 2, 3, 4, 5))</p>
     *
     * <li> 注意！当集合为 空或null 时, Expr会拼接为：WHERE (字段名 IN ()), 执行时报错</li>
     * <li> 若要在特定条件下不拼接, 可在 condition 条件中判断 </li>
     *
     * @param column 字段
     * @param coll   数据集合
     * @return children
     */
    default Children in(R column, Collection<?> coll) {
        return in(true, column, coll);
    }

    /**
     * 字段 IN (value.get(0), value.get(1), ...)
     * <p>例: in(true, "id", Arrays.asList(1, 2, 3, 4, 5))</p>
     *
     * <li> 注意！当集合为 空或null 时, Expr会拼接为：WHERE (字段名 IN ()), 执行时报错</li>
     * <li> 若要在特定条件下不拼接, 可在 condition 条件中判断 </li>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param coll      数据集合
     * @return children
     */
    Children in(boolean condition, R column, Collection<?> coll);

    /**
     * 字段 IN (v0, v1, ...)
     * <p>例: in("id", 1, 2, 3, 4, 5)</p>
     *
     * <li> 注意！当数组为 空或null 时, Expr会拼接为：WHERE (字段名 IN ()), 执行时报错</li>
     * <li> 若要在特定条件下不拼接, 可在 condition 条件中判断 </li>
     *
     * @param column 字段
     * @param values 数据数组
     * @return children
     */
    default Children in(R column, Object... values) {
        return in(true, column, values);
    }

    /**
     * 字段 IN (v0, v1, ...)
     * <p>例: in(true, "id", 1, 2, 3, 4, 5)</p>
     *
     * <li> 注意！当数组为 空或null 时, Expr会拼接为：WHERE (字段名 IN ()), 执行时报错</li>
     * <li> 若要在特定条件下不拼接, 可在 condition 条件中判断 </li>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param values    数据数组
     * @return children
     */
    Children in(boolean condition, R column, Object... values);

    /**
     * 字段 NOT IN (value.get(0), value.get(1), ...)
     * <p>例: notIn("id", Arrays.asList(1, 2, 3, 4, 5))</p>
     *
     * <li> 注意！当集合为 空或null 时, Expr会拼接为：WHERE (字段名 NOT IN ()), 执行时报错</li>
     * <li> 若要在特定条件下不拼接, 可在 condition 条件中判断 </li>
     *
     * @param column 字段
     * @param coll   数据集合
     * @return children
     */
    default Children notIn(R column, Collection<?> coll) {
        return notIn(true, column, coll);
    }

    /**
     * 字段 NOT IN (value.get(0), value.get(1), ...)
     * <p>例: notIn(true, "id", Arrays.asList(1, 2, 3, 4, 5))</p>
     *
     * <li> 注意！当集合为 空或null 时, Expr会拼接为：WHERE (字段名 NOT IN ()), 执行时报错</li>
     * <li> 若要在特定条件下不拼接, 可在 condition 条件中判断 </li>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param coll      数据集合
     * @return children
     */
    Children notIn(boolean condition, R column, Collection<?> coll);

    /**
     * 字段 NOT IN (v0, v1, ...)
     * <p>例: notIn("id", 1, 2, 3, 4, 5)</p>
     *
     * <li> 注意！当数组为 空或null 时, Expr会拼接为：WHERE (字段名 NOT IN ()), 执行时报错</li>
     * <li> 若要在特定条件下不拼接, 可在 condition 条件中判断 </li>
     *
     * @param column 字段
     * @param values 数据数组
     * @return children
     */
    default Children notIn(R column, Object... values) {
        return notIn(true, column, values);
    }

    /**
     * 字段 NOT IN (v0, v1, ...)
     * <p>例: notIn(true, "id", 1, 2, 3, 4, 5)</p>
     *
     * <li> 注意！当数组为 空或null 时, Expr会拼接为：WHERE (字段名 NOT IN ()), 执行时报错</li>
     * <li> 若要在特定条件下不拼接, 可在 condition 条件中判断 </li>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param values    数据数组
     * @return children
     */
    Children notIn(boolean condition, R column, Object... values);

    /**
     * 消费函数
     *
     * @param consumer 消费函数
     * @return children
     */
    default Children func(Consumer<Children> consumer) {
        return func(true, consumer);
    }

    /**
     * 消费函数
     *
     * @param condition 执行条件
     * @param consumer  消费函数
     * @return children
     * @since 3.3.1
     */
    Children func(boolean condition, Consumer<Children> consumer);


    /**
     * 字段 json_contains("identifier", value)
     *
     * @param column     字段
     * @param identifier json字段
     * @param value      值
     * @return children
     */
    default Children jsonContains(R column, Object value, Object... identifier) {
        return jsonContains(true, column, value, identifier);
    }

    /**
     * 字段 json_contains("identifier", value)
     *
     * @param condition  执行条件
     * @param column     字段
     * @param identifier json字段
     * @param value      值
     * @return children
     */
    Children jsonContains(boolean condition, R column, Object value, Object... identifier);


    /**
     * 字段 json_contains_all("identifier", [values])
     *
     * @param column     字段
     * @param identifier json字段
     * @param coll       数据集合
     * @return children
     */
    default Children jsonContainsAll(R column, Collection<?> coll, Object... identifier) {
        return jsonContainsAll(true, column, coll, identifier);
    }

    /**
     * 字段 json_contains_all("identifier", [values])
     *
     * @param condition  执行条件
     * @param column     字段
     * @param identifier json字段
     * @param coll       数据集合
     * @return children
     */
    Children jsonContainsAll(boolean condition, R column, Collection<?> coll, Object... identifier);


    /**
     * 字段 json_contains_any("identifier", [values])
     *
     * @param column     字段
     * @param identifier json字段
     * @param coll       数据集合
     * @return children
     */
    default Children jsonContainsAny(R column, Collection<?> coll, Object... identifier) {
        return jsonContainsAny(true, column, coll, identifier);
    }

    /**
     * 字段 json_contains_any("identifier", [values])
     *
     * @param condition  执行条件
     * @param column     字段
     * @param identifier json字段
     * @param coll       数据集合
     * @return children
     */
    Children jsonContainsAny(boolean condition, R column, Collection<?> coll, Object... identifier);


    /**
     * 字段 array_contains("column", value)
     *
     * @param column 字段
     * @param value  值
     * @return children
     */
    default Children arrayContains(R column, Object value) {
        return arrayContains(true, column, value);
    }

    /**
     * 字段 array_contains("column", value)
     *
     * @param condition 执行条件
     * @param column    字段
     * @param value     值
     * @return children
     */
    Children arrayContains(boolean condition, R column, Object value);


    /**
     * 字段 array_contains_all("column", [values])
     *
     * @param column 字段
     * @param coll   数据集合
     * @return children
     */
    default Children arrayContainsAll(R column, Collection<?> coll) {
        return arrayContainsAll(true, column, coll);
    }

    /**
     * 字段 array_contains_all("column", [values])
     *
     * @param condition 执行条件
     * @param column    字段
     * @param coll      数据集合
     * @return children
     */
    Children arrayContainsAll(boolean condition, R column, Collection<?> coll);


    /**
     * 字段 array_contains_all("column", [values])
     *
     * @param column 字段
     * @param values 值
     * @return children
     */
    default Children arrayContainsAll(R column, Object... values) {
        return arrayContainsAll(true, column, values);
    }

    /**
     * 字段 array_contains_all("column", [values])
     *
     * @param condition 执行条件
     * @param column    字段
     * @param values    值
     * @return children
     */
    Children arrayContainsAll(boolean condition, R column, Object... values);


    /**
     * 字段 array_contains_any("column", [values])
     *
     * @param column 字段
     * @param coll   数据集合
     * @return children
     */
    default Children arrayContainsAny(R column, Collection<?> coll) {
        return arrayContainsAny(true, column, coll);
    }

    /**
     * 字段 array_contains_any("column", [values])
     *
     * @param condition 执行条件
     * @param column    字段
     * @param coll      数据集合
     * @return children
     */
    Children arrayContainsAny(boolean condition, R column, Collection<?> coll);


    /**
     * 字段 array_contains_any("column", [values])
     *
     * @param column 字段
     * @param values 值
     * @return children
     */
    default Children arrayContainsAny(R column, Object... values) {
        return arrayContainsAny(true, column, values);
    }

    /**
     * 字段 array_contains_any("column", [values])
     *
     * @param condition 执行条件
     * @param column    字段
     * @param values    值
     * @return children
     */
    Children arrayContainsAny(boolean condition, R column, Object... values);


    /**
     * 字段 array_length(column) == value
     *
     * @param column 字段
     * @return children
     */
    default Children arrayLength(R column, Number value) {
        return arrayContainsAny(true, column);
    }

    /**
     * 字段 array_length(column) == value
     *
     * @param condition 执行条件
     * @param column    字段
     * @return children
     */
    Children arrayLength(boolean condition, R column, Number value);
}
