package com.project.email.scheduler.audit.config_tables.config_audit.dao;

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
public class ConfigAuditID implements Serializable {

    @JsonProperty("table_name")
    @Column(name = "table_name")
    String tableName;

}