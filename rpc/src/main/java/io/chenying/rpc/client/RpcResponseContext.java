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
package io.chenying.rpc.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.JsonNode;

import io.chenying.rpc.RpcResponse;

public class RpcResponseContext extends RpcResponse<Object> {

    private final static long serialVersionUID = -5014302768240598034L;

    @JsonProperty(value = "body", access = Access.WRITE_ONLY)
    private JsonNode bodyNode;

    public JsonNode getBodyNode() {
        return this.bodyNode;
    }

    public void setBodyNode(JsonNode bodyNode) {
        this.bodyNode = bodyNode;
    }

}
