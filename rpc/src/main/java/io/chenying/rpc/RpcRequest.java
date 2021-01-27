/**
 * Copyright 2020 Chenying Wang
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
package io.chenying.rpc;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

public class RpcRequest<T> implements Serializable {

    private final static long serialVersionUID = -924909642294436355L;

    private long id;
    private String method;
    private T parameters;

    public RpcRequest() {
        this(StringUtils.EMPTY, null);
    }

    public RpcRequest(String method, T parameters) {
        this.id = 0;
        this.method = method;
        this.parameters = parameters;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMethod() {
        return this.method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public T getParameters() {
        return parameters;
    }

    public void setParameters(T parameters) {
        this.parameters = parameters;
    }

}
