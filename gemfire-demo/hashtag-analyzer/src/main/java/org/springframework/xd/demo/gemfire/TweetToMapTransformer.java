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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.integration.transformer.MessageTransformationException;
import org.springframework.xd.tuple.Tuple;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author David Turanski
 *
 */
public class TweetToMapTransformer {
	ObjectMapper mapper = new ObjectMapper();

	public Map<String, Object> transform(Object obj) {
		if (obj instanceof Tuple) {
			return transformTuple((Tuple) obj);
		}
		throw new MessageTransformationException("Don't know how to transform " + obj.getClass().getName());
	}

	/**
	 * @param obj
	 * @return
	 */
	private Map<String, Object> transformTuple(Tuple tuple) {
		Map<String, Object> tweet = new HashMap<String, Object>();
		List<String> hashTagsList = new ArrayList<String>();
		tweet.put("id", tuple.getValue("id").toString());
		tweet.put("text", tuple.getString("text"));
		tweet.put("createdAt", tuple.getLong("createdAt"));
		tweet.put("language", tuple.getString("languageCode"));
		Map<?, ?> entities = (Map<?, ?>) tuple.getValue("entities");
		List<?> htlist = getHashTags(entities);
		for (Object obj : htlist) {
			Map<?, ?> htmap = (Map<?, ?>) obj;
			hashTagsList.add((String) htmap.get("text"));
		}
		tweet.put("hashTags", hashTagsList);
		return tweet;
	}

	private List<?> getHashTags(Map<?, ?> entities) {
		return (List<?>) (entities.get("hashtags") == null ? entities.get("hashTags") : entities.get("hashtags"));
	}
}
