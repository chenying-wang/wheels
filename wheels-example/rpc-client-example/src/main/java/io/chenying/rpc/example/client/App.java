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
package io.chenying.rpc.example.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import io.chenying.rpc.example.server.service.TestData;
import io.chenying.rpc.example.server.service.TestRpcService;
import io.chenying.rpc.utils.JsonUtils;

public class App {

    private final static Logger logger = LoggerFactory.getLogger(App.class);

    private JsonUtils json = JsonUtils.instance();

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        AnnotationConfigApplicationContext appCtx = new AnnotationConfigApplicationContext();
        appCtx.register(AppConfig.class);
        appCtx.registerShutdownHook();
        appCtx.refresh();

        TestData<String> request = new TestData<>();
        request.setValue(233);
        request.setText("Hello");
        request.setMsg("Server");
        TestData<String> result = this.getService(appCtx).someMethod(request);
        logger.info(this.json.write(result));

        int x = 210, y = 23;
        int res = this.getService(appCtx).add(x, y);
        logger.info("{} + {} = {}", x, y, res);

        try {
            Thread.currentThread().join();
        } catch (Exception e) {
            logger.error("Current Thread Interrupted", e);
        }
    }

    private TestRpcService getService(ApplicationContext appCtx) {
        return appCtx.getBean(TestRpcService.class);
    }

}
