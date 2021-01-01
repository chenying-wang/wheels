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

import java.util.List;

import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.support.GenericApplicationContext;

public class RpcClientContextInitializer {

    private RpcClient client;

    public RpcClientContextInitializer(RpcClient client) {
        this.client = client;
    }

    public void initialize(GenericApplicationContext applicationContext, List<Class<?>> rpcServices) {
        for (Class<?> rpcService: rpcServices) {
            RootBeanDefinition bd = new RootBeanDefinition(rpcService);
            bd.setInstanceSupplier(() -> this.createProxy(rpcService, this.client));
            String beanName = AnnotationBeanNameGenerator.INSTANCE.generateBeanName(bd, applicationContext);
            applicationContext.registerBeanDefinition(beanName, bd);
        }
    }

    private Object createProxy(Class<?> rpcService, RpcClient client) {
        ProxyFactoryBean factoryBean = new ProxyFactoryBean();
        factoryBean.addInterface(rpcService);
        factoryBean.addAdvice(new RpcInvocation(rpcService, client));
        return factoryBean.getObject();
    }

}
