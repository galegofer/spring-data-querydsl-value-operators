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
package org.galegofer.spring.data.querydsl.value.operators.integration.model;

public enum Department {

	HR("HR"),
	SALES("SALES"),
	MARKETING("MARKETING"),
	FINANCE("FINANCE"),
	IT("IT");

	private String value;

	Department(String value) {
		this.value = value;
	}

	public String toString() {
		return String.valueOf(value);
	}

	public static Department fromValue(String text) {
		for (Department b : Department.values()) {
			if (String.valueOf(b.value)
					.equals(text)) {
				return b;
			}
		}
		return null;
	}
}
