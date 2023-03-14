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
package org.galegofer.spring.data.querydsl.value.operators.integration.tests;


import org.galegofer.spring.data.querydsl.value.operators.integration.TestSpringApplication;
import org.galegofer.spring.data.querydsl.value.operators.integration.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * Integration test starts the Test ApplicationContext and performs search-specific verification by invoking APIs
 *
 * @author gt_tech
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {TestSpringApplication.class})
@TestPropertySource(locations = "classpath:test.yml")
public class UsersSearchIT {

    @Autowired
    TestRestTemplate template;

    @Test
    public void testUserSearchWithUserNames_ImplicitORClause() {
        ResponseEntity<List<User>> response = template.exchange("/users/search?userName=dgayle&userName=ssmith",
                                                                HttpMethod.GET, null,
                                                                new ParameterizedTypeReference<List<User>>() {
                                                                });

        assertEquals(2, response.getBody()
                                .size());
        assertThat(response.getBody(), containsInAnyOrder(
                hasProperty("userName", is("dgayle")),
                hasProperty("userName", is("ssmith"))));
    }

    @Test
    public void testUserSearchWithUserNames_ImplicitORClause_WithDefaultCaseSensitivity() {
        ResponseEntity<List<User>> response = template.exchange("/users/search?userName=dgayle&userName=SsmiTh",
                                                                HttpMethod.GET, null,
                                                                new ParameterizedTypeReference<List<User>>() {
                                                                });

        assertEquals(1, response.getBody()
                                .size());
        assertThat(response.getBody(), contains(
                hasProperty("userName", is("dgayle"))));
    }

    @Test
    public void testUserSearchWithUserNames_ImplicitORClause_WithExplicitCaseInSensitivity() {
        ResponseEntity<List<User>> response = template.exchange("/users/search?userName=dgayle&userName=ci(eq" +
                                                                        "(SsmiTh))",
                                                                HttpMethod.GET, null,
                                                                new ParameterizedTypeReference<List<User>>() {
                                                                });

        assertEquals(2, response.getBody()
                                .size());
        assertThat(response.getBody(), containsInAnyOrder(
                hasProperty("userName", is("dgayle")),
                hasProperty("userName", is("ssmith"))));
    }


    @Test
    public void testUserSearchWithEmailsEndsWith_ImplicitORClause() {
        ResponseEntity<List<User>> response = template.exchange("/users/search?emails.address=endsWith(@company.com)" +
                                                                        "&emails.address=endsWith(@dummy.com)",
                                                                HttpMethod.GET, null,
                                                                new ParameterizedTypeReference<List<User>>() {
                                                                });

        assertEquals(4, response.getBody()
                                .size());
        assertThat(response.getBody(), containsInAnyOrder(
                hasProperty("userName", is("bsummers")),
                hasProperty("userName", is("dgayle")),
                hasProperty("userName", is("ksmith")),
                hasProperty("userName", is("ssmith"))));
    }

    @Test
    public void testUserSearchWithEmailsKebabCaseStartsWith() {
        ResponseEntity<List<User>> response = template.exchange("/users/search?emails.address=starts-with(ssmith)",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<User>>() {
                });

        assertEquals(1, response.getBody()
                                .size());
        assertThat(response.getBody(), contains(
                hasProperty("userName", is("ssmith"))));
    }

    /*
     * Test for Long value in NumberPath
     */
    @Test
    public void testUserSearchWithEmployeeId() {
        ResponseEntity<List<User>> response = template.exchange("/users/search?employeeId=9223372036854775801",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<User>>() {
                });

        assertEquals(1, response.getBody()
                                .size());
        assertThat(response.getBody(), contains(
                hasProperty("userName", is("ssmith"))));
    }


    @Test
    public void testUserSearchWithEmailsStartsWith() {
        ResponseEntity<List<User>> response = template.exchange("/users/search?emails.address=startsWith(dgayle@co)",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<User>>() {
                });

        assertEquals(1, response.getBody()
                                .size());
        assertThat(response.getBody(), contains(
                hasProperty("userName", is("dgayle"))));
    }


