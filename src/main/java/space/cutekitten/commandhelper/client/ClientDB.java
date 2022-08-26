package space.cutekitten.commandhelper.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.ScoreboardPlayerScore;

import java.util.ArrayList;
import java.util.List;

public class ClientDB {
    public static MinecraftClient client = MinecraftClient.getInstance();
    public static List<ScoreboardPlayerScore> scores = new ArrayList<>();
    public static List<ScoreboardPlayerScore> showScores = new ArrayList<>();
    public static List<ScoreboardPlayerScore> pinnedScores = new ArrayList<>();
    public static String currentSearch = "";
    public static boolean customScoreboardActive = true;
}
