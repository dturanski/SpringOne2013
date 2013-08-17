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
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.springframework.xd.analytics.metrics.core.FieldValueCounter;

import com.gemstone.gemfire.pdx.PdxInstance;

/**
 * @author David Turanski
 *
 */
public class HashTagAnalyzer {
	private boolean ignoreCase = true;

	public Map<String, Integer> aggregateAssociatedHashTags(Collection<Object> data, String targetHashTag) {

		FieldValueCounter hashTagCounts = new FieldValueCounter(targetHashTag + " Associated Hashtags");
		ValueComparator vc = new ValueComparator(hashTagCounts.getFieldValueCount());
		TreeMap<String, Integer> sorted = new TreeMap<String, Integer>(vc);

		for (Iterator<Object> it =  data.iterator(); it.hasNext();) {
			PdxInstance entry = (PdxInstance) it.next();
			List<String> associatedHashTags = getAssociatedHashTags(entry,targetHashTag);
			for (String hashTag: associatedHashTags) {
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

		for (Entry<String, Double> count: hashTagCounts.getFieldValueCount().entrySet()) {
			int i = count.getValue().intValue();
			sorted.put(count.getKey(),i);
		}
		for (Entry<String, Integer> count: sorted.entrySet()) {
			System.out.println(count.getKey() + "=" + count.getValue());
		}
		return sorted;
		
	}

	private List<String> getAssociatedHashTags(PdxInstance entry, String targetHashTag) {
		List<String> results = new ArrayList<String>();
		PdxInstance entities = (PdxInstance) entry.getField("entities");
		List<PdxInstance> hashTags = (List<PdxInstance>) entities.getField("hashTags");
		for (PdxInstance hashTag : hashTags) {

			String text = (String) hashTag.getField("text");
			if (!text.equalsIgnoreCase(targetHashTag)) {
				results.add(text);
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

	static class ValueComparator implements Comparator<String> {

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
