package com.trivadis.disruptor.trade;

import com.lmax.disruptor.EventTranslator;

public class ExchangeEventTranslator implements EventTranslator<TradeEvent> {
	private long id;
	private long amount;
	private TradeType tradeType;

	public void setTrade(final TradeType tradeType, final long id, final long amount) {
		this.id = id;
		this.tradeType = tradeType;
		this.amount = amount;
	}

	@Override
	public TradeEvent translateTo(final TradeEvent event, final long sequence) {
		event.trade(tradeType, sequence, amount);
		return event;
	}
}
