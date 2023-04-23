package com.questhelper.dynamicsequencer;

import com.questhelper.questhelpers.QuestHelper;

import com.questhelper.requirements.Requirement;
import com.questhelper.steps.DetailedQuestStep;
import com.questhelper.steps.QuestStep;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.runelite.api.coords.WorldPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicSteps
{
	private static final Logger log = LoggerFactory.getLogger(DynamicSteps.class);

	private final QuestHelper quest;
	private final QuestStep activeStep;
	private final QuestStep step;
	private final WorldPoint stepLocation;
	private final List<Requirement> requirements;
	private final List<Requirement> recommended;

	public DynamicSteps(QuestHelper quest, QuestStep currentStep, WorldPoint stepLocation, List<Requirement> requirements, List<Requirement> recommended) {
		this.quest = quest;
		this.activeStep = currentStep.getActiveStep();
		this.stepLocation = stepLocation;
		this.requirements = requirements;
		this.recommended = recommended;
		this.step = getActiveStep();
	}
	public QuestHelper getQuest() {
		return quest;
	}
	public QuestStep getActiveStep() {
		return activeStep;
	}
	public WorldPoint getStepLocation() {
		return stepLocation;
	}
	public List<Requirement> getRequirements() {
		return requirements;
	}
	public List<Requirement> getRecommended() {
		return recommended;
	}
	public QuestStep getStep() {return step;}

	public static DynamicSteps currentNormalSteps(QuestHelper questHelper) {
		QuestStep activeStep = questHelper.getCurrentStep().getActiveStep();
		WorldPoint stepLocation = null;
		List<Requirement> requirements = null;
		List<Requirement> recommended = null;

		if (activeStep instanceof DetailedQuestStep) {
			DetailedQuestStep detailedQuestStep = (DetailedQuestStep) activeStep;
			stepLocation = detailedQuestStep.getWorldPoint();
			requirements = detailedQuestStep.getRequirements();
			recommended = detailedQuestStep.getRecommended();
		}
	//	log.debug("Active Step Parameters: {} {} {} {} {}", questHelper.getQuest().getName(), activeStep.getText(), stepLocation, requirements != null ? requirements.stream().map(Requirement::getDisplayText).collect(Collectors.joining(", ")) : "null", recommended != null ? recommended.stream().map(Requirement::getDisplayText).collect(Collectors.joining(", ")) : "null");
		return new DynamicSteps(questHelper, activeStep, stepLocation, requirements, recommended);
	}
	public static DynamicSteps fromQuestStep(QuestStep dynamicStep) {
		if (!(dynamicStep instanceof DetailedQuestStep)) {
			return null;
		}

		DetailedQuestStep detailedQuestStep = (DetailedQuestStep) dynamicStep;
		QuestHelper dynamicHelper = dynamicStep.getQuestHelper();
		WorldPoint stepLocation = detailedQuestStep.getWorldPoint();
		List<Requirement> requirements = detailedQuestStep.getRequirements();
		List<Requirement> recommended = detailedQuestStep.getRecommended();

	//	log.debug("Diary & Future Step Parameters: {} {} {} {} {}", dynamicHelper.getQuest().getName(), dynamicStep.getText(), stepLocation, requirements != null ? requirements.stream().map(Requirement::getDisplayText).collect(Collectors.joining(", ")) : "null", recommended != null ? recommended.stream().map(Requirement::getDisplayText).collect(Collectors.joining(", ")) : "null");
		return new DynamicSteps(dynamicHelper, dynamicStep, stepLocation, requirements, recommended);
	}


	public static Map<String, List<DynamicSteps>> extractParameters(List<QuestStep> currentSteps, List<QuestStep> remainingNormalSteps, List<QuestStep> diarySteps) {
		List<DynamicSteps> currentDynamicSteps = new ArrayList<>();
		List<DynamicSteps> remainingDynamicSteps = new ArrayList<>();
		List<DynamicSteps> diaryDynamicSteps = new ArrayList<>();

		// Populate the currentDynamicSteps list
		for (QuestStep step : currentSteps) {
			DynamicSteps dynamicStep = DynamicSteps.currentNormalSteps(step.getQuestHelper());
			currentDynamicSteps.add(dynamicStep);
		}

		// Populate the remainingDynamicSteps list
		for (QuestStep step : remainingNormalSteps) {
			DynamicSteps dynamicStep = DynamicSteps.fromQuestStep(step);
			remainingDynamicSteps.add(dynamicStep);
		}

		// Populate the diaryDynamicSteps list
		for (QuestStep step : diarySteps) {
			DynamicSteps dynamicStep = DynamicSteps.fromQuestStep(step);
			diaryDynamicSteps.add(dynamicStep);
		}

		Map<String, List<DynamicSteps>> DynamicStepParameters = new HashMap<>();
		DynamicStepParameters.put("currentSteps", currentDynamicSteps);
		DynamicStepParameters.put("remainingNormalSteps", remainingDynamicSteps);
		DynamicStepParameters.put("diarySteps", diaryDynamicSteps);
		return DynamicStepParameters;
	}
}
