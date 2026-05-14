package com.vidyalaya.domain.repository;

import com.vidyalaya.domain.FeePayment;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeePaymentRepository extends JpaRepository<FeePayment, UUID> {
    List<FeePayment> findByStudentIdOrderByCreatedAtDesc(UUID studentId);

    @Query("select coalesce(sum(f.amount),0) from FeePayment f where lower(f.status) = 'paid'")
    BigDecimal sumPaid();

    @Query(
            "select f from FeePayment f where lower(f.status) = 'paid' and f.paidAt is not null and f.paidAt >= :start and f.paidAt < :end")
    List<FeePayment> findPaidBetween(@Param("start") Instant start, @Param("end") Instant end);
}
