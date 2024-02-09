package pkg.deepCurse.pandora.common.util.interfaces;

public interface PlayerGrueDataInterface {

	short getTutorialEncountersLeft();

	void setTutorialEncountersLeft(short tutorialEncountersLeft);

	long getLastTutorialEncounterTime();

	void setLastTutorialEncounterTime(long lastTutorialEncounterTime);

	boolean skipTimeCheck();

	void setSkipTimeCheck(boolean skipTimeCheck);
}
