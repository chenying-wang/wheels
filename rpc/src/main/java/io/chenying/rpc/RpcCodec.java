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

import com.fasterxml.jackson.core.type.TypeReference;

import io.chenying.rpc.utils.JsonUtils;

public class RpcCodec {

    private JsonUtils json = JsonUtils.instance();

    public <T> String encode(T msg) {
        return this.json.write(msg);
    }

    public <T> T decode(String msg, TypeReference<T> typeRef) {
        return this.json.read(msg, typeRef);
    }

}
