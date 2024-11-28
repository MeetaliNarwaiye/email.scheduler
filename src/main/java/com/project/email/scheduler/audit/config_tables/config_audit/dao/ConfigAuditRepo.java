package com.project.email.scheduler.audit.config_tables.config_audit.dao;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigAuditRepo extends JpaRepository<ConfigAudit, ConfigAuditID> {
}