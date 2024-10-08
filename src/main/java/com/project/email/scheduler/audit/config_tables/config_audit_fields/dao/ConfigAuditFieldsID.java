package com.project.email.scheduler.audit.config_tables.config_audit_fields.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ConfigAuditFieldsID implements Serializable {


    @JsonProperty("table_name")
    @Column(name = "table_name")
    String tableName;

    @JsonProperty("field_name")
    @Column(name = "field_name")
    String fieldName;

}