package com.project.email.scheduler.audit.config_tables.config_audit.service;

import com.project.email.scheduler.audit.config_tables.config_audit.dao.ConfigAudit;
import com.project.email.scheduler.audit.config_tables.config_audit.dao.ConfigAuditID;
import com.project.email.scheduler.audit.config_tables.config_audit.dao.ConfigAuditRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigAuditService {

    @Autowired
    ConfigAuditRepo repo;


    public List<ConfigAudit> getAll() {
        return repo.findAll();
    }

    public ConfigAudit getById(ConfigAuditID id) {
        return repo.findById(id).orElse(null);
    }

    //create & update
    public List<ConfigAudit> saveAll(List<ConfigAudit> data) {
        return repo.saveAll(data);
    }

    //delete
    public void deleteById(List<ConfigAuditID> ids) {
        repo.deleteAllById(ids);
    }

}