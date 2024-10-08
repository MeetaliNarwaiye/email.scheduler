package com.project.email.scheduler.audit.config_tables.config_audit_fields.dao;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigAuditFieldsRepo extends JpaRepository<ConfigAuditFields, ConfigAuditFieldsID>,
        ConfigAuditFieldsCustomRepo<ConfigAuditFields> {
}