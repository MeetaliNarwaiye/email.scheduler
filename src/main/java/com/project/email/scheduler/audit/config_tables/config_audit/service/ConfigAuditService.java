package com.project.email.scheduler.audit.config_tables.config_audit.service;

import com.project.email.scheduler.audit.config_tables.config_audit.dao.ConfigAudit;
import com.project.email.scheduler.audit.config_tables.config_audit.dao.ConfigAuditID;
import com.project.email.scheduler.audit.config_tables.config_audit.dao.ConfigAuditRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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


    //find all based on input provided
    public Object findAll(List<ConfigAudit> data, Integer page, Integer size, Sort sort) {

        // if data is null send full dataset
        if (data == null) {
            ConfigAudit temp = new ConfigAudit();
            data = new ArrayList<>();
            data.add(temp);
        }


        if (page != null) {
            Pageable pageable = null;

            if (sort.isEmpty()) pageable = PageRequest.of(page, size);
            else pageable = PageRequest.of(page, size, sort);

            return repo.findAllByAnyField(data, pageable);
        } else {
            return repo.findAllByAnyField(data);
        }
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