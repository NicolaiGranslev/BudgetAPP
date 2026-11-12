package dk.budget;

import jakarta.persistence.*;
import java.time.LocalDate;

// One expense = one row in the database
@Entity
public class Expense {
    @Id
    @GeneratedValue
    private Long id;
    private String description;
    private double amount;
    private String category;
    private LocalDate date;
    private boolean fixed; // true = comes every month (rent, insurance...)

    public Long getId() { return id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public boolean isFixed() { return fixed; }
    public void setFixed(boolean fixed) { this.fixed = fixed; }
}
