package com.project.email.scheduler.audit.audit_log.service;

import com.project.email.scheduler.audit.audit_log.dao.AuditLog;
import com.project.email.scheduler.audit.audit_log.dao.AuditLogRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AuditLogService {

    @Autowired
    AuditLogRepo repo;

    public List<AuditLog> getAll() {
        return repo.findAll();
    }

    public List<AuditLog> getAll(List<Long> id) {
        return repo.findAllById(id);
    }

    public AuditLog create(AuditLog data) {
        return repo.save(data);
    }

    public List<AuditLog> createAll(List<AuditLog> data) {
        return repo.saveAll(data);
    }


//    public Object findAll(List<AuditLog> data, Integer page, Integer size, Sort sort) {
//
//        if (data == null) {
//            AuditLog temp = new AuditLog();
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