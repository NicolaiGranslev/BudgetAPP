package dk.budget;

import java.util.LinkedHashMap;
import java.util.Map;

// Finds a category by looking for keywords in the text
public class Categorizer {

    private static final Map<String, String[]> KEYWORDS = new LinkedHashMap<>();

    static {
        KEYWORDS.put("Housing", new String[]{"rent", "husleje", "el ", "varme", "vand", "andel"});
        KEYWORDS.put("Insurance", new String[]{"insurance", "forsikring", "tryg", "alka", "topdanmark"});
        KEYWORDS.put("Food", new String[]{"netto", "rema", "føtex", "lidl", "bilka", "coop", "meny", "aldi", "food", "mad"});
        KEYWORDS.put("Transport", new String[]{"dsb", "rejsekort", "benzin", "circle k", "q8", "shell", "bus", "train"});
        KEYWORDS.put("Subscriptions", new String[]{"netflix", "spotify", "hbo", "disney", "telefon", "phone", "internet", "fitness"});
        KEYWORDS.put("Leisure", new String[]{"restaurant", "bar", "cafe", "biograf", "cinema", "mcdonald", "burger", "pizza"});
    }

    public static String categorize(String text) {
        String lower = text.toLowerCase();
        for (Map.Entry<String, String[]> entry : KEYWORDS.entrySet()) {
            for (String word : entry.getValue()) {
                if (lower.contains(word)) {
                    return entry.getKey();
                }
            }
        }
        return "Other";
    }
}
