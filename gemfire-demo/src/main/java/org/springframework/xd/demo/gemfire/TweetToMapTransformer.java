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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * @author David Turanski
 *
 */
public class TweetToMapTransformer {
	ObjectMapper mapper = new ObjectMapper();
	public Map<String,Object> transform(JsonNode root) {
		Map<String,Object> tweet = new HashMap<String,Object>();
		List<String> hashTagsList = new ArrayList<String>();
		ArrayNode hashTags = (ArrayNode) root.get("entities").get("hashTags");
		for (Iterator<JsonNode> it = hashTags.elements(); it.hasNext();) {
			JsonNode hashTag = it.next();
			hashTagsList.add(hashTag.get("text").textValue());
		}
		tweet.put("id",root.get("id").toString());
		tweet.put("text",root.get("text").textValue());
		tweet.put("createdAt", root.get("createdAt").asLong());
		tweet.put("language", root.get("languageCode").textValue());
		tweet.put("hashTags",hashTagsList);
		return tweet;
	}
}
