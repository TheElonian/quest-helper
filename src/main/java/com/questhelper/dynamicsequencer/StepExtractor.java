package com.questhelper.dynamicsequencer;

import com.questhelper.questhelpers.QuestHelper;

import com.questhelper.requirements.Requirement;
import com.questhelper.steps.DetailedQuestStep;
import com.questhelper.steps.QuestStep;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.coords.WorldPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StepExtractor
{
	private static final Logger log = LoggerFactory.getLogger(StepExtractor.class);

	private final QuestHelper quest;
	private final QuestStep activeStep;
	private final QuestStep step;
	private final WorldPoint stepLocation;
	private final List<Requirement> requirements;
	private final List<Requirement> recommended;

	public StepExtractor(QuestHelper quest, QuestStep currentStep, WorldPoint stepLocation, List<Requirement> requirements, List<Requirement> recommended) {
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

	public static StepExtractor stepExtractor(QuestStep questStep) {
		if (!(questStep instanceof DetailedQuestStep)) {
			return null;
		}

		DetailedQuestStep detailedQuestStep = (DetailedQuestStep) questStep;
		QuestHelper dynamicHelper = questStep.getQuestHelper();
		WorldPoint stepLocation = detailedQuestStep.getWorldPoint();
		List<Requirement> requirements = detailedQuestStep.getRequirements();
		List<Requirement> recommended = detailedQuestStep.getRecommended();

	//	log.debug("Step Parameters: {} {} {} {} {}", dynamicHelper.getQuest().getName(), questStep.getText(), stepLocation, requirements != null ? requirements.stream().map(Requirement::getDisplayText).collect(Collectors.joining(", ")) : "null", recommended != null ? recommended.stream().map(Requirement::getDisplayText).collect(Collectors.joining(", ")) : "null");
		return new StepExtractor(dynamicHelper, questStep, stepLocation, requirements, recommended);
	}


	public static Map<String, List<StepExtractor>> typeExtractor(List<QuestStep> currentSteps, List<QuestStep> remainingSteps, List<QuestStep> diarySteps) {
		List<StepExtractor> currentLocationSteps = new ArrayList<>();
		List<StepExtractor> remainingLocationSteps = new ArrayList<>();
		List<StepExtractor> diaryLocationSteps = new ArrayList<>();

		// Populate the currentSteps list
		for (QuestStep step : currentSteps) {
			StepExtractor currentStep = StepExtractor.stepExtractor(step);
			currentLocationSteps.add(currentStep);
		}
		log.debug("Populated currentSteps list with {} steps", currentLocationSteps.size());

		// Populate the remainingSteps list
		for (QuestStep step : remainingSteps) {
			StepExtractor remainingStep = StepExtractor.stepExtractor(step);
			remainingLocationSteps.add(remainingStep);
		}
		log.debug("Populated remainingSteps list with {} steps", remainingLocationSteps.size());

		// Populate the diarySteps list
		for (QuestStep step : diarySteps) {
			StepExtractor diaryStep = StepExtractor.stepExtractor(step);
			diaryLocationSteps.add(diaryStep);
		}
		log.debug("Populated diarySteps list with {} steps", diaryLocationSteps.size());

		Map<String, List<StepExtractor>> locationSteps = new HashMap<>();
		locationSteps.put("currentSteps", currentLocationSteps);
		locationSteps.put("remainingSteps", remainingLocationSteps);
		locationSteps.put("diarySteps", diaryLocationSteps);


		return locationSteps;
	}
	@Override
	public String toString()
	{
	return "LocationSteps{" +
		"step='" + step.getText() + '\'' +
		", location=" + stepLocation + '\''  +
		", requirements='" + requirements + '\'' +
		", recommended='" + recommended + '\'' +
		'}';
	}
}
