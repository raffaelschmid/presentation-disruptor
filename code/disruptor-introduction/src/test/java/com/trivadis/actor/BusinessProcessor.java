package com.trivadis.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;

public class BusinessProcessor extends UntypedActor {

	@Override
	public void onReceive(final Object trade) throws Exception {
		if (trade instanceof Trade)
			System.out.println("trading " + ((Trade) trade).product);
	}

	public static void main(final String[] args) {
		final ActorSystem system = ActorSystem.create("TradingSystem");
		final ActorRef greeter = system.actorOf(new Props(BusinessProcessor.class));
		greeter.tell(new Trade("GBP/USD"));
	}
}