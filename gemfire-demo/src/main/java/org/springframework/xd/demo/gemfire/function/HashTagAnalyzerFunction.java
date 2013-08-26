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
package org.springframework.xd.demo.gemfire.function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.springframework.data.gemfire.function.annotation.GemfireFunction;
import org.springframework.data.gemfire.function.annotation.RegionData;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.xd.analytics.metrics.core.FieldValueCounter;

/**
 * Aggregates counts and sorts by descending count for associated hash tags present with a target hashTag
 * @author David Turanski
 *
 */
@Component
public class HashTagAnalyzerFunction {
	private boolean ignoreCase = true;

	@SuppressWarnings("unchecked")
	@GemfireFunction(hasResult = true)
	public Map<String, Integer> aggregateAssociatedHashTags(@RegionData Map<?, ?> data, String targetHashTag) {
		FieldValueCounter hashTagCounts = new FieldValueCounter(targetHashTag + " Associated Hashtags");
		ValueComparator vc = new ValueComparator(hashTagCounts.getFieldValueCount());
		TreeMap<String, Integer> sorted = new TreeMap<String, Integer>(vc);

		for (Object obj : data.values()) {
			Map<String, Object> entry = (Map<String, Object>) obj;
			List<String> associatedHashTags = getAssociatedHashTags(entry, targetHashTag);
			for (String hashTag : associatedHashTags) {
				if (ignoreCase) {
					hashTag = hashTag.toLowerCase();
				}
				Double count = hashTagCounts.getFieldValueCount().get(hashTag);
				if (count == null) {
					hashTagCounts.getFieldValueCount().put(hashTag, new Double(0));
				}
				count = hashTagCounts.getFieldValueCount().get(hashTag) + 1;

				hashTagCounts.getFieldValueCount().put(hashTag, count);
			}
		}

		for (Entry<String, Double> count : hashTagCounts.getFieldValueCount().entrySet()) {
			int i = count.getValue().intValue();
			sorted.put(count.getKey(), i);
		}

		return sorted;

	}

	@SuppressWarnings("unchecked")
	private List<String> getAssociatedHashTags(Map<String, Object> entry, String targetHashTag) {
		List<String> results = new ArrayList<String>();
		List<String> hashTags = (List<String>) entry.get("hashTags");
		if (!CollectionUtils.isEmpty(hashTags)) {
			for (String hashTag : hashTags) {
				if (!hashTag.equalsIgnoreCase(targetHashTag)) {
					results.add(hashTag);
				}
			}
		}
		return results;
	}

	/**
	 * @param ignoreCase the ignoreCase to set
	 */
	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

	@SuppressWarnings("serial")
	static class ValueComparator implements Comparator<String>, Serializable {

		Map<String, Double> base;

		ValueComparator(Map<String, Double> base) {
			this.base = base;
		}

		@Override
		public int compare(String a, String b) {
			if (base.get(a) <= base.get(b)) {
				return 1;
			} else {
				return -1;
			}
		}
	}
}
