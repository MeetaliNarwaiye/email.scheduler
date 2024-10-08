package com.project.email.scheduler.audit.audit_log_i.controller;

import com.project.email.scheduler.audit.audit_log_i.dao.AuditLogI;
import com.project.email.scheduler.audit.audit_log_i.dao.AuditLogIID;
import com.project.email.scheduler.audit.audit_log_i.service.AuditLogIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping("/${var.base_url}" + "/t/audit_log_i")
@Validated
public class AuditLogIController {

    @Autowired
    AuditLogIService service;


    @PostMapping("/get")
    public List<AuditLogI> get(@RequestBody List<AuditLogIID> ids) {
        return service.getAll(ids);
    }

    @PostMapping("/find")
    public Object find(@RequestBody List<AuditLogI> data,
                       @RequestParam(required = false) Integer page,
                       @RequestParam(required = false) Integer size,
                       @RequestParam(required = false) Sort sort) {

        return service.findAll(data, page, size, sort);
    }

    @PostMapping()
    public List<AuditLogI> createAll(@Valid @RequestBody List<AuditLogI> data) {
        return service.createAll(data);
    }

}