package com.project.email.scheduler.audit.config_tables.config_audit.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.email.scheduler.audit.config_tables.config_audit_fields.dao.ConfigAuditFields;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.List;


@Entity
@Table(name = "CONFIG_AUDIT")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@IdClass(ConfigAuditID.class)
@DynamicInsert
@DynamicUpdate
public class ConfigAudit implements Serializable {
    @Id
    @JsonProperty("table_name")
    @Column(name = "table_name")
    String tableName;

    @JsonProperty("insert_flag")
    @Column(name = "insert_flag")
    Boolean insertFlag;

    @JsonProperty("update_flag")
    @Column(name = "update_flag")
    Boolean updateFlag;

    @JsonProperty("delete_flag")
    @Column(name = "delete_flag")
    Boolean deleteFlag;

    @JsonProperty("enable_log")
    @Column(name = "enable_log")
    Boolean enableLog;

    @JsonProperty("object_group")
    @Column(name = "object_group")
    String objectGroup;

    @JsonProperty("group_key_field")
    @Column(name = "group_key_field")
    String groupKeyField;

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

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name = "table_name", referencedColumnName = "table_name", insertable = false, updatable = false)
    })
    @JsonProperty(value = "field_name", access = JsonProperty.Access.READ_ONLY)
    private List<ConfigAuditFields> fields;


}