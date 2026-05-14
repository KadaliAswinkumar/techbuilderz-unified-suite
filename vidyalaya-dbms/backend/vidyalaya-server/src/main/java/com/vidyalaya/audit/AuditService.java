package com.vidyalaya.audit;

import com.vidyalaya.domain.AuditLog;
import com.vidyalaya.domain.repository.AuditLogRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(UUID actorUserId, String action, String detail) {
        AuditLog a = new AuditLog();
        a.setActorUserId(actorUserId);
        a.setAction(action);
        a.setDetail(detail);
        auditLogRepository.save(a);
    }
}
