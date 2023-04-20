package com.questhelper.dynamicsequencer;

import com.questhelper.questhelpers.QuestHelper;

import com.questhelper.requirements.Requirement;
import com.questhelper.steps.DetailedQuestStep;
import com.questhelper.steps.QuestStep;
import java.util.List;
import java.util.stream.Collectors;
import net.runelite.api.coords.WorldPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicProgress {
	private static final Logger log = LoggerFactory.getLogger(DynamicProgress.class);

	private final QuestHelper quest;
	private final QuestStep activeStep;
	private final WorldPoint stepLocation;
	private final List<Requirement> requirements;
	private final List<Requirement> recommended;

	public DynamicProgress(QuestHelper quest, QuestStep currentStep, WorldPoint stepLocation, List<Requirement> requirements, List<Requirement> recommended) {
		this.quest = quest;
		this.activeStep = currentStep.getActiveStep();
		this.stepLocation = stepLocation;
		this.requirements = requirements;
		this.recommended = recommended;
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

	public static DynamicProgress fromQuestHelper(QuestHelper questHelper) {
		QuestStep activeStep = questHelper.getCurrentStep().getActiveStep();
		WorldPoint stepLocation = null;
		List<Requirement> requirements = null; // Initialize as null
		List<Requirement> recommended = null; // Initialize as null

		if (activeStep instanceof DetailedQuestStep) {
			DetailedQuestStep detailedQuestStep = (DetailedQuestStep) activeStep;
			stepLocation = detailedQuestStep.getWorldPoint();
			requirements = detailedQuestStep.getRequirements();
			recommended = detailedQuestStep.getRecommended();
		}
		log.debug("Dynamic Parameters: {} {} {} {} {}", questHelper.getQuest().getName(), activeStep.getText(), stepLocation, requirements != null ? requirements.stream().map(Requirement::getDisplayText).collect(Collectors.joining(", ")) : "null", recommended != null ? recommended.stream().map(Requirement::getDisplayText).collect(Collectors.joining(", ")) : "null");
		return new DynamicProgress(questHelper, activeStep, stepLocation, requirements, recommended);
	}
}
