package com.project.email.scheduler.audit.audit_log_i.dao;

import com.project.email.scheduler.db.RepoCommonService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class AuditLogICustomRepoImpl<T> extends RepoCommonService
        implements AuditLogICustomRepo<T> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<T> findAllByAnyField(List<T> data) {

        String query = findAllByAnyFieldString(data);

        return em.createNativeQuery(query, getParentClass()).getResultList();

    }

    @Override
    public Page<T> findAllByAnyField(List<T> data, Pageable pageable) {

        String query = findAllByAnyFieldString(data);

        List<T> result = em.createNativeQuery(query + pageableString(pageable), getParentClass()).getResultList();

        long count = (int) em.createNativeQuery(countString(query)).getSingleResult();

        return new PageImpl<>(result, pageable, count);
    }

}