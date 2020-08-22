package fantasy.teamgenerator.model;

public class PlayerGameweekScore {

    private final String playerKey;
    private final String firstName;
    private final String lastName;
    private final String minutes;
    private final String points;
    private final String round;

    public PlayerGameweekScore(String playerKey, String firstName, String lastName, String minutes, String points, String round) {
        this.playerKey = playerKey;
        this.firstName = firstName;
        this.lastName = lastName;
        this.minutes = minutes;
        this.points = points;
        this.round = round;
    }

    public String getPlayerKey() {
        return playerKey;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getMinutes() {
        return minutes;
    }

    public String getPoints() {
        return points;
    }

    public String getRound() {
        return round;
    }
}
