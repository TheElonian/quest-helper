package com.questhelper.dynamicsequencer;

import com.questhelper.questhelpers.QuestHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.runelite.client.eventbus.EventBus;

public class DynamicCache
{
	private final Map<String, QuestHelper> dynamicHelperCache;
	private final Map<String, List<StepExtractor>> stepsCache;
	private final EventBus eventBus;

	public DynamicCache(EventBus eventBus)
	{
		this.eventBus = eventBus;
		dynamicHelperCache = new HashMap<>();
		stepsCache = new HashMap<>();
	}


	public QuestHelper get(String questHelperName)
	{
		return dynamicHelperCache.get(questHelperName);
	}

	public void put(String questHelperName, QuestHelper questHelper)
	{
		dynamicHelperCache.put(questHelperName, questHelper);
	}

	public void remove(String questHelperName)
	{
		dynamicHelperCache.remove(questHelperName);
	}

	public boolean contains(String questHelperName)
	{
		return dynamicHelperCache.containsKey(questHelperName);
	}

	public List<StepExtractor> getSteps(String questHelperName)
	{
		return stepsCache.get(questHelperName);
	}
}
