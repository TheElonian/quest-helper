package com.questhelper.dynamicsequencer;

import com.questhelper.questhelpers.QuestHelper;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.runelite.client.eventbus.EventBus;

public class DynamicCache {
	private final Map<String, QuestHelper> cache;
	private final EventBus eventBus;

	public DynamicCache(EventBus eventBus) {
		this.eventBus = eventBus;
		cache = new HashMap<>();
	}

	public QuestHelper get(String questHelperName) {
		return cache.get(questHelperName);
	}

	public void put(String questHelperName, QuestHelper questHelper) {
		cache.put(questHelperName, questHelper);
	}

	public void remove(String questHelperName) {
		cache.remove(questHelperName);
	}

	public boolean contains(String questHelperName) {
		return cache.containsKey(questHelperName);
	}
	public Set<String> getCacheKeys() {
		return cache.keySet();
	}
	public void registerQuestHelper(QuestHelper questHelper) {
		cache.put(questHelper.getQuest().getName(), questHelper);
		eventBus.register(questHelper);
	}

	public void unregisterQuestHelper(String questHelperName) {
		QuestHelper questHelper = cache.get(questHelperName);
		if (questHelper != null) {
			eventBus.unregister(questHelper);
			cache.remove(questHelperName);
		}
	}
}
