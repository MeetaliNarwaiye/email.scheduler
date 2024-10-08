package com.project.email.scheduler.audit.audit_log_i.dao;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogIRepo extends JpaRepository<AuditLogI, AuditLogIID>,
        AuditLogICustomRepo<AuditLogI> {

}