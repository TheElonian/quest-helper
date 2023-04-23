package com.questhelper.dynamicsequencer;

import com.questhelper.QuestHelperConfig;
import com.questhelper.QuestHelperQuest;
import com.questhelper.panel.PanelDetails;
import com.questhelper.questhelpers.BasicQuestHelper;
import com.questhelper.questhelpers.QuestDetails;
import com.questhelper.questhelpers.QuestHelper;
import com.questhelper.requirements.Requirement;
import com.questhelper.steps.QuestStep;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicProcessor
{
	private static final Logger log = LoggerFactory.getLogger(DynamicProcessor.class);

	private final Client client;
	private final QuestHelperConfig config;
	private final EventBus eventBus;
	private final DynamicCache dynamicCache;
	public QuestHelper selectedQuest;

	public DynamicProcessor(Client client, QuestHelperConfig config, EventBus eventBus, DynamicCache dynamicCache)
	{
		this.client = client;
		this.config = config;
		this.eventBus = eventBus;
		this.dynamicCache = dynamicCache;
	}
	public List<QuestHelper> dynamicFilter() {
		List<QuestHelper> dynamicQuests = Collections.emptyList();

		if (client.getGameState() != GameState.LOGGED_IN) {
			return dynamicQuests;
		}

		QuestHelperConfig.QuestFilter dynamicSequencingFilter = QuestHelperConfig.QuestFilter.SHOW_MEETS_REQS;

		log.debug("Getting filtered quests");

		List<QuestHelper> normalQuests = QuestHelperQuest.getQuestHelpers()
			.stream()
			.filter(dynamicSequencingFilter)
			.filter(q -> {
				QuestDetails.Type questType = q.getQuest().getQuestType();
				return questType == QuestDetails.Type.F2P || questType == QuestDetails.Type.P2P || questType == QuestDetails.Type.MINIQUEST;
			})
			.collect(Collectors.toList());

		log.debug("Filtering diary quests...");
		List<QuestHelper> diaryQuests = QuestHelperQuest.getQuestHelpers()
			.stream()
			.filter(q -> q.getQuest().getQuestType() == QuestDetails.Type.ACHIEVEMENT_DIARY)
			.collect(Collectors.toList());

		log.debug("Concatenating quests...");
		dynamicQuests = Stream.concat(normalQuests.stream(), diaryQuests.stream())
			.filter(q -> !q.isCompleted()) // exclude completed quests
			.collect(Collectors.toList());

		log.debug("Available quests: " + dynamicQuests.size());

		return dynamicQuests;
	}

	public void startUpDynamicQuest(String dynamicHelperName, Consumer<QuestHelper> callback) {
		if (!(client.getGameState() == GameState.LOGGED_IN)) {
			return;
		}
		if (dynamicCache.contains(dynamicHelperName)) {
			log.debug("Found dynamic quest {} in cache.", dynamicHelperName);
			callback.accept(dynamicCache.get(dynamicHelperName));
			return;
		}
		log.debug("Checking selected quest...");
		if (selectedQuest != null && selectedQuest.getQuest().getName().equals(dynamicHelperName)) {
			log.debug("Selected quest is already {}.", dynamicHelperName);
			return;
		}
		log.debug("Starting up dynamic quest {}...", dynamicHelperName);
		QuestHelper dynamicHelper = QuestHelperQuest.getByName(dynamicHelperName);
		if (dynamicHelper == null) {
			log.debug("Dynamic quest {} not found.", dynamicHelperName);
			return;
		}
		if (dynamicHelper.isCompleted()) {
			log.debug("Dynamic quest {} is already completed.", dynamicHelperName);
			return;
		}
		log.debug("Registering dynamic quest helper...");
		eventBus.register(dynamicHelper);
		dynamicHelper.startUp(config);
		dynamicCache.put(dynamicHelperName, dynamicHelper);

		if (dynamicHelper.getCurrentStep() == null) {
			log.debug("Dynamic quest {} has no current step. Shutting down.", dynamicHelperName);
			dynamicHelper.shutDown();
			eventBus.unregister(dynamicHelper);
			dynamicCache.remove(dynamicHelperName);
		} else {
			log.debug("Callback accepted for dynamic quest {}.", dynamicHelperName);
			callback.accept(dynamicHelper);
		}
	}

	public interface DynamicExtractionCallback {
		void onCompleted(List<StepExtractor> currentSteps, List<StepExtractor> remainingNormalSteps, List<StepExtractor> diarySteps);
	}
	public void extractDynamicQuests(List<QuestHelper> dynamicQuests, DynamicExtractionCallback callback) {
		log.debug("Starting dynamic quest extraction...");
		List<QuestStep> currentSteps = new ArrayList<>();
		List<QuestStep> remainingSteps = new ArrayList<>();
		List<QuestStep> diarySteps = new ArrayList<>();

		if (dynamicQuests.isEmpty()) {
			log.debug("Dynamic quest list is empty. Returning parameters.");
			Map<String, List<StepExtractor>> dynamicStepParameters = StepExtractor.typeExtractor(currentSteps, remainingSteps, diarySteps);
			callback.onCompleted(dynamicStepParameters.get("currentSteps"), dynamicStepParameters.get("remainingSteps"), dynamicStepParameters.get("diarySteps"));
			return;
		}

		processDynamicQuests(dynamicQuests, 0, currentSteps, remainingSteps, diarySteps, callback);
	}
	private void processDynamicQuests(List<QuestHelper> DynamicQuests, int currentIndex, List<QuestStep> currentSteps, List<QuestStep> remainingSteps, List<QuestStep> diarySteps, DynamicExtractionCallback callback) {
		log.debug("Entering processDynamicQuests with currentIndex: {}", currentIndex);

		if (currentIndex >= DynamicQuests.size()) {
			log.debug("Current index is greater or equal to the size of DynamicQuests. Calling callback.onCompleted.");
			Map<String, List<StepExtractor>> dynamicStepParameters = StepExtractor.typeExtractor(currentSteps, remainingSteps, diarySteps);
			callback.onCompleted(dynamicStepParameters.get("currentSteps"), dynamicStepParameters.get("remainingSteps"), dynamicStepParameters.get("diarySteps"));
			return;
		}

		QuestHelper questHelper = DynamicQuests.get(currentIndex);
		log.debug("Processing quest: {}", questHelper.getQuest().getName());

		startUpDynamicQuest(questHelper.getQuest().getName(), dynamicQuestHelper -> {
			if (dynamicQuestHelper == null) {
				log.debug("DynamicQuestHelper is null, continuing to next quest.");
				processDynamicQuests(DynamicQuests, currentIndex + 1, currentSteps, remainingSteps, diarySteps, callback);
			} else {
				if (dynamicQuestHelper.getQuest().getQuestType() == QuestDetails.Type.ACHIEVEMENT_DIARY) {
					List<PanelDetails> panelDetailsList = dynamicQuestHelper.getPanels();
					for (PanelDetails panelDetail : panelDetailsList) {
						// Check if the header is "Finishing Off"
						log.debug("PanelDetail header: {}", panelDetail.getHeader());
						if ("Finishing off".equals(panelDetail.getHeader())) {
							log.debug("PanelDetail header is Finishing off, skipping: {}", panelDetail.getHeader());
							continue;
						}
						// Check if the panel is hidden
						Requirement hideCondition = panelDetail.getHideCondition();
						if (hideCondition != null && hideCondition.check(client)) {
							continue;
						}
						// Check if all requirements are met
						if (panelDetail.getRequirements().stream().allMatch(requirement -> requirement.check(client))) {
							List<QuestStep> questSteps = panelDetail.getSteps();
							diarySteps.add(questSteps.get(0));
						}
					}
				} else if (dynamicQuestHelper instanceof BasicQuestHelper) {
					BasicQuestHelper basicQuestHelper = (BasicQuestHelper) dynamicQuestHelper;

					List<QuestStep> filteredSteps = basicQuestHelper.getFilteredSteps();
					QuestStep currentStep = dynamicQuestHelper.getCurrentStep().getActiveStep();

					boolean foundCurrentStep = false;
					for (QuestStep step : filteredSteps) {
						if (step == currentStep) {
							foundCurrentStep = true;
							currentSteps.add(step);
						} else {
							if (foundCurrentStep) {
								remainingSteps.add(step);
							}
						}
					}
				}
				if (dynamicQuestHelper.isCompleted()) {
					log.debug("DynamicQuestHelper is completed. Shutting down, unregistering, and removing from cache.");
					dynamicQuestHelper.shutDown();
					eventBus.unregister(dynamicQuestHelper);
					dynamicCache.remove(dynamicQuestHelper.getQuest().getName());
				}
				processDynamicQuests(DynamicQuests, currentIndex + 1, currentSteps, remainingSteps, diarySteps, callback);
			}
		});
	}
}
