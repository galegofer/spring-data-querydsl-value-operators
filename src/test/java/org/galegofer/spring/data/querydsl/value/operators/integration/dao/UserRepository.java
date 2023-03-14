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
package org.galegofer.spring.data.querydsl.value.operators.integration.dao;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import jakarta.transaction.Transactional;
import org.galegofer.spring.data.querydsl.value.operators.ExpressionProviderFactory;
import org.galegofer.spring.data.querydsl.value.operators.integration.model.QUser;
import org.galegofer.spring.data.querydsl.value.operators.integration.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


/**
 *
 * Spring data repository for {@link User} resource which is also exposed as a
 * {@link org.springframework.web.bind.annotation.RestController} and provides search capabilities using this SDK
 * library.
 *
 * @author gt_tech
 */
@Transactional
@Repository
@RepositoryRestResource(collectionResourceRel = "users", path = "/users")
@RequestMapping(value = "/users")
public interface UserRepository extends JpaRepository<User, String>, QuerydslPredicateExecutor<User>,
                                        QuerydslBinderCustomizer<QUser> {

    @RequestMapping(path = {"/search"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {
            RequestMethod.GET,
            RequestMethod.POST
    })
    default ResponseEntity<Iterable<User>> searchUser(@QuerydslPredicate(root = User.class) Predicate
                                                              predicate) {
        System.out.println("Predicate: " + predicate);


        if (predicate == null || (BooleanBuilder.class.isAssignableFrom(predicate.getClass())
                && !((BooleanBuilder) predicate).hasValue())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            return ResponseEntity.ok(this.findAll(predicate));
        }
    }

    @Override
    default void customize(QuerydslBindings bindings, QUser root) {

        bindings.bind(root.userName)
                .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values));
        bindings.bind(root.creationDate)
            .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values));
        bindings.bind(root._id)
                .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values));

        bindings.bind(root.profile.firstName)
                .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values));
        bindings.bind(root.profile.lastName)
                .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values));

        bindings.bind(root.profile.dateOfBirth)
          .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values));

        bindings.bind(root.employeeId)
                .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values));


        // demonstrates usage of BooleanPath natively// doing type conversion, ugh! here since test's setup uses
        // experimental advanced process that disables type-conversion in Spring and hence the value received
        // is of String type.
        bindings.bind(root.enabled).firstOptional((path, optionalValue) ->
                optionalValue.map( v -> path.eq(v))
        );

        // Demonstration of how a certain attribute or search parameter can be compared on single-value level for
        // indicating the fact that search interface doesn't expect API consumer to provide multiple values for this
        // parameter and if that happens, it will use only the first value for querying.
        // Since in case of using single-valued the value passed to it is wrapped in Optional monad and
        // ExpressionProvider has a contract that value passed to it must not be Optional so it is unwrapped here.
        bindings.bind(root.profile.age)
                .firstOptional((path, optionalValue) -> ExpressionProviderFactory.getPredicate(path, optionalValue
                        .orElseGet(() -> null)));


        bindings.bind(root.emails.any().address)
                .as("emails.address")
                .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values));
        // Not Required for StringPath values but it's recommended to register
        // alias(es) explicitly.
        // ListPath won't work without alias anyway easily or as elegantly..
        ExpressionProviderFactory.registerAlias(root.emails.any().address, "emails.address");


        bindings.bind(root.status)
                .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values));

        bindings.bind(root.jobData.department)
                .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values));
        bindings.bind(root.jobData.location)
                .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values));

        /*
         * Demonstration of black-listing non-searchable parameters/fields
         */
        bindings.excluding(root.profile.middleName);

        /*
         * Another mechanism for black-listing unlisted properties. Note this
         * may require explicit addition of including(..) paths for other than
         * where an explicit alias is provided.
         */
        // bindings.excludeUnlistedProperties(true);

    }
}
