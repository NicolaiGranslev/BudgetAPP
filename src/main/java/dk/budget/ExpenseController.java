package dk.budget;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;

// All endpoints the JavaScript talks to
@RestController
@RequestMapping("/api")
public class ExpenseController {

    private final ExpenseRepository repo;
    private final OcrService ocr;

    public ExpenseController(ExpenseRepository repo, OcrService ocr) {
        this.repo = repo;
        this.ocr = ocr;
    }

    @GetMapping("/expenses")
    public List<Expense> all() {
        return repo.findAll();
    }

    @PostMapping("/expenses")
    public Expense add(@RequestBody Expense expense) {
        if (expense.getCategory() == null || expense.getCategory().isBlank()) {
            expense.setCategory(Categorizer.categorize(expense.getDescription()));
        }
        if (expense.getDate() == null) {
            expense.setDate(LocalDate.now());
        }
        return repo.save(expense);
    }

    @DeleteMapping("/expenses/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }

    // Receipt picture -> API -> text -> suggested expense (user confirms in the form)
    @PostMapping("/receipt")
    public Map<String, Object> receipt(@RequestParam("image") MultipartFile image) throws Exception {
        String text = ocr.readText(image);
        String firstLine = text.isBlank() ? "Receipt" : text.lines().findFirst().get().trim();

        Map<String, Object> result = new HashMap<>();
        result.put("description", firstLine);
        result.put("amount", ocr.findTotal(text));
        result.put("category", Categorizer.categorize(text));
        return result;
    }

    // Totals per category, estimate for next month and advice
    @GetMapping("/summary")
    public Map<String, Object> summary(@RequestParam double income) {
        List<Expense> expenses = repo.findAll();

        Map<String, Double> perCategory = new TreeMap<>();
        double fixed = 0;
        double variable = 0;
        Set<String> months = new HashSet<>();

        for (Expense e : expenses) {
            perCategory.merge(e.getCategory(), e.getAmount(), Double::sum);
            if (e.isFixed()) {
                fixed += e.getAmount();
            } else {
                variable += e.getAmount();
                months.add(e.getDate().getYear() + "-" + e.getDate().getMonthValue());
            }
        }

        // Estimate = fixed costs + average variable spending per month
        double avgVariable = months.isEmpty() ? 0 : variable / months.size();
        double estimate = fixed + avgVariable;

        Map<String, Object> result = new HashMap<>();
        result.put("perCategory", perCategory);
        result.put("estimate", Math.round(estimate));
        result.put("advice", makeAdvice(income, estimate, perCategory, months.size()));
        return result;
    }

    private List<String> makeAdvice(double income, double estimate, Map<String, Double> perCategory, int months) {
        List<String> advice = new ArrayList<>();
        if (income <= 0) {
            advice.add("Enter your monthly income to get advice.");
            return advice;
        }
        double leftOver = income - estimate;
        if (leftOver < 0) {
            advice.add("Warning: you are expected to spend " + Math.round(-leftOver) + " kr more than you earn next month.");
        } else if (leftOver < income * 0.1) {
            advice.add("You save less than 10% of your income. Try to save at least " + Math.round(income * 0.1) + " kr.");
        } else {
            advice.add("Good job! You are expected to have " + Math.round(leftOver) + " kr left over.");
        }

        int m = Math.max(months, 1);
        double food = perCategory.getOrDefault("Food", 0.0) / m;
        double leisure = perCategory.getOrDefault("Leisure", 0.0) / m;
        double subs = perCategory.getOrDefault("Subscriptions", 0.0);
        double housing = perCategory.getOrDefault("Housing", 0.0);

        if (housing > income * 0.4) advice.add("Housing takes over 40% of your income. That is high.");
        if (food > income * 0.2) advice.add("Food is over 20% of your income. Plan meals and buy discount brands.");
        if (leisure > income * 0.15) advice.add("Leisure is over 15% of your income. Set a monthly limit.");
        if (subs > 500) advice.add("Subscriptions cost over 500 kr. Cancel the ones you don't use.");
        return advice;
    }
}
