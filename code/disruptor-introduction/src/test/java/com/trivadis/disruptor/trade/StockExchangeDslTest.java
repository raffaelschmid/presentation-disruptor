/*
 * Copyright 2011 LMAX Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.trivadis.disruptor.trade;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;

import com.lmax.disruptor.SingleThreadedClaimStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.trivadis.disruptor.trade.journaling.JournalingHandler;
import com.trivadis.disruptor.trade.replication.ReplicationHandler;

public final class StockExchangeDslTest {
	private static final int NUM_EVENT_PROCESSORS = 3;
	private static final int BUFFER_SIZE = 1024 * 8;
	private static final long ITERATIONS = 1000L * 1000L * 100L;
	private final ExecutorService EXECUTOR = Executors.newFixedThreadPool(NUM_EVENT_PROCESSORS);

	private final long expectedResult;
	{
		long temp = 0L;

		for (long i = 0; i < ITERATIONS; i++) {
			final boolean fizz = 0 == (i % 3L);
			final boolean buzz = 0 == (i % 5L);

			if (fizz && buzz) {
				++temp;
			}
		}

		expectedResult = temp;
	}

	@Test
	public void test_exchange() throws Exception {
		final CountDownLatch latch = new CountDownLatch(1);
		final BusinessLogicHandler blHandler = new BusinessLogicHandler();
		final Disruptor<TradeEvent> disruptor = new Disruptor<TradeEvent>(TradeEvent.EVENT_FACTORY, EXECUTOR,
				new SingleThreadedClaimStrategy(BUFFER_SIZE), new YieldingWaitStrategy());
		blHandler.reset(latch, ITERATIONS - 1);
		disruptor.handleEventsWith(new ReplicationHandler(), new JournalingHandler()).then(new BusinessLogicHandler());
		disruptor.start();

		final int amount = 50;
		final ExchangeEventTranslator eventTranslator = new ExchangeEventTranslator();
		for (long i = 0; i < ITERATIONS; i++) {
			eventTranslator.setTrade(TradeType.BID, i, amount);
			disruptor.publishEvent(eventTranslator);
		}
		latch.await();
		disruptor.halt();

		Assert.assertEquals(expectedResult, blHandler.getCounter());
	}
}
