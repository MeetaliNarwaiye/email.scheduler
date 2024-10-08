package com.project.email.scheduler.audit.config_tables.config_audit_fields.service;

import com.project.email.scheduler.audit.config_tables.config_audit_fields.dao.ConfigAuditFields;
import com.project.email.scheduler.audit.config_tables.config_audit_fields.dao.ConfigAuditFieldsID;
import com.project.email.scheduler.audit.config_tables.config_audit_fields.dao.ConfigAuditFieldsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class ConfigAuditFieldsService {

    @Autowired
    ConfigAuditFieldsRepo repo;

    public List<ConfigAuditFields> getAll() {
        return repo.findAll();
    }

    public ConfigAuditFields getById(ConfigAuditFieldsID id) {
        return repo.findById(id).orElse(null);
    }


    //find all based on input provided
    public Object findAll(List<ConfigAuditFields> data, Integer page, Integer size, Sort sort) {

        // if data is null send full dataset
        if (data == null) {
            ConfigAuditFields temp = new ConfigAuditFields();
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
    public List<ConfigAuditFields> saveAll(List<ConfigAuditFields> data) {

       return repo.saveAll(data);
    }


    //delete
    public void deleteById(List<ConfigAuditFieldsID> ids) {
        repo.deleteAllById(ids);
    }

}