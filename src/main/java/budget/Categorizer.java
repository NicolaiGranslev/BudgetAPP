package budget;

import java.util.LinkedHashMap;
import java.util.Map;

// Finds a category by looking for known words in the text could have more words to look for
public class Categorizer {

    private static final Map<String, String[]> KEYWORDS = new LinkedHashMap<>();

    static {
        KEYWORDS.put("Housing", new String[]{"rent", "husleje", "varme", "vand"});
        KEYWORDS.put("Insurance", new String[]{"insurance", "forsikring", "tryg", "alka"});
        KEYWORDS.put("Food", new String[]{"netto", "rema", "føtex", "lidl", "bilka", "coop", "meny", "aldi"});
        KEYWORDS.put("Transport", new String[]{"dsb", "rejsekort", "benzin", "circle k", "q8", "shell"});
        KEYWORDS.put("Subscriptions", new String[]{"netflix", "spotify", "hbo", "disney", "telefon", "fitness"});
        KEYWORDS.put("Fun", new String[]{"restaurant", "cafe", "biograf", "cinema", "mcdonald", "pizza"});
    }

    // "Netto" -> "Food"
    public static String categorize(String text) {
        String word = findKeyword(text);
        for (String category : KEYWORDS.keySet()) {
            for (String w : KEYWORDS.get(category)) {
                if (w.equals(word)) return category;
            }
        }
        return "Other";
    }

    // Returns the first known word in the text, ex "netto", or null
    public static String findKeyword(String text) {
        String lower = text.toLowerCase();
        for (String[] words : KEYWORDS.values()) {
            for (String word : words) {
                if (lower.contains(word)) return word;
            }
        }
        return null;
    }
}
