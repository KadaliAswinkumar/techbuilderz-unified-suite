package com.vidyalaya.school;

import com.vidyalaya.domain.Expense;
import com.vidyalaya.domain.FeePayment;
import com.vidyalaya.domain.FeeStructure;
import com.vidyalaya.domain.SalaryPayment;
import com.vidyalaya.domain.repository.ExpenseRepository;
import com.vidyalaya.domain.repository.FeePaymentRepository;
import com.vidyalaya.domain.repository.FeeStructureRepository;
import com.vidyalaya.domain.repository.SalaryPaymentRepository;
import com.vidyalaya.domain.repository.StudentRepository;
import com.vidyalaya.domain.repository.TeacherRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Transactional(readOnly = true)
public class FinanceController {

    private final FeeStructureRepository feeStructureRepository;
    private final FeePaymentRepository feePaymentRepository;
    private final SalaryPaymentRepository salaryPaymentRepository;
    private final ExpenseRepository expenseRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    public FinanceController(
            FeeStructureRepository feeStructureRepository,
            FeePaymentRepository feePaymentRepository,
            SalaryPaymentRepository salaryPaymentRepository,
            ExpenseRepository expenseRepository,
            StudentRepository studentRepository,
            TeacherRepository teacherRepository) {
        this.feeStructureRepository = feeStructureRepository;
        this.feePaymentRepository = feePaymentRepository;
        this.salaryPaymentRepository = salaryPaymentRepository;
        this.expenseRepository = expenseRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
    }

    @GetMapping("/fee-structures")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','SUPER_ADMIN')")
    public List<FeeStructure> feeStructures() {
        return feeStructureRepository.findAll();
    }

    @PostMapping("/fee-structures")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Transactional(readOnly = false)
    public FeeStructure createFeeStructure(@RequestBody FeeStructureReq req) {
        FeeStructure f = new FeeStructure();
        f.setName(req.name());
        f.setAmount(req.amount());
        f.setDueDate(req.dueDate());
        return feeStructureRepository.save(f);
    }

    @PutMapping("/fee-structures/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Transactional(readOnly = false)
    public FeeStructure updateFeeStructure(@PathVariable UUID id, @RequestBody FeeStructureReq req) {
        FeeStructure f = feeStructureRepository.findById(id).orElseThrow();
        f.setName(req.name());
        f.setAmount(req.amount());
        f.setDueDate(req.dueDate());
        return feeStructureRepository.save(f);
    }

    @DeleteMapping("/fee-structures/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Transactional(readOnly = false)
    public void deleteFeeStructure(@PathVariable UUID id) {
        feeStructureRepository.deleteById(id);
    }

    @GetMapping("/fees/student/{studentId}/dues")
    @PreAuthorize("hasAnyRole('ADMIN','PARENT','STUDENT')")
    public List<FeePayment> dues(@PathVariable UUID studentId) {
        return feePaymentRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .filter(fp -> "DUE".equalsIgnoreCase(fp.getStatus()))
                .toList();
    }

    @GetMapping("/fee-payments")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public List<FeePayment> allPayments() {
        return feePaymentRepository.findAll();
    }

    @PostMapping("/fee-payments")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Transactional(readOnly = false)
    public FeePayment recordPayment(@RequestBody FeePayReq req) {
        FeePayment fp = new FeePayment();
        fp.setStudent(studentRepository.getReferenceById(req.studentId()));
        fp.setFeeStructure(feeStructureRepository.getReferenceById(req.feeStructureId()));
        fp.setAmount(req.amount());
        fp.setStatus(req.status());
        if ("PAID".equalsIgnoreCase(req.status())) {
            fp.setPaidAt(Instant.now());
        }
        return feePaymentRepository.save(fp);
    }

    @GetMapping("/salary-payments")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public List<SalaryPayment> salaryPayments() {
        return salaryPaymentRepository.findAll();
    }

    @PostMapping("/salary-payments")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Transactional(readOnly = false)
    public SalaryPayment paySalary(@RequestBody SalaryReq req) {
        SalaryPayment s = new SalaryPayment();
        s.setTeacher(teacherRepository.getReferenceById(req.teacherId()));
        s.setAmount(req.amount());
        s.setMonthYear(req.monthYear());
        s.setStatus(req.status());
        if ("PAID".equalsIgnoreCase(req.status())) {
            s.setPaidAt(Instant.now());
        }
        return salaryPaymentRepository.save(s);
    }

    @GetMapping("/expenses")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public List<Expense> expenses() {
        return expenseRepository.findAll();
    }

    @PostMapping("/expenses")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Transactional(readOnly = false)
    public Expense addExpense(@RequestBody ExpenseReq req) {
        Expense e = new Expense();
        e.setCategory(req.category());
        e.setAmount(req.amount());
        e.setDescription(req.description());
        e.setExpenseDate(req.expenseDate());
        return expenseRepository.save(e);
    }

    public record FeeStructureReq(@NotBlank String name, @NotNull BigDecimal amount, LocalDate dueDate) {}

    public record FeePayReq(
            @NotNull UUID studentId,
            @NotNull UUID feeStructureId,
            @NotNull BigDecimal amount,
            @NotBlank String status) {}

    public record SalaryReq(
            @NotNull UUID teacherId,
            @NotNull BigDecimal amount,
            @NotBlank String monthYear,
            @NotBlank String status) {}

    public record ExpenseReq(
            @NotBlank String category,
            @NotNull BigDecimal amount,
            String description,
            @NotNull LocalDate expenseDate) {}
}
