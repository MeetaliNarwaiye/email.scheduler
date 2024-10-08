package com.project.email.scheduler.audit.audit_log.controller;

import com.project.email.scheduler.audit.audit_log.dao.AuditLog;
import com.project.email.scheduler.audit.audit_log.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping("/${var.base_url}" + "/t/audit_log")
@Validated
public class AuditLogController {

    @Autowired
    AuditLogService service;


    @PostMapping("/get")
    public List<AuditLog> get(@RequestBody List<Long> ids) {
        return service.getAll(ids);
    }

    @PostMapping("/find")
    public Object find(@RequestBody List<AuditLog> data,
                       @RequestParam(required = false) Integer page,
                       @RequestParam(required = false) Integer size,
                       @RequestParam(required = false) Sort sort) {

        return service.findAll(data, page, size, sort);
    }

    @PostMapping()
    public List<AuditLog> createAll(@Valid @RequestBody List<AuditLog> data) {
        return service.createAll(data);
    }

}