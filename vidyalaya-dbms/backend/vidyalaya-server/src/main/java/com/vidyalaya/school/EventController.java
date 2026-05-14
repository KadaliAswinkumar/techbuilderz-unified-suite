package com.vidyalaya.school;

import com.vidyalaya.domain.Event;
import com.vidyalaya.domain.repository.EventRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventRepository eventRepository;

    public EventController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<Event> range(@RequestParam Instant from, @RequestParam Instant to) {
        return eventRepository.findByStartTimeBetweenOrderByStartTimeAsc(from, to);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public Event create(@RequestBody EventReq req) {
        Event e = new Event();
        e.setTitle(req.title());
        e.setStartTime(req.startTime());
        e.setEndTime(req.endTime());
        e.setEventType(req.eventType());
        return eventRepository.save(e);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public Event update(@PathVariable UUID id, @RequestBody EventReq req) {
        Event e = eventRepository.findById(id).orElseThrow();
        e.setTitle(req.title());
        e.setStartTime(req.startTime());
        e.setEndTime(req.endTime());
        e.setEventType(req.eventType());
        return eventRepository.save(e);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public void delete(@PathVariable UUID id) {
        eventRepository.deleteById(id);
    }

    public record EventReq(
            @NotBlank String title,
            @NotNull Instant startTime,
            @NotNull Instant endTime,
            String eventType) {}
}
