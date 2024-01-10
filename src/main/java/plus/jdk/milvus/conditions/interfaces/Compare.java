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

/**
 * 查询条件封装
 * <p>比较值</p>
 */
public interface Compare<Children, R> extends Serializable {

    /**
     * 等于 =
     *
     * @param column 字段
     * @param val    值
     * @return children
     */
    default Children eq(R column, Object val) {
        return eq(true, column, val);
    }

    /**
     * 等于 =
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return children
     */
    Children eq(boolean condition, R column, Object val);

    /**
     * 不等于 !=
     *
     * @param column 字段
     * @param val    值
     * @return children
     */
    default Children ne(R column, Object val) {
        return ne(true, column, val);
    }

    /**
     * 不等于 !=
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return children
     */
    Children ne(boolean condition, R column, Object val);

    /**
     * 大于 &gt;
     *
     * @param column 字段
     * @param val    值
     * @return children
     */
    default Children gt(R column, Object val) {
        return gt(true, column, val);
    }

    /**
     * 大于 &gt;
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return children
     */
    Children gt(boolean condition, R column, Object val);

    /**
     * 大于等于 &gt;=
     *
     * @param column 字段
     * @param val    值
     * @return children
     */
    default Children ge(R column, Object val) {
        return ge(true, column, val);
    }

    /**
     * 大于等于 &gt;=
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return children
     */
    Children ge(boolean condition, R column, Object val);

    /**
     * 小于 &lt;
     *
     * @param column 字段
     * @param val    值
     * @return children
     */
    default Children lt(R column, Object val) {
        return lt(true, column, val);
    }

    /**
     * 小于 &lt;
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return children
     */
    Children lt(boolean condition, R column, Object val);

    /**
     * 小于等于 &lt;=
     *
     * @param column 字段
     * @param val    值
     * @return children
     */
    default Children le(R column, Object val) {
        return le(true, column, val);
    }

    /**
     * 小于等于 &lt;=
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return children
     */
    Children le(boolean condition, R column, Object val);

    /**
     * NOT LIKE '值%'
     *
     * @param column 字段
     * @param val    值
     * @return children
     */
    default Children notLikeRight(R column, Object val) {
        return notLikeRight(true, column, val);
    }

    /**
     * NOT LIKE '值%'
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return children
     */
    Children notLikeRight(boolean condition, R column, Object val);

    /**
     * LIKE '值%'
     *
     * @param column 字段
     * @param val    值
     * @return children
     */
    default Children likeRight(R column, Object val) {
        return likeRight(true, column, val);
    }

    /**
     * LIKE '值%'
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return children
     */
    Children likeRight(boolean condition, R column, Object val);
}
