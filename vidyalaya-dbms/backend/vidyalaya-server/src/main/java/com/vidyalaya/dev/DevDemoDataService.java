package com.vidyalaya.dev;

import com.vidyalaya.domain.AppUser;
import com.vidyalaya.domain.Event;
import com.vidyalaya.domain.Exam;
import com.vidyalaya.domain.ExamResult;
import com.vidyalaya.domain.Expense;
import com.vidyalaya.domain.FeePayment;
import com.vidyalaya.domain.FeeStructure;
import com.vidyalaya.domain.Notice;
import com.vidyalaya.domain.Parent;
import com.vidyalaya.domain.SalaryPayment;
import com.vidyalaya.domain.SchoolClass;
import com.vidyalaya.domain.Student;
import com.vidyalaya.domain.Subject;
import com.vidyalaya.domain.Teacher;
import com.vidyalaya.domain.TransportRoute;
import com.vidyalaya.domain.repository.EventRepository;
import com.vidyalaya.domain.repository.ExamRepository;
import com.vidyalaya.domain.repository.ExamResultRepository;
import com.vidyalaya.domain.repository.ExpenseRepository;
import com.vidyalaya.domain.repository.FeePaymentRepository;
import com.vidyalaya.domain.repository.FeeStructureRepository;
import com.vidyalaya.domain.repository.NoticeRepository;
import com.vidyalaya.domain.repository.AppUserRepository;
import com.vidyalaya.domain.repository.ParentRepository;
import com.vidyalaya.domain.repository.SalaryPaymentRepository;
import com.vidyalaya.domain.repository.SchoolClassRepository;
import com.vidyalaya.domain.repository.StudentRepository;
import com.vidyalaya.domain.repository.SubjectRepository;
import com.vidyalaya.domain.repository.TeacherRepository;
import com.vidyalaya.domain.repository.TransportRouteRepository;
import com.vidyalaya.tenant.TenantContext;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("dev")
public class DevDemoDataService {

    private static final Logger log = LoggerFactory.getLogger(DevDemoDataService.class);

