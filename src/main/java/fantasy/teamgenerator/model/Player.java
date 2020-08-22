package fantasy.teamgenerator.model;

public class Player implements Comparable<Player> {

    private final String firstName;
    private final String lastName;
    private final Team team;

    public Player(String firstName, String lastName, Team team) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.team = team;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Team getTeam() {
        return team;
    }

    @Override
    public String toString() {
        return getLastName() + " " + getFirstName();
    }

    @Override
    public int compareTo(Player o) {
        int lastNameCompare = this.lastName.compareTo(o.lastName);
        return lastNameCompare == 0 ? this.firstName.compareTo(o.firstName) : lastNameCompare;
    }
}
