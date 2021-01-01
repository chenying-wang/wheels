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

public class RpcResponse<T> implements Serializable {

    private final static long serialVersionUID = 1580369783307587082L;

    private long id;
    private int code;
    private String message;
    private T body;

    public RpcResponse() {
        this(0, StringUtils.EMPTY, null);
    }

    public RpcResponse(int code, String message, T body) {
        this.id = 0;
        this.code = code;
        this.message = message;
        this.body = body;
    }

    public static <T> RpcResponse<T> newSuccessResponse(T body) {
        return new RpcResponse<>(0, StringUtils.EMPTY, body);
    }

    public static <T> RpcResponse<T> newFailedResponse(int code, String message) {
        return new RpcResponse<>(code, message, null);
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getBody() {
        return this.body;
    }

    public void setBody(T body) {
        this.body = body;
    }

}
