package com.project.email.scheduler.audit.audit_log_i.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AuditLogIID {

    @JsonProperty("id")
    @Column(name = "id")
    Integer id;

    @JsonProperty("table_name")
    @Column(name = "table_name")
    String tableName;

    @JsonProperty("table_key")
    @Column(name = "table_key")
    String tableKey;
}