    private final SchoolClassRepository schoolClassRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final FeeStructureRepository feeStructureRepository;
    private final FeePaymentRepository feePaymentRepository;
    private final SalaryPaymentRepository salaryPaymentRepository;
    private final ExpenseRepository expenseRepository;
    private final ExamRepository examRepository;
    private final ExamResultRepository examResultRepository;
    private final TransportRouteRepository transportRouteRepository;
    private final NoticeRepository noticeRepository;
    private final EventRepository eventRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public DevDemoDataService(
            SchoolClassRepository schoolClassRepository,
            SubjectRepository subjectRepository,
            TeacherRepository teacherRepository,
            StudentRepository studentRepository,
            ParentRepository parentRepository,
            FeeStructureRepository feeStructureRepository,
            FeePaymentRepository feePaymentRepository,
            SalaryPaymentRepository salaryPaymentRepository,
            ExpenseRepository expenseRepository,
            ExamRepository examRepository,
            ExamResultRepository examResultRepository,
            TransportRouteRepository transportRouteRepository,
            NoticeRepository noticeRepository,
            EventRepository eventRepository,
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder) {
        this.schoolClassRepository = schoolClassRepository;
        this.subjectRepository = subjectRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.parentRepository = parentRepository;
        this.feeStructureRepository = feeStructureRepository;
        this.feePaymentRepository = feePaymentRepository;
        this.salaryPaymentRepository = salaryPaymentRepository;
        this.expenseRepository = expenseRepository;
        this.examRepository = examRepository;
        this.examResultRepository = examResultRepository;
        this.transportRouteRepository = transportRouteRepository;
        this.noticeRepository = noticeRepository;
        this.eventRepository = eventRepository;
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void seedIfNeeded() {
        TenantContext.require();
        if (studentRepository.count() > 0) {
            ensureDemoAuthUsers();
            if (feeStructureRepository.count() == 0) {
                log.info("Replenishing finance demo data for tenant {}", TenantContext.get());
                replenishFinanceDemoData();
            }
            return;
        }
        log.info("Seeding demo dataset for tenant {}", TenantContext.get());

        String[] gradeNames = {"Grade VI", "Grade VII", "Grade VIII", "Grade IX", "Grade X"};
        List<SchoolClass> classes = new ArrayList<>();
        for (String n : gradeNames) {
            SchoolClass c = new SchoolClass();
            c.setName(n);
            classes.add(schoolClassRepository.save(c));
        }

        String[] subjectNames = {"Mathematics", "Science", "English", "Hindi", "Social Studies", "Computer", "Art", "PE"};
        List<Subject> subjects = new ArrayList<>();
        for (String n : subjectNames) {
            Subject s = new Subject();
            s.setName(n);
            subjects.add(subjectRepository.save(s));
        }

        List<Teacher> teachers = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            Teacher t = new Teacher();
            t.setFullName("Demo Teacher " + i);
            t.setEmail("teacher" + i + "@demo.school");
            t.setPhone("900000" + String.format("%04d", i));
            t.setGender(i % 2 == 0 ? "Female" : "Male");
            t.setDateOfBirth(LocalDate.of(1985, 1, 1).plusDays(i * 17L));
            t.setAddress("Demo Nagar, Block " + (i % 5 + 1));
            t.setQualification(i % 3 == 0 ? "M.Ed Mathematics" : "B.Ed Integrated Science");
            t.setExperienceSummary(
                    (i + 2) + " years classroom teaching; led remedial batches; CBSE workshop facilitator.");
            t.setJoiningDate(LocalDate.of(2020, 6, 1).plusMonths(i));
            t.setSalaryAmount(BigDecimal.valueOf(35000 + i * 500));
            teachers.add(teacherRepository.save(t));
        }

        String[][] givenNames = {
            {"Jason", "Lee", "Black"},
            {"Ananya", "R", "Sharma"},
            {"Vihaan", "K", "Patel"},
            {"Diya", "S", "Iyer"},
            {"Arjun", "M", "Nair"},
            {"Meera", "P", "Reddy"},
            {"Rohan", "T", "Singh"},
            {"Kavya", "L", "Menon"}
        };
        String[] fatherJobs = {"Banker", "Engineer", "Physician", "Architect", "Civil servant", "Entrepreneur"};
        String[] motherJobs = {"Teacher", "Doctor", "Designer", "Nurse", "Scientist", "Consultant"};

        List<Student> students = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            SchoolClass sc = classes.get(i % classes.size());
            Student s = new Student();
            String[] tri = givenNames[i % givenNames.length];
            s.setFirstName(tri[0]);
            s.setMiddleName(tri[1]);
            s.setLastName(tri[2] + " " + i);
            s.setFullName(tri[0] + " " + tri[1] + " " + tri[2]);
            s.setEmail("student" + i + "@demo.school");
            s.setPhone("910000" + String.format("%04d", i));
            s.setGender(i % 2 == 0 ? "Female" : "Male");
            s.setFatherName("Alex " + tri[2]);
            s.setMotherName("Jessica " + tri[2]);
            s.setFatherOccupation(fatherJobs[i % fatherJobs.length]);
            s.setMotherOccupation(motherJobs[i % motherJobs.length]);
            s.setDateOfBirth(LocalDate.now().minusYears(8 + (i % 6)).minusDays(i));
            s.setReligion("Christian");
            s.setCaste("General");
            s.setAddress("House " + (i % 40 + 1) + ", Demo Avenue, Bengaluru");
            s.setClassName(sc.getName());
            s.setSection(String.valueOf((char) ('A' + (i % 3))));
            s.setAdmissionDate(LocalDate.of(2023, 4, 1).plusDays(i % 200));
            s.setAboutStudent(
                    "Enrolled in " + sc.getName() + ". Enjoys STEM clubs and inter-school quizzes. "
                            + "Attendance consistently strong.");
            students.add(studentRepository.save(s));
        }

