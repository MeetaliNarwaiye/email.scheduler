package com.project.email.scheduler.audit.config_tables.config_audit_fields.service;

import com.project.email.scheduler.audit.config_tables.config_audit_fields.dao.ConfigAuditFields;
import com.project.email.scheduler.audit.config_tables.config_audit_fields.dao.ConfigAuditFieldsID;
import com.project.email.scheduler.audit.config_tables.config_audit_fields.dao.ConfigAuditFieldsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ConfigAuditFieldsService {

    @Autowired
    ConfigAuditFieldsRepo repo;

    public List<ConfigAuditFields> getAll() {
        return repo.findAll();
    }

    public ConfigAuditFields getById(ConfigAuditFieldsID id) {
        return repo.findById(id).orElse(null);
    }

    //create & update
    public List<ConfigAuditFields> saveAll(List<ConfigAuditFields> data) {

        return repo.saveAll(data);
    }

    //delete
    public void deleteById(List<ConfigAuditFieldsID> ids) {
        repo.deleteAllById(ids);
    }

}