/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.xd.demo.gemfire;

import javax.annotation.Resource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.stereotype.Component;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.query.SelectResults;

/**
 * @author David Turanski
 *
 */
@Component
public class HashtagQuery implements InitializingBean {
	@Resource(name = "hashtags")
	Region hashtags;
	private GemfireTemplate template;

	public void executeComplex(String query, Object... params) {
		SelectResults results = template.find(query, params);
		System.out.println(results.size());
		for (Object obj : results) {
			System.out.println(obj);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		template = new GemfireTemplate(hashtags);
	}

	public static void main(String... args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring/client-cache.xml");
		HashtagQuery query = context.getBean(HashtagQuery.class);
		query.executeComplex("select * from /hashtags h where (h['hashTags'].contains('java') or h.contains('Java') or h.contains('JAVA'))");
	}
}