        List<Parent> parents = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            Parent p = new Parent();
            p.setFullName("Demo Parent " + i);
            p.setEmail("parent" + i + "@demo.school");
            p.setPhone("920000" + String.format("%04d", i));
            p.setAddress("House " + i + ", Guardian Street, Bengaluru");
            p.setOccupation(i % 2 == 0 ? "Operations manager" : "Finance analyst");
            p.setEmployer("Demo Industries Pvt Ltd");
            p.setEducationSummary("MBA (Finance); B.Com from state university; school PTA volunteer since 2021.");
            parents.add(parentRepository.save(p));
        }
        for (int i = 0; i < parents.size(); i++) {
            Parent p = parents.get(i);
            int s1 = (i * 2) % students.size();
            int s2 = (i * 2 + 1) % students.size();
            p.getChildren().add(students.get(s1));
            if (s2 != s1) {
                p.getChildren().add(students.get(s2));
            }
            parentRepository.save(p);
        }

        seedFinanceSalaryExpenses(students, teachers);

        List<Exam> exams = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            Exam ex = new Exam();
            ex.setName("Term assessment " + (i + 1));
            ex.setExamType(i % 2 == 0 ? "UNIT_TEST" : "TERM");
            ex.setExamDate(LocalDate.now().plusWeeks(i - 6));
            ex.setSchoolClass(classes.get(i % classes.size()));
            ex.setSubject(subjects.get(i % subjects.size()));
            exams.add(examRepository.save(ex));
        }

        for (int i = 0; i < 60; i++) {
            ExamResult er = new ExamResult();
            er.setExam(exams.get(i % exams.size()));
            er.setStudent(students.get(i % students.size()));
            er.setSubject(subjects.get(i % subjects.size()));
            er.setGrade(i % 4 == 0 ? "A+" : "A");
            er.setPercentage(BigDecimal.valueOf(72 + (i % 28)));
            er.setStatus(i % 5 == 0 ? "ACTIVE" : "COMPLETED");
            examResultRepository.save(er);
        }

        String[] routes = {"North loop", "South loop", "East express", "West suburban", "City centre", "Hillside", "Lake road", "Airport link"};
        for (String r : routes) {
            TransportRoute tr = new TransportRoute();
            tr.setName(r);
            tr.setDescription("Scheduled demo route — " + r);
            transportRouteRepository.save(tr);
        }

        for (int i = 1; i <= 10; i++) {
            Notice n = new Notice();
            n.setTitle("School notice #" + i);
            n.setBody("This is auto-generated demo notice text for the dashboard and notice board (" + i + ").");
            noticeRepository.save(n);
        }

        for (int i = 0; i < 8; i++) {
            Event ev = new Event();
            ev.setTitle("Demo event " + (i + 1));
            Instant start = Instant.now().plusSeconds((i - 3) * 86400L);
            ev.setStartTime(start);
            ev.setEndTime(start.plusSeconds(7200));
            ev.setEventType(i % 2 == 0 ? "MEETING" : "HOLIDAY");
            eventRepository.save(ev);
        }

        log.info(
                "Demo seed complete: {} students, {} teachers, {} parents",
                studentRepository.count(),
                teacherRepository.count(),
                parentRepository.count());
        ensureDemoAuthUsers();
    }

    private void replenishFinanceDemoData() {
        List<Student> students = studentRepository.findAll();
        List<Teacher> teachers = teacherRepository.findAll();
        if (students.isEmpty()) {
            return;
        }
        seedFinanceSalaryExpenses(students, teachers);
        log.info(
                "Finance demo replenished: {} fee structures, {} fee payments",
                feeStructureRepository.count(),
                feePaymentRepository.count());
    }

    private void seedFinanceSalaryExpenses(List<Student> students, List<Teacher> teachers) {
        String[] feeNames = {"Tuition", "Lab fee", "Sports", "Library", "Transport fee", "Annual charges"};
        List<FeeStructure> fees = new ArrayList<>();
        for (int i = 0; i < feeNames.length; i++) {
            FeeStructure f = new FeeStructure();
            f.setName(feeNames[i]);
            f.setAmount(BigDecimal.valueOf(3000 + i * 750L));
            f.setDueDate(LocalDate.now().plusMonths(1).withDayOfMonth(10));
            fees.add(feeStructureRepository.save(f));
        }

        YearMonth ymBase = YearMonth.now().minusMonths(11);
        for (int si = 0; si < students.size(); si++) {
            Student st = students.get(si);
            for (int j = 0; j < 2; j++) {
                FeeStructure fs = fees.get((si + j) % fees.size());
                FeePayment fp = new FeePayment();
                fp.setStudent(st);
                fp.setFeeStructure(fs);
                fp.setAmount(fs.getAmount());
                boolean paid = (si + j) % 3 != 0;
                fp.setStatus(paid ? "PAID" : "DUE");
                if (paid) {
                    YearMonth ym = ymBase.plusMonths((si + j) % 12);
                    fp.setPaidAt(ym.atDay(10 + (j * 3)).atTime(11, 0).toInstant(ZoneOffset.UTC));
                }
                feePaymentRepository.save(fp);
            }
        }

        for (int ti = 0; ti < teachers.size(); ti++) {
            Teacher t = teachers.get(ti);
            for (int m = 0; m < 4; m++) {
                YearMonth ym = ymBase.plusMonths((ti + m * 3) % 12);
                SalaryPayment sp = new SalaryPayment();
                sp.setTeacher(t);
                sp.setAmount(t.getSalaryAmount() != null ? t.getSalaryAmount() : BigDecimal.valueOf(40000));
                sp.setMonthYear(ym.toString());
                sp.setStatus("PAID");
                sp.setPaidAt(ym.atEndOfMonth().atTime(9, 0).toInstant(ZoneOffset.UTC));
                salaryPaymentRepository.save(sp);
            }
        }

        String[] cats = {"Utilities", "Supplies", "Maintenance", "IT", "Catering"};
        for (int i = 0; i < 15; i++) {
            YearMonth ym = ymBase.plusMonths(i % 12);
            Expense e = new Expense();
            e.setCategory(cats[i % cats.length]);
            e.setAmount(BigDecimal.valueOf(2000 + i * 350L));
            e.setDescription("Demo expense #" + (i + 1));
            e.setExpenseDate(ym.atDay(5 + (i % 10)));
            expenseRepository.save(e);
        }
    }

    private void ensureDemoAuthUsers() {
        Teacher anyTeacher = teacherRepository.findAll().stream().findFirst().orElse(null);
        Parent anyParent = parentRepository.findAll().stream().findFirst().orElse(null);
        Student anyStudent = studentRepository.findAll().stream().findFirst().orElse(null);

        createUserIfMissing("demoadmin", "ROLE_ADMIN", "DemoAdmin123!", null, null, null);
        createUserIfMissing("demouser", "ROLE_TEACHER", "DemoUser123!", anyTeacher, anyParent, anyStudent);
        createUserIfMissing("demoteacher", "ROLE_TEACHER", "DemoTeacher123!", anyTeacher, null, null);
        createUserIfMissing("demoparent", "ROLE_PARENT", "DemoParent123!", null, anyParent, null);
        createUserIfMissing("demostudent", "ROLE_STUDENT", "DemoStudent123!", null, null, anyStudent);
    }

    private void createUserIfMissing(
            String username,
            String role,
            String plainPassword,
            Teacher teacher,
            Parent parent,
            Student student) {
        if (appUserRepository.findByUsernameIgnoreCase(username).isPresent()) {
            return;
        }
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setRole(role);
        user.setPasswordHash(passwordEncoder.encode(plainPassword));
        user.setActive(true);
        user.setTeacher(teacher);
        user.setParent(parent);
        user.setStudent(student);
        appUserRepository.save(user);
        log.info("Seeded demo auth user '{}' with role {}", username, role);
    }
}
