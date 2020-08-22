package fantasy.teamgenerator;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import fantasy.teamgenerator.json.DataJson;
import fantasy.teamgenerator.json.PlayerJson;
import fantasy.teamgenerator.json.TeamJson;
import fantasy.teamgenerator.model.Player;
import fantasy.teamgenerator.model.PlayerGameweekScore;
import fantasy.teamgenerator.model.Team;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

@SpringBootTest
class TeamGeneratorApplicationTests {

    private static final int FULL_NAME_CSV_INDEX = 0;
    private static final int MINUTES_CSV_INDEX = 13;
    private static final int ROUND_CSV_INDEX = 19;
    private static final int POINTS_CSV_INDEX = 25;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createMergedFiles() throws IOException, URISyntaxException {
        generateMergedFile("2019-20");
        generateMergedFile("2018-19");
        generateMergedFile("2017-18");
        generateMergedFile("2016-17");
    }

    private void generateMergedFile(String season) throws IOException, URISyntaxException {
        URL resource = this.getClass().getResource("/data/" + season + "/players/Aaron_Connolly_534");
        Files.walk(Paths.get(resource.toURI()))
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    List<String[]> strings = readFromCSV(file.toString());
                });
    }

    @Test
    void contextLoads_fromGws() throws IOException {

        DataJson apiData = getApiData("2020-2021");

        List<TeamJson> allTeamsApi = apiData.getTeams();

        List<PlayerJson> playersApi = apiData.getPlayers();
        List<Player> players = playersApi.stream().map(
                playerJson -> new Player(playerJson.getFirst_name(), playerJson.getSecond_name(), new Team(playerJson.getTeamJsonObject().getName()))
        ).collect(Collectors.toList());

        List<String[]> playerGameweekScoresCsv = getAllGameweekScoresFromCsv();
        List<PlayerGameweekScore> playerGameweekScores = convertCsvToPlayerGameweekScore(playerGameweekScoresCsv);

        TreeMap<Team, TreeMap<Player, List<PlayerGameweekScore>>> teamToPlayersToScores = new TreeMap<>();
        allTeamsApi.forEach(teamApi -> teamToPlayersToScores.put(new Team(teamApi.getName()), new TreeMap<>()));

        players.forEach(player -> {
            Team team = player.getTeam();
            TreeMap<Player, List<PlayerGameweekScore>> playerToScores = teamToPlayersToScores.get(team);
            List<PlayerGameweekScore> scores = playerGameweekScores.stream()
                    .filter(score -> score.getLastName().equals(player.getLastName()) &&
                            score.getFirstName().equals(player.getFirstName())).collect(Collectors.toList());
            playerToScores.computeIfAbsent(player, k -> new ArrayList<>()).addAll(scores);
        });
    }

    private List<String[]> getAllGameweekScoresFromCsv() {
        List<String[]> gameweekScoresCsv2019 = readFromCSV("/merged-gw/merged-gw-2018-2019.csv");
        List<String[]> gameweekScoresCsv2020 = readFromCSV("/merged-gw/merged-gw-2019-2020.csv");
        List<String[]> allGameweekScores = new ArrayList<>();
        allGameweekScores.addAll(gameweekScoresCsv2019);
        allGameweekScores.addAll(gameweekScoresCsv2020);
        return allGameweekScores;
    }

    private List<String[]> readFromCSV(String file) {
        URL resourceAsStream = this.getClass().getResource(file);
        List<String[]> lines;
        try (CSVReader reader = new CSVReader(new FileReader(new File(resourceAsStream.toURI())))) {
            lines = reader.readAll();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        lines.remove(0);
        return lines;
    }

    private List<PlayerGameweekScore> convertCsvToPlayerGameweekScore(List<String[]> gameweekScoresCsv) {
        return gameweekScoresCsv.stream().map(result -> {
            String firstName = result[FULL_NAME_CSV_INDEX].split("_")[0];
            String lastName = result[FULL_NAME_CSV_INDEX].split("_")[1];
            String playerKey = result[FULL_NAME_CSV_INDEX].split("_")[2];
            String minutes = result[MINUTES_CSV_INDEX];
            String round = result[ROUND_CSV_INDEX];
            String points = result[POINTS_CSV_INDEX];
            return new PlayerGameweekScore(playerKey, firstName, lastName, minutes, points, round);
        }).collect(Collectors.toList());
    }

    @Test
    void contextLoads_fromApi() throws IOException {

        DataJson dataJson = getApiData("2020-2021");
        List<PlayerJson> playerJsons = dataJson.getPlayers().stream().filter(a -> a.getAppearances() > 10).sorted(Comparator.comparingDouble(PlayerJson::getValueSeasonPerAppearances).reversed()).collect(Collectors.toList());

        List<PlayerJson> gk = playerJsons.stream().filter(p -> p.getElement_type() == 1).collect(Collectors.toList());
        List<PlayerJson> def = playerJsons.stream().filter(p -> p.getElement_type() == 2).collect(Collectors.toList());
        List<PlayerJson> mid = playerJsons.stream().filter(p -> p.getElement_type() == 3).collect(Collectors.toList());
        List<PlayerJson> fwd = playerJsons.stream().filter(p -> p.getElement_type() == 4).collect(Collectors.toList());

        System.out.println("----------------------");
        System.out.println("Goalkeepers");
        gk.forEach(playerJson -> printPlayer(playerJson));
        System.out.println("----------------------");
        System.out.println("Defenders");
        def.forEach(playerJson -> printPlayer(playerJson));
        System.out.println("----------------------");
        System.out.println("Midfielders");
        mid.forEach(playerJson -> printPlayer(playerJson));
        System.out.println("----------------------");
        System.out.println("Forwards");
        fwd.forEach(playerJson -> printPlayer(playerJson));
        System.out.println("----------------------");
    }

    private void printPlayer(PlayerJson playerJson) {
        System.out.println(format15(playerJson.getTeamJsonObject().name) + " : " + formatLong(playerJson.toString()) + " : " + formatCost(playerJson.getNow_cost()) + " : " + playerJson.getAppearances() + " : " + formatDouble(playerJson.getValueSeasonPerAppearances()) + " : " + playerJson.getAppearanceAvgMinutes());
    }

    private String formatCost(int now_cost) {
        String cost = Integer.toString(now_cost);
        return now_cost / 10 + "." + cost.substring(cost.length() - 1);
    }

    private String format15(String name) {
        return String.format("%15s", name);
    }

    private String formatLong(String name) {
        return String.format("%30s", name);
    }

    private String formatDouble(double number) {
        return String.format("%.3f", number);
    }

    private DataJson getApiData(String season) throws IOException {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        DataJson dataJson = objectMapper.readValue(this.getClass().getResourceAsStream("/api-data/" + season + ".json"), DataJson.class);
        dataJson.setTeamsOnPlayers();
        return dataJson;
    }
}
