package com.project.email.scheduler.audit.config_tables.config_audit_fields.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

@Entity
@Table(name = "CONFIG_AUDIT_FIELDS")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@IdClass(ConfigAuditFieldsID.class)
@DynamicInsert
@DynamicUpdate
public class ConfigAuditFields implements Serializable {

    @Id
    @JsonProperty("table_name")
    @Column(name = "table_name")
    String tableName;

    @Id
    @JsonProperty("field_name")
    @Column(name = "field_name")
    String fieldName;

    @JsonProperty("enable_log")
    @Column(name = "enable_log")
    Integer enableLog;

    @Column(name = "created_on", insertable = false, updatable = false)
    @JsonProperty(value = "created_on", access = JsonProperty.Access.READ_ONLY)
    String createdOn;


    @Column(name = "created_at", insertable = false, updatable = false)
    @JsonProperty(value = "created_at", access = JsonProperty.Access.READ_ONLY)
    String createdAt;

    @Column(name = "changed_on", insertable = false, updatable = false)
    @JsonProperty(value = "changed_on", access = JsonProperty.Access.READ_ONLY)
    String changedOn;

    @Column(name = "changed_at", insertable = false, updatable = false)
    @JsonProperty(value = "changed_at", access = JsonProperty.Access.READ_ONLY)
    String changedAt;

}