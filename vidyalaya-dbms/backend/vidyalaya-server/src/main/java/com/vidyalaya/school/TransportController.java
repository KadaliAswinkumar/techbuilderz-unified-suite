package com.vidyalaya.school;

import com.vidyalaya.domain.TransportRoute;
import com.vidyalaya.domain.repository.TransportRouteRepository;
import jakarta.validation.constraints.NotBlank;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transport/routes")
public class TransportController {

    private final TransportRouteRepository transportRouteRepository;

    public TransportController(TransportRouteRepository transportRouteRepository) {
        this.transportRouteRepository = transportRouteRepository;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<TransportRoute> list() {
        return transportRouteRepository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public TransportRoute create(@RequestBody RouteReq req) {
        TransportRoute r = new TransportRoute();
        r.setName(req.name());
        r.setDescription(req.description());
        return transportRouteRepository.save(r);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public TransportRoute update(@PathVariable UUID id, @RequestBody RouteReq req) {
        TransportRoute r = transportRouteRepository.findById(id).orElseThrow();
        r.setName(req.name());
        r.setDescription(req.description());
        return transportRouteRepository.save(r);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public void delete(@PathVariable UUID id) {
        transportRouteRepository.deleteById(id);
    }

    public record RouteReq(@NotBlank String name, String description) {}
}
