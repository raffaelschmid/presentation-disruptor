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

import com.lmax.disruptor.EventFactory;

public final class TradeEvent {

	private boolean replicated = false;
	private boolean journaled = false;
	private long value = 0;
	private long amount;
	private TradeType type;

	public long getValue() {
		return value;
	}

	public void trade(final TradeType type, final long value, final long amount) {
		replicated = false;
		journaled = false;
		this.type = type;
		this.value = value;
		this.amount = amount;
	}

	public boolean isReplicated() {
		return replicated;
	}

	public void setReplication(final boolean replicated) {
		this.replicated = replicated;
	}

	public boolean isJournaled() {
		return journaled;
	}

	public void setJournaled(final boolean journaled) {
		this.journaled = journaled;
	}

	public long getAmount() {
		return amount;
	}

	public final static EventFactory<TradeEvent> EVENT_FACTORY = new EventFactory<TradeEvent>() {
		@Override
		public TradeEvent newInstance() {
			return new TradeEvent();
		}
	};
}
