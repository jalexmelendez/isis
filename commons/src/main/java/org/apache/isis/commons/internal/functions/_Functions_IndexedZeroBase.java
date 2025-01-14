/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.commons.internal.functions;

import java.util.function.Function;

import org.apache.isis.commons.internal.functions._Functions.IndexedFunction;

/**
 * Package private mixin for _Functions. <br/>
 * Extending a Function to keep track of an index, incremented with each function call.
 */
class _Functions_IndexedZeroBase<T, R> implements Function<T, R> {


    private int index=0;
    private final IndexedFunction<T, R> indexAwareFunction;

    _Functions_IndexedZeroBase(IndexedFunction<T, R> indexAwareFunction) {
        this.indexAwareFunction = indexAwareFunction;
    }

    @Override
    public R apply(T t) {
        return indexAwareFunction.apply(index++, t);
    }

}
