package com.project.email.scheduler.audit.audit_log_i.controller;

import com.project.email.scheduler.audit.audit_log_i.dao.AuditLogI;
import com.project.email.scheduler.audit.audit_log_i.service.AuditLogIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping("audit_log_i")
@Validated
public class AuditLogIController {

    @Autowired
    AuditLogIService service;


    @PostMapping("/get")
    public List<AuditLogI> get() {
        return service.getAll();
    }

//    @PostMapping("/find")
//    public Object find(@RequestBody List<AuditLogI> data,
//                       @RequestParam(required = false) Integer page,
//                       @RequestParam(required = false) Integer size,
//                       @RequestParam(required = false) Sort sort) {
//
//        return service.findAll(data, page, size, sort);
//    }

    @PostMapping("/create")
    public List<AuditLogI> createAll(@Valid @RequestBody List<AuditLogI> data) {
        return service.createAll(data);
    }

}