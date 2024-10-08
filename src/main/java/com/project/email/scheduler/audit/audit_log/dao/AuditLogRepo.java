package com.project.email.scheduler.audit.audit_log.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepo extends JpaRepository<AuditLog, Long>,
        AuditLogCustomRepo<AuditLog> {
    List<AuditLog> findAllByObjectGroupAndGroupKey(String objectGroup, String groupKey);

}