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
package com.trivadis.disruptor.trade.replication;

import java.util.concurrent.CountDownLatch;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.util.PaddedLong;
import com.trivadis.disruptor.trade.TradeEvent;

public final class ReplicationHandler implements EventHandler<TradeEvent> {
	private final PaddedLong counter = new PaddedLong();
	private long count;
	private CountDownLatch latch;

	public void reset(final CountDownLatch latch, final long expectedCount) {
		counter.set(0L);
		this.latch = latch;
		count = expectedCount;
	}

	public long getFizzBuzzCounter() {
		return counter.get();
	}

	@Override
	public void onEvent(final TradeEvent event, final long sequence, final boolean endOfBatch) throws Exception {
		if (0 == (event.getValue() % 3)) {
			event.setReplication(true);
		}

		if (latch != null && count == sequence) {
			latch.countDown();
		}
	}
}
