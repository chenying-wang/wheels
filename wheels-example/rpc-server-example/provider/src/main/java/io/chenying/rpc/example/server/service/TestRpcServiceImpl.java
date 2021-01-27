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
package io.chenying.rpc.example.server.service;

import io.chenying.rpc.annotation.RpcServiceReference;

@RpcServiceReference
public class TestRpcServiceImpl implements TestRpcService {

    @Override
    public TestData<String> someMethod(TestData<String> input) {
        TestData<String> body = new TestData<>();
        body.setValue(233);
        body.setText("Hello");
        body.setMsg("Client");
        return body;
    }

    @Override
    public int add(Integer x, int y) {
        return x + y;
    }

}
