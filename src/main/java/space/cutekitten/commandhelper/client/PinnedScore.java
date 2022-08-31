package space.cutekitten.commandhelper.client;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.util.Util;

import java.io.File;
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

    public static NbtList toNbt(List<PinnedScore> pins) {
        ClientDB.currentSearch = "";

        NbtList list = new NbtList();

        list.addAll(pins.stream().map(score -> {
            NbtCompound c = new NbtCompound();
            c.putString("objective", score.objective);
            c.putString("player", score.player);

            return c;
        }).toList());

        return list;
    }

    public static void fromNbt(NbtList list, List<PinnedScore> pins) {
        ClientDB.currentSearch = "";

        pins.clear();

        list.forEach(element -> {
            if (!(element instanceof NbtCompound compound)) return;

            pins.add(new PinnedScore(
                    compound.getString("objective"),
                    compound.getString("player")
            ));
        });
    }

    public static void savePins(int serverHash, List<PinnedScore> pins) {
        try {
            NbtCompound compound = NbtIo.read(new File(ClientDB.client.runDirectory, "server_pins.dat"));
            if (compound == null) compound = new NbtCompound();

            NbtList list = toNbt(pins);

            compound.put("" + serverHash, list);

            File file = File.createTempFile("server_pins", ".dat", ClientDB.client.runDirectory);
            NbtIo.write(compound, file);
            File file2 = new File(ClientDB.client.runDirectory, "server_pins.dat_old");
            File file3 = new File(ClientDB.client.runDirectory, "server_pins.dat");
            Util.backupAndReplace(file3, file, file2);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to save pins");
        }
    }

    public static void loadPins(int serverHash, List<PinnedScore> pins) {
        try {
            NbtCompound compound = NbtIo.read(new File(ClientDB.client.runDirectory, "server_pins.dat"));

            if (compound == null) {
                System.out.println("Failed to load servers pins");
                return;
            }

            fromNbt(compound.getList("" + serverHash, 10), pins);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to load servers pins");
        }
    }
}
