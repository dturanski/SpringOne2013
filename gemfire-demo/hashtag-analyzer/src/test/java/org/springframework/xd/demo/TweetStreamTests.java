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
package org.springframework.xd.demo;

import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.core.SubscribableChannel;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.xd.tuple.Tuple;

/**
 * @author David Turanski
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class TweetStreamTests {
	@Autowired
	SubscribableChannel output;
	@SuppressWarnings("unchecked")
	@Test
	
	public void test() throws InterruptedException {
		output.subscribe(new MessageHandler() {
			long start = new DateTime().getMillis();
			@Override
			public void handleMessage(Message<?> msg) throws MessagingException {
				System.out.println(msg.getHeaders().getTimestamp() -start + " " + msg.getHeaders().getId() + " "+ msg.getHeaders().get("delay"));
				Tuple t = (Tuple)msg.getPayload();
				Map<?,?> m = (Map<?,?>)t.getValue("entities");
				System.out.println(m.get("hashtags"));
				start = msg.getHeaders().getTimestamp();
			}
		});
 		Thread.sleep(60000);
	}
}
