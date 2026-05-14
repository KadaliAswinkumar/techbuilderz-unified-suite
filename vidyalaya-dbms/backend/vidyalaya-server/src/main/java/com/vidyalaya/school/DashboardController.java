package com.vidyalaya.school;

import com.vidyalaya.domain.Expense;
import com.vidyalaya.domain.FeePayment;
import com.vidyalaya.domain.SalaryPayment;
import com.vidyalaya.domain.repository.EventRepository;
import com.vidyalaya.domain.repository.ExpenseRepository;
import com.vidyalaya.domain.repository.FeePaymentRepository;
import com.vidyalaya.domain.repository.NoticeRepository;
import com.vidyalaya.domain.repository.ParentRepository;
import com.vidyalaya.domain.repository.SalaryPaymentRepository;
import com.vidyalaya.domain.repository.StudentRepository;
import com.vidyalaya.domain.repository.TeacherRepository;
import com.vidyalaya.security.SecurityUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ParentRepository parentRepository;
    private final FeePaymentRepository feePaymentRepository;
    private final SalaryPaymentRepository salaryPaymentRepository;
    private final ExpenseRepository expenseRepository;
    private final NoticeRepository noticeRepository;
    private final EventRepository eventRepository;

    public DashboardController(
            StudentRepository studentRepository,
            TeacherRepository teacherRepository,
            ParentRepository parentRepository,
            FeePaymentRepository feePaymentRepository,
            SalaryPaymentRepository salaryPaymentRepository,
            ExpenseRepository expenseRepository,
            NoticeRepository noticeRepository,
            EventRepository eventRepository) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.parentRepository = parentRepository;
        this.feePaymentRepository = feePaymentRepository;
        this.salaryPaymentRepository = salaryPaymentRepository;
        this.expenseRepository = expenseRepository;
        this.noticeRepository = noticeRepository;
        this.eventRepository = eventRepository;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Transactional(readOnly = true)
    public Map<String, Object> admin(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate chartTo = to != null ? to : today;
        LocalDate chartFrom = from != null ? from : YearMonth.from(chartTo).minusMonths(11).atDay(1);
        if (chartFrom.isAfter(chartTo)) {
            LocalDate tmp = chartFrom;
            chartFrom = chartTo;
            chartTo = tmp;
        }

        long total = studentRepository.count();
        long male = studentRepository.countMaleApprox();
        long female = studentRepository.countFemaleApprox();
        BigDecimal feeIncome = feePaymentRepository.sumPaid();
        BigDecimal salaries = salaryPaymentRepository.sumPaid();
        BigDecimal expenses = expenseRepository.sumAmount();
        BigDecimal net = feeIncome.subtract(salaries).subtract(expenses);
        double malePct =
                total == 0
                        ? 0
                        : BigDecimal.valueOf(male)
                                .multiply(BigDecimal.valueOf(100))
                                .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP)
                                .doubleValue();
        double femalePct =
                total == 0
                        ? 0
                        : BigDecimal.valueOf(female)
                                .multiply(BigDecimal.valueOf(100))
                                .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP)
                                .doubleValue();

        List<Map<String, Object>> monthly = buildMonthlySeries(chartFrom, chartTo);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("students", studentRepository.count());
        body.put("teachers", teacherRepository.count());
        body.put("parents", parentRepository.count());
        body.put("feeIncome", feeIncome);
        body.put("salariesPaid", salaries);
        body.put("expensesTotal", expenses);
        body.put("net", net);
        body.put("genderBreakdown", Map.of("malePercent", malePct, "femalePercent", femalePct));
        body.put("monthlySeries", monthly);
        body.put("chartFrom", chartFrom.toString());
        body.put("chartTo", chartTo.toString());
        body.put("notices", noticeRepository.findAll().stream().limit(10).toList());
        body.put(
                "events",
                eventRepository.findByStartTimeBetweenOrderByStartTimeAsc(
                        Instant.now().minus(7, ChronoUnit.DAYS), Instant.now().plus(60, ChronoUnit.DAYS)));
        return body;
    }

    private List<Map<String, Object>> buildMonthlySeries(LocalDate chartFrom, LocalDate chartTo) {
        YearMonth startYm = YearMonth.from(chartFrom);
        YearMonth endYm = YearMonth.from(chartTo);
        List<Map<String, Object>> out = new ArrayList<>();
        for (YearMonth ym = startYm; !ym.isAfter(endYm); ym = ym.plusMonths(1)) {
            Instant ms = ym.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant me = ym.plusMonths(1).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            LocalDate dStart = ym.atDay(1);
            LocalDate dEnd = ym.atEndOfMonth();
            if (dStart.isBefore(chartFrom)) {
                dStart = chartFrom;
            }
            if (dEnd.isAfter(chartTo)) {
                dEnd = chartTo;
            }

            BigDecimal earnings =
                    feePaymentRepository.findPaidBetween(ms, me).stream()
                            .map(FeePayment::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal expenseMonth =
                    expenseRepository.findByExpenseDateBetween(dStart, dEnd).stream()
                            .map(Expense::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal salaryMonth =
                    salaryPaymentRepository.findPaidBetween(ms, me).stream()
                            .map(SalaryPayment::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal expenseBar = expenseMonth.add(salaryMonth);
            String label =
                    ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.US) + " " + ym.getYear();
            out.add(
                    Map.of(
                            "month",
                            label,
                            "earnings",
                            earnings,
                            "expenses",
                            expenseBar));
        }
        return out;
    }

    @GetMapping("/teacher")
    @PreAuthorize("hasRole('TEACHER')")
    public Map<String, String> teacher() {
        return Map.of("message", "Teacher dashboard metrics — extend as needed");
    }

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public Map<String, String> student() {
        return Map.of("userId", SecurityUtils.currentUserId().toString());
    }

    @GetMapping("/parent")
    @PreAuthorize("hasRole('PARENT')")
    public Map<String, String> parent() {
        return Map.of("userId", SecurityUtils.currentUserId().toString());
    }
}