    @Test
    public void testUserSearchWithEmailsKebabCaseEndsWith_ImplicitORClause() {
        ResponseEntity<List<User>> response = template.exchange("/users/search?emails.address=ends-with(@company.com)" +
                        "&emails.address=ends-with(@dummy.com)",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<User>>() {
                });

        assertEquals(4, response.getBody()
                                .size());
        assertThat(response.getBody(), containsInAnyOrder(
                hasProperty("userName", is("bsummers")),
                hasProperty("userName", is("dgayle")),
                hasProperty("userName", is("ksmith")),
                hasProperty("userName", is("ssmith"))));
    }

    @Test
    public void testUserSearchWithEmailsEndsWith_ImplicitORClause_WithNegatedCriteria() {
        ResponseEntity<List<User>> response = template.exchange("/users/search?emails.address=endsWith(@company.com)" +
                                                                        "&emails.address=and(not(endsWith(@dummy.com)" +
                                                                        "))",
                                                                HttpMethod.GET, null,
                                                                new ParameterizedTypeReference<List<User>>() {
                                                                });

        assertEquals(3, response.getBody()
                                .size());
        assertThat(response.getBody(), containsInAnyOrder(
                hasProperty("userName", is("bsummers")),
                hasProperty("userName", is("ksmith")),
                hasProperty("userName", is("ssmith"))));
    }

    @Test
    public void testUserSearchWithEmailsEndsWith_ImplicitANDClause() {
        ResponseEntity<List<User>> response = template.exchange("/users/search?emails.address=endsWith(@company.com)" +
                                                                        "&emails.address=and(endsWith(@dummy.com))",
                                                                HttpMethod.GET, null,
                                                                new ParameterizedTypeReference<List<User>>() {
                                                                });

        assertEquals(1, response.getBody()
                                .size());
        assertThat(response.getBody(), contains(
                hasProperty("userName", is("dgayle"))));
    }

    @Test
    public void testUserSearchWithEmailsEndsWith_ExplicitAndClause() {
        ResponseEntity<List<User>> response = template.exchange("/users/search?emails.address=endsWith(@company.com)" +
                                                                        "&emails.address=and(endsWith(@dummy.com))",
                                                                HttpMethod.GET, null,
                                                                new ParameterizedTypeReference<List<User>>() {
                                                                });

        assertEquals(1, response.getBody()
                                .size());
        assertThat(response.getBody(), contains(
                hasProperty("userName", is("dgayle"))));
    }

    @Test
    public void testUserSearchWithUserStatus_And_DepartmentClause() {
        ResponseEntity<List<User>> response = template.exchange("/users/search?status=matches(^.*LOC.*$)" +
                                                                        "&department=SALES",
                                                                HttpMethod.GET, null,
                                                                new ParameterizedTypeReference<List<User>>() {
                                                                });

        assertEquals(1, response.getBody()
                                .size());
        assertThat(response.getBody(), contains(
                hasProperty("userName", is("ksmith"))));
    }


    @Test
    public void testUserSearchWithAge_GreaterThanEqualClause() {
        ResponseEntity<List<User>> response = template.exchange("/users/search?profile.age=gte(35)",
                                                                HttpMethod.GET, null,
                                                                new ParameterizedTypeReference<List<User>>() {
                                                                });

        assertEquals(3, response.getBody()
                                .size());
        assertThat(response.getBody(), containsInAnyOrder(
                hasProperty("userName", is("bsummers")),
                hasProperty("userName", is("dgayle")),
                hasProperty("userName", is("ssmith"))));
    }

    @Test
    public void testUserSearchWithAge_LessThanClause() {
        ResponseEntity<List<User>> response = template.exchange("/users/search?profile.age=lt(35)",
                                                                HttpMethod.GET, null,
                                                                new ParameterizedTypeReference<List<User>>() {
                                                                });

        assertEquals(1, response.getBody()
                                .size());
        assertThat(response.getBody(), contains(
                hasProperty("userName", is("ksmith"))));
    }

