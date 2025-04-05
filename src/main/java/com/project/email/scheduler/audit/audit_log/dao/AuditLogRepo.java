package com.project.email.scheduler.audit.audit_log.dao;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepo extends JpaRepository<AuditLog, Integer>,
        AuditLogCustomRepo<AuditLog> {

}