package com.vidyalaya.domain.repository;

import com.vidyalaya.domain.TransportRoute;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransportRouteRepository extends JpaRepository<TransportRoute, UUID> {}
