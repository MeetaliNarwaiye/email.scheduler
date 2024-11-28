package com.project.email.scheduler.audit.config_tables.config_audit.controller;

import com.project.email.scheduler.audit.config_tables.config_audit.dao.ConfigAudit;
import com.project.email.scheduler.audit.config_tables.config_audit.dao.ConfigAuditID;
import com.project.email.scheduler.audit.config_tables.config_audit.service.ConfigAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("config_audit")
public class ConfigAuditController {

    @Autowired
    ConfigAuditService service;

    @GetMapping("/get")
    public List<ConfigAudit> get() {
        return service.getAll();
    }

    @PostMapping("/save")
    public List<ConfigAudit> save(@RequestBody List<ConfigAudit> data) {
        return service.saveAll(data);
    }

    @DeleteMapping("/delete")
    public void delete(@RequestBody List<ConfigAuditID> data) {
        service.deleteById(data);
    }
}