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
package org.galegofer.spring.data.querydsl.value.operators.experimental

import com.querydsl.core.types.EntityPath
import com.querydsl.core.types.Path
import jakarta.servlet.http.HttpServletRequest
import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Specification


/**
 * Specification tests QuerydslHttpRequestContext
 *
 * @author gt_tech
 */
class QuerydslHttpRequestContextSpecs extends Specification {

    def rootPath = [toString: { 'user' }, getType: { User.class }] as EntityPath

    def firstNamePath = [toString: { 'user.profile.firstName' }, getRoot: { rootPath }] as Path
    def lastNamePath = [toString: { 'user.profile.lastName' }, getRoot: { rootPath }] as Path
    def agePath = [toString: { 'user.profile.age' }, getRoot: { rootPath }] as Path

    def mockHttpServletRequest = new MockHttpServletRequest()

    QuerydslHttpRequestContext context

    HttpServletRequest wrappedRequest

    def setup() {
        def parameters = ['profile.firstName': ['eq(John)', 'and(not(startsWith(Harr)))'] as String[], 'profile.lastName': 'Doe', 'profile.age': 'gte(27)']
        mockHttpServletRequest.addParameters(parameters)

        context = new QuerydslHttpRequestContext(rootPath, mockHttpServletRequest)
        wrappedRequest = context.getWrappedHttpServletRequest()
    }

    def "it must return a wrapped request"() {
        expect:
        !(wrappedRequest.is(mockHttpServletRequest))
        context.getOriginalHttpServletRequest().is(mockHttpServletRequest)
    }

    def "it must return transformed parameter values"() {
        expect:
        wrappedRequest.getParameter('profile.age') == '27'
        wrappedRequest.getParameter('profile.lastName') == 'Doe'
        wrappedRequest.getParameterValues('profile.firstName')[0..1] == ['John', 'Harr'] as String[]
    }

    def "it must return original parameter values for agePath"() {
        expect:
        context.getSingleValue(agePath) == 'gte(27)'
        context.getAllValues(agePath).size() == 1
        context.getAllValues(agePath)[0] == 'gte(27)'
    }

    def "it must return original parameter values for firstName"() {
        expect:
        context.getSingleValue(firstNamePath) == 'eq(John)'
        context.getAllValues(firstNamePath).size() == 2
        context.getAllValues(firstNamePath)[0..1] == ['eq(John)', 'and(not(startsWith(Harr)))'] as String[]
    }

    def "it must return original parameter values for lastName"() {
        expect:
        context.getSingleValue(lastNamePath) == 'Doe'
        context.getAllValues(lastNamePath).size() == 1
        context.getAllValues(lastNamePath)[0] == 'Doe'
    }

    // ============== START: Test/Stub classes ==============
    static class User {}
    // ============== STOP: Test/Stub classes ==============
}
