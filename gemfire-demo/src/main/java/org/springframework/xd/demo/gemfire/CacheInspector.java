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

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.cache.client.ClientRegionShortcut;
import com.gemstone.gemfire.pdx.JSONFormatter;
import com.gemstone.gemfire.pdx.PdxInstance;

/**
 * @author David Turanski
 *
 */
public class CacheInspector {

	private static String GEMFIRE_HOST = "localhost";
	private static int GEMFIRE_PORT = 40404;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ClientCache cache = new ClientCacheFactory().addPoolServer(GEMFIRE_HOST, GEMFIRE_PORT)
				.create();
				
		Region<Object,Object> tweets = cache.createClientRegionFactory(ClientRegionShortcut.CACHING_PROXY)
				.create("tweets");
		for (Object key: tweets.keySetOnServer()) {
			PdxInstance entry = (PdxInstance)tweets.get(key);
		}
		new HashTagAnalyzer().analyzeHashTags(tweets.values(),"java");
	}

	/**
	 * @param entry
	 */
	private static void display(PdxInstance entry) {
		for (String name: entry.getFieldNames()) {
			System.out.println(name + ":" + entry.getField(name));
		}
		PdxInstance entities = (PdxInstance) entry.getField("entities");
		for (String name: entities.getFieldNames()) {
			System.out.println("\t" +name + ":" + entities.getField(name));
		}
		
	}

}
