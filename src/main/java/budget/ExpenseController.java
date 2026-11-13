package budget;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;

// All the endpoints the JavaScript talks to
@RestController
@RequestMapping("/api")
public class ExpenseController {

    private final ExpenseRepository repo;
    private final OcrService ocr;
    private final AiService ai;

    public ExpenseController(ExpenseRepository repo, OcrService ocr, AiService ai) {
        this.repo = repo;
        this.ocr = ocr;
        this.ai = ai;
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

    // Receipt picture -> OCR API -> text -> store, total, category and date
    @PostMapping("/receipt")
    public Map<String, Object> receipt(@RequestParam("image") MultipartFile image) throws Exception {
        String text = ocr.readText(image);
        String store = Categorizer.findKeyword(text);
        LocalDate date = ocr.findDate(text);

        Map<String, Object> result = new HashMap<>();
        result.put("description", store == null ? "Receipt" : store.toUpperCase());
        result.put("amount", ocr.findTotal(text));
        result.put("category", Categorizer.categorize(text));
        result.put("date", date == null ? null : date.toString());
        return result;
    }

    // Spending per category, estimate for next month and advice
    @GetMapping("/summary")
    public Map<String, Object> summary(@RequestParam double income) {
        Map<String, Double> perCategory = new TreeMap<>();
        double fixed = 0;
        double variable = 0;
        Set<String> months = new HashSet<>();

        for (Expense e : repo.findAll()) {
            perCategory.merge(e.getCategory(), e.getAmount(), Double::sum);
            if (e.isFixed()) {
                fixed += e.getAmount();
            } else {
                variable += e.getAmount();
                months.add(e.getDate().getYear() + "-" + e.getDate().getMonthValue());
            }
        }

        // Estimate = fixed costs + average variable spending per month
        double estimate = fixed + (months.isEmpty() ? 0 : variable / months.size());

        Map<String, Object> result = new HashMap<>();
        result.put("perCategory", perCategory);
        result.put("estimate", Math.round(estimate));
        result.put("advice", makeAdvice(income, estimate, perCategory));
        try {
            if (income > 0) {
                List<String> aiAdvice = ai.advice(income, estimate, perCategory);
                if (!aiAdvice.isEmpty()) result.put("advice", aiAdvice); // AI advice
            }
        } catch (Exception e) {
            System.out.println("AI error: " + e.getMessage()); // if the AI fails, we keep the rule-based advice
        }
        return result;
    }

    private List<String> makeAdvice(double income, double estimate, Map<String, Double> perCategory) {
        List<String> advice = new ArrayList<>();
        if (income <= 0) {
            advice.add("Enter this month's income to get advice.");
            return advice;
        }

        double leftOver = income - estimate;
        if (leftOver < 0) {
            advice.add("Warning: you will spend " + Math.round(-leftOver) + " kr more than you earn.");
        } else if (leftOver < income * 0.1) {
            advice.add("You save less than 10%. Try to save " + Math.round(income * 0.1) + " kr.");
        } else {
            advice.add("Good job! You will have " + Math.round(leftOver) + " kr left over.");
        }

        if (perCategory.getOrDefault("Housing", 0.0) > income * 0.4) advice.add("Housing is over 40% of your income.");
        if (perCategory.getOrDefault("Food", 0.0) > income * 0.2) advice.add("Food is over 20% of your income. Plan your meals.");
        if (perCategory.getOrDefault("Fun", 0.0) > income * 0.15) advice.add("Fun is over 15% of your income. Set a limit.");
        if (perCategory.getOrDefault("Subscriptions", 0.0) > 500) advice.add("Subscriptions cost over 500 kr. Cancel some.");
        return advice;
    }
}