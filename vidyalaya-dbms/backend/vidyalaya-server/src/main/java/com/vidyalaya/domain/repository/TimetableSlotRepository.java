package com.vidyalaya.domain.repository;

import com.vidyalaya.domain.TimetableSlot;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimetableSlotRepository extends JpaRepository<TimetableSlot, UUID> {
    List<TimetableSlot> findByTeacherIdOrderByDayOfWeekAscStartTimeAsc(UUID teacherId);
}
