import java.util.ArrayList;
public class Database {
    public int guessNumber;
    public String[] colors;
    public int pegNumber;
    public ArrayList<String> colorSet;
    public ArrayList<String> resultSet;
    public String ans;
    public Database(String ans) {
        this.guessNumber = GameConfiguration.guessNumber;
        this.colors = GameConfiguration.colors;
        this.pegNumber = GameConfiguration.pegNumber;
        this.colorSet = new ArrayList<>();
        this.resultSet = new ArrayList<>();
        this.ans = ans;
    }
}
