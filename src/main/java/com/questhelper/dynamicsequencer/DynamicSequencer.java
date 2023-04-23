package com.questhelper.dynamicsequencer;

import net.runelite.api.Client;
import net.runelite.client.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicSequencer {
	private final DynamicCache dynamicCache;
	private final EventBus eventBus;
	private final Client client;
	private static final Logger log = LoggerFactory.getLogger(DynamicSequencer.class);

	public DynamicSequencer(EventBus eventBus, Client client) {
		this.eventBus = eventBus;
		this.dynamicCache = new DynamicCache(eventBus);
		this.client = client;
	}
}
