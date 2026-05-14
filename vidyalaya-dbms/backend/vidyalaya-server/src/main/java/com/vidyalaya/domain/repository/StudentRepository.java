package com.vidyalaya.domain.repository;

import com.vidyalaya.domain.Student;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StudentRepository extends JpaRepository<Student, UUID> {
    long count();

    @Query("select count(s) from Student s where lower(coalesce(s.gender,'')) like 'm%'")
    long countMaleApprox();

    @Query("select count(s) from Student s where lower(coalesce(s.gender,'')) like 'f%'")
    long countFemaleApprox();
}
