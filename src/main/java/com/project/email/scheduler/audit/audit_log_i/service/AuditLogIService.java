package com.project.email.scheduler.audit.audit_log_i.service;

import com.project.email.scheduler.audit.audit_log_i.dao.AuditLogI;
import com.project.email.scheduler.audit.audit_log_i.dao.AuditLogIID;
import com.project.email.scheduler.audit.audit_log_i.dao.AuditLogIRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AuditLogIService {

    @Autowired
    AuditLogIRepo repo;


    public List<AuditLogI> getAll() {
        return repo.findAll();
    }

    public List<AuditLogI> getAll(List<AuditLogIID> id) {
        return repo.findAllById(id);
    }


    public List<AuditLogI> createAll(List<AuditLogI> data) {
        return repo.saveAll(data);
    }

//    public Object findAll(List<AuditLogI> data, Integer page, Integer size, Sort sort) {
//
//        if (data == null) {
//            AuditLogI temp = new AuditLogI();
//            data = new ArrayList<>();
//            data.add(temp);
//        }
//
//        // if data is null send full dataset
//        if (page != null) {
//            Pageable pageable = null;
//
//            if (sort.isEmpty()) pageable = PageRequest.of(page, size);
//            else pageable = PageRequest.of(page, size, sort);
//
//            return repo.findAllByAnyField(data, pageable);
//        } else {
//            return repo.findAllByAnyField(data);
//        }
//    }

}