package com.vidyalaya.domain.repository;

import com.vidyalaya.domain.SalaryPayment;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SalaryPaymentRepository extends JpaRepository<SalaryPayment, UUID> {
    List<SalaryPayment> findByTeacherIdOrderByCreatedAtDesc(UUID teacherId);

    @Query("select coalesce(sum(s.amount),0) from SalaryPayment s where lower(s.status) = 'paid'")
    BigDecimal sumPaid();

    @Query(
            "select s from SalaryPayment s where lower(s.status) = 'paid' and s.paidAt is not null and s.paidAt >= :start and s.paidAt < :end")
    List<SalaryPayment> findPaidBetween(@Param("start") Instant start, @Param("end") Instant end);
}
