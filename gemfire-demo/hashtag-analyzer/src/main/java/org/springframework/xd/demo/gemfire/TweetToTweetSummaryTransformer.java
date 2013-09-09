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
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.integration.transformer.MessageTransformationException;
import org.springframework.xd.tuple.Tuple;
import org.springframework.xd.tuple.integration.JsonToTupleTransformer;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author David Turanski
 * Converts a {@link Tuple} to a {@link TweetSummary}
 */
public class TweetToTweetSummaryTransformer {
	private static Log logger = LogFactory.getLog(TweetToTweetSummaryTransformer.class);
	
	ObjectMapper mapper = new ObjectMapper();
	
	JsonToTupleTransformer jsonToTupleTransformer = new JsonToTupleTransformer();

	public TweetSummary transform(Object obj) {
		try {
		if (obj instanceof String) {
			obj = jsonToTupleTransformer.transformPayload((String) obj);
		}
		if (obj instanceof Tuple) {
			return transformTuple((Tuple) obj);
		}
		logger.error("Don't know how to transform " + obj.getClass().getName());
		return null;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw new MessageTransformationException(e.getMessage());
		}
	}

	/**
	 * @param obj
	 * @return
	 */
	private TweetSummary transformTuple(Tuple tuple) {
		TweetSummary tweet = new TweetSummary();
		List<String> hashTagsList = new ArrayList<String>();
		tweet.setId(tuple.getValue("id").toString());
		tweet.setText(tuple.getString("text"));
		tweet.setLang(tuple.getString("languageCode"));
		tweet.setCreatedAt(tuple.getLong("createdAt"));
		Map<?, ?> entities = (Map<?, ?>) tuple.getValue("entities");
		List<?> htlist = getHashTags(entities);
		for (Object obj : htlist) {
			Map<?, ?> htmap = (Map<?, ?>) obj;
			hashTagsList.add((String) htmap.get("text"));
		}
		tweet.setHashTags(hashTagsList);
		return tweet;
	}

	private List<?> getHashTags(Map<?, ?> entities) {
		return (List<?>) (entities.get("hashtags") == null ? entities.get("hashTags") : entities.get("hashtags"));
	}
}
