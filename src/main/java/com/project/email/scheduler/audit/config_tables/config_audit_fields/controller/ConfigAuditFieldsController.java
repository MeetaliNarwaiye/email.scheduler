package com.project.email.scheduler.audit.config_tables.config_audit_fields.controller;

import com.project.email.scheduler.audit.config_tables.config_audit_fields.dao.ConfigAuditFields;
import com.project.email.scheduler.audit.config_tables.config_audit_fields.dao.ConfigAuditFieldsID;
import com.project.email.scheduler.audit.config_tables.config_audit_fields.service.ConfigAuditFieldsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/${var.base_url}" + "/c/config_audit_fields")
public class ConfigAuditFieldsController {

    @Autowired
    ConfigAuditFieldsService service;

    @GetMapping("/get")
    public List<ConfigAuditFields> get() {
        return service.getAll();
    }

    @PostMapping("/find")
    public Object find(@RequestBody(required = false) List<ConfigAuditFields> data,
                       @RequestParam(required = false) Integer page,
                       @RequestParam(required = false) Integer size,
                       @RequestParam(required = false) Sort sort) {

        return service.findAll(data, page, size, sort);
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