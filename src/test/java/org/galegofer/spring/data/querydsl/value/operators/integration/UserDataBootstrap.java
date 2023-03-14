/*******************************************************************************
 * Copyright (c) 2018 @gt_tech
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
 *******************************************************************************/
package org.galegofer.spring.data.querydsl.value.operators.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.galegofer.spring.data.querydsl.value.operators.integration.dao.UserRepository;
import org.galegofer.spring.data.querydsl.value.operators.integration.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Bootstraps the test data into embedded database for integration-tests.
 *
 * @author gt_tech
 */
@Component
public class UserDataBootstrap implements ResourceLoaderAware {

    private ResourceLoader resourceLoader;

    private final String pattern = "classpath:users/*.json";

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    UserRepository repository;

    @PostConstruct
    public void bootstrap() throws Exception {
        Resource[] usersResources = loadResources(pattern);
        if (usersResources != null) {
            Arrays.stream(usersResources)
                    .forEach(us -> {
                        try {
                            try (InputStream is = us.getInputStream()) {
                                repository.save(mapper.readValue(is, User.class));
                            }
                        } catch (Throwable t) {
                            throw new RuntimeException("Failed to bootstrap application integration test user data");
                        }
                    });
        }
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    Resource[] loadResources(String pattern) throws IOException {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
                .getResources(pattern);
    }
}
