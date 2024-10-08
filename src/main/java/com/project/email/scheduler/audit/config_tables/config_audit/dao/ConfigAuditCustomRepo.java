package com.project.email.scheduler.audit.config_tables.config_audit.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface ConfigAuditCustomRepo<T> {

    List<T> findAllByAnyField(List<T> data);

    Page<T> findAllByAnyField(List<T> data, Pageable pageable);
}