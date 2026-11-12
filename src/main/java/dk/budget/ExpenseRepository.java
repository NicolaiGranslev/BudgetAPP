package dk.budget;

import org.springframework.data.jpa.repository.JpaRepository;

// Spring writes all the SQL for us (save, findAll, deleteById ...)
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
}
