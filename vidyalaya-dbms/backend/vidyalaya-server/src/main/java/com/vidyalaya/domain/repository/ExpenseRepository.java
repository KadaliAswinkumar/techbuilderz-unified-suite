package com.vidyalaya.domain.repository;

import com.vidyalaya.domain.Expense;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    @Query("select coalesce(sum(e.amount),0) from Expense e")
    BigDecimal sumAmount();

    List<Expense> findByExpenseDateBetween(LocalDate fromInclusive, LocalDate toInclusive);
}