    @Test
    public void testUserSearchWithDateOfBirth_EqualsNeedsToBeExactClause() {
        ResponseEntity<List<User>> response = template.exchange("/users/search?profile.dateOfBirth=04/29/1980 00:00:00 UTC",
                                                                HttpMethod.GET, null,
                                                                new ParameterizedTypeReference<List<User>>() {
                                                                });

        assertEquals(1, response.getBody()
                                .size());
        assertThat(response.getBody(), contains(
            hasProperty("userName", is("ssmith"))));
    }


    @Test
    public void testUserSearchWithDateOfBirth_EqualsNeedsToBeExactAlternativeNotationClause() {
        ResponseEntity<List<User>> response = template.exchange("/users/search?profile.dateOfBirth=Tue, April 29 1980 00:00:00 GMT",
                                                                HttpMethod.GET, null,
                                                                new ParameterizedTypeReference<List<User>>() {
                                                                });

        assertEquals(1, response.getBody()
                                .size());
        assertThat(response.getBody(), contains(
            hasProperty("userName", is("ssmith"))));
    }

    @Test
    public void testUserSearchWithDateOfBirth_LessThanEqualsClause() {
        ResponseEntity<List<User>> response = template.exchange("/users/search?profile.dateOfBirth=lte(12/31/1969)",
                                                            HttpMethod.GET, null,
                                                            new ParameterizedTypeReference<List<User>>() {
                                                            });

        assertEquals(2, response.getBody()
                                .size());
        assertThat(response.getBody(), containsInAnyOrder(
            hasProperty("userName", is("bsummers")),
            hasProperty("userName", is("dgayle"))));
    }

    @Test
    public void testUserSearchWithDateOfBirth_LessThanAndGreaterThanClause() {
        ResponseEntity<List<User>> response = template.exchange("/users/search?profile.dateOfBirth=lt(1/1/1970)&profile.dateOfBirth=and(gt(12/31/1959))",
                                                                HttpMethod.GET, null,
                                                                new ParameterizedTypeReference<List<User>>() {
                                                                });

        assertEquals(1, response.getBody()
                                .size());
        assertThat(response.getBody(), contains(
            hasProperty("userName", is("dgayle"))));
    }

    @Test
    public void testUserSearchWithCreationDate_EqualsClause() {
        ResponseEntity<List<User>> response = template.exchange("/users/search?creationDate=2/21/19 14:15:36 UTC",
                                                                HttpMethod.GET, null,
                                                                new ParameterizedTypeReference<List<User>>() {
                                                                });

        assertEquals(1, response.getBody()
                                .size());
        assertThat(response.getBody(), contains(
            hasProperty("userName", is("bsummers"))));
    }

    @Test
    public void testUserSearchWithCreationDate_GreaterThanLessThanDateWithoutTimeClause() {
        ResponseEntity<List<User>> response = template.exchange("/users/search?creationDate=gt(2/21/19)&creationDate=and(lt(2/22/19))",
                                                                HttpMethod.GET, null,
                                                                new ParameterizedTypeReference<List<User>>() {
                                                                });

        assertEquals(2, response.getBody()
                                .size());
        assertThat(response.getBody(), containsInAnyOrder(
            hasProperty("userName", is("bsummers")),
            hasProperty("userName", is("dgayle"))));
    }


    /*
     * Demonstrates the use of BooleanPath natively
     */
    @Test
    public void testUserSearchWithEnabledStatusAsTrue() {
        ResponseEntity<List<User>> response = template.exchange("/users/search?enabled=true",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<User>>() {
                });

        assertEquals(3, response.getBody()
                .size());
        assertThat(response.getBody(), containsInAnyOrder(
                hasProperty("userName", is("bsummers")),
                hasProperty("userName", is("dgayle")),
                hasProperty("userName", is("ssmith"))));
    }

    /*
     * Demonstrates the use of BooleanPath natively
     */
    @Test
    public void testUserSearchWithEnabledStatusAsFalse() {
        ResponseEntity<List<User>> response = template.exchange("/users/search?enabled=false",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<User>>() {
                });

        assertEquals(1, response.getBody()
                .size());
        assertThat(response.getBody(), contains(
                hasProperty("userName", is("ksmith"))));
    }
}
