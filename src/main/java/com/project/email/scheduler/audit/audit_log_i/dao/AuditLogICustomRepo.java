package com.project.email.scheduler.audit.audit_log_i.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface AuditLogICustomRepo<T> {

    List<T> findAllByAnyField(List<T> data);

    Page<T> findAllByAnyField(List<T> data, Pageable pageable);
}