package space.cutekitten.commandhelper.client;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardPlayerScore;

import java.text.Collator;
import java.util.List;
import java.util.Locale;

public class PinnedScore {
    public String player;
    public String objective;

    public PinnedScore(ScoreboardPlayerScore score) {
        this.objective = score.getObjective().getName();
        this.player = score.getPlayerName();
    }

    public PinnedScore(String objective, String player) {
        this.objective = objective;
        this.player = player;
    }

    public static List<ScoreboardPlayerScore> toScoreboardPlayerScores(Scoreboard scoreboard, List<PinnedScore> scores, String searched) {
        Collator collator = Collator.getInstance(Locale.ROOT);
        return scores.stream().filter(
//                        filter out scores that don't match the search filter
                score -> score.player.toLowerCase(Locale.ROOT).contains(searched) &&
//                        filter out scores with objectives that don't exist to avoid NPE on 3rd filter
                        scoreboard.containsObjective(score.objective) &&
//                        filter out scores with players that don't exist to avoid creating new scores
                        scoreboard.getKnownPlayers().contains(score.player) &&
//                        filter out scores if the objective doesn't exist for the player
                        scoreboard.getPlayerObjectives(score.player).containsKey(scoreboard.getObjective(score.objective))).sorted(
//                sort by player name
                (score1, score2) -> collator.compare(score1.player, score2.player)).map(
//                convert to ScoreboardPlayerScore
                score -> scoreboard.getPlayerScore(score.player, scoreboard.getObjective(score.objective))).toList();
    }

    public static List<PinnedScore> containsScore(List<PinnedScore> scores, ScoreboardPlayerScore score) {
        return scores.stream().filter(
                pinnedScore -> pinnedScore.player.equals(score.getPlayerName()) &&
                        pinnedScore.objective.equals(score.getObjective().getName())).toList();
    }
}
