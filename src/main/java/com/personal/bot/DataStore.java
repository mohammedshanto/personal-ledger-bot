import com.google.gson.Gson;
import java.io.*;

public class DataStore {
    private static final String FILE = "data.json";
    private static final Gson gson = new Gson();

    public static BotData load() {
        try {
            return gson.fromJson(new FileReader(FILE), BotData.class);
        } catch (Exception e) {
            return new BotData();
        }
    }

    public static void save(BotData data) {
        try (FileWriter writer = new FileWriter(FILE)) {
            gson.toJson(data, writer);
        } catch (Exception ignored) {}
    }
}
