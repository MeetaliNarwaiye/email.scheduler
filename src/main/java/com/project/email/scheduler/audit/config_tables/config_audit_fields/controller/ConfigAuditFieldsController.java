package com.project.email.scheduler.audit.config_tables.config_audit_fields.controller;

import com.project.email.scheduler.audit.config_tables.config_audit_fields.dao.ConfigAuditFields;
import com.project.email.scheduler.audit.config_tables.config_audit_fields.dao.ConfigAuditFieldsID;
import com.project.email.scheduler.audit.config_tables.config_audit_fields.service.ConfigAuditFieldsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("config_audit_fields")
public class ConfigAuditFieldsController {

    @Autowired
    ConfigAuditFieldsService service;

    @GetMapping("/get")
    public List<ConfigAuditFields> get() {
        return service.getAll();
    }

    @PostMapping("/save")
    public List<ConfigAuditFields> save(@RequestBody List<ConfigAuditFields> data) {
        return service.saveAll(data);
    }

    @DeleteMapping("/delete")
    public void delete(@RequestBody List<ConfigAuditFieldsID> data) {
        service.deleteById(data);
    }
}