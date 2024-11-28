package com.project.email.scheduler.audit.audit_log_i.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;


@Entity
@Table(name = "AUDIT_LOG_I")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@DynamicInsert
@DynamicUpdate
public class AuditLogI {

    @Id
    @JsonProperty("id")
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @JsonProperty("table_name")
    @Column(name = "table_name")
    String tableName;

    @JsonProperty("table_key")
    @Column(name = "table_key")
    String tableKey;

    @JsonProperty("object_group")
    @Column(name = "object_group")
    String objectGroup;


    @JsonProperty("group_key")
    @Column(name = "group_key")
    String groupKey;

    @JsonProperty("action")
    @Column(name = "action")
    String action;

    @JsonProperty("new_value")
    @Column(name = "new_value")
    String newValue;

    @JsonProperty("old_value")
    @Column(name = "old_value")
    String oldValue;

    @JsonProperty("field_name")
    @Column(name = "field_name")
    String fieldName;

    @JsonProperty("change_id")
    @Column(name = "change_id")
    Integer changeId;


    @Column(name = "created_on", insertable = false, updatable = false)
    @JsonProperty(value = "created_on", access = JsonProperty.Access.READ_ONLY)
    String createdOn;


    @Column(name = "created_at", insertable = false, updatable = false)
    @JsonProperty(value = "created_at", access = JsonProperty.Access.READ_ONLY)
    String createdAt;

}