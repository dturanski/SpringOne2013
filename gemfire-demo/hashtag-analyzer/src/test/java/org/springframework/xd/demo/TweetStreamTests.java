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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.core.PollableChannel;
import org.springframework.integration.message.GenericMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.xd.demo.gemfire.TweetSummary;
/**
 * @author David Turanski
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class TweetStreamTests {
	@Autowired
	PollableChannel output;
	@Autowired
	MessageChannel input;

	@Test
	@Ignore
	public void test() throws InterruptedException, IOException {
		File file = new File("../../data/twitter.out");

		BufferedReader reader = new BufferedReader(new FileReader(file));
		
		String tweet = null;
		int num = 0;
		while ((tweet = reader.readLine())!=null && num < 10){
			input.send(new GenericMessage<String>(tweet));
			Message<?> msg;
			while ((msg = output.receive(1000) )!= null) {
				TweetSummary summary = (TweetSummary)msg.getPayload();
				if (summary.getHashTags().size() > 0) {
					num++;
					System.out.println(summary.getHashTags());
				}
			}
		}
		reader.close();

	}
}
