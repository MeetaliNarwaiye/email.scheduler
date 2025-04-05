package com.project.email.scheduler.audit.audit_log.controller;

import com.project.email.scheduler.audit.audit_log.dao.AuditLog;
import com.project.email.scheduler.audit.audit_log.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping("audit_log")
@Validated
public class AuditLogController {

    @Autowired
    AuditLogService service;

    @PostMapping("/get")
    public List<AuditLog> get(@RequestBody List<Integer> ids) {
        return service.getAll(ids);
    }

    @PostMapping()
    public List<AuditLog> createAll(@Valid @RequestBody List<AuditLog> data) {
        return service.createAll(data);
    }

}