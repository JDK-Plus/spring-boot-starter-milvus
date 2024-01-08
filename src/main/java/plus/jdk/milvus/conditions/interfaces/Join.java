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
 * <p>拼接</p>
 */
public interface Join<Children> extends Serializable {

    /**
     * 拼接 OR
     *
     * @return children
     */
    default Children or() {
        return or(true);
    }

    /**
     * 拼接 OR
     *
     * @param condition 执行条件
     * @return children
     */
    Children or(boolean condition);

    /**
     * 拼接 Expr
     * <p>例1: apply("id = 1")</p>
     *
     * @param values 数据数组
     * @return children
     */
    default Children apply(String applyExpr, Object... values) {
        return apply(true, applyExpr, values);
    }

    /**
     * 拼接 Expr
     * <p>例1: apply("id = 1")</p>
     *
     * @param condition 执行条件
     * @param values    数据数组
     * @return children
     */
    Children apply(boolean condition, String applyExpr, Object... values);
}
