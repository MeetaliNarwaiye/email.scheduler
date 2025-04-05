package com.project.email.scheduler.audit.audit_log.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDate;

@Entity
@Table(name = "AUDIT_LOG")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@DynamicInsert
@DynamicUpdate
public class AuditLog {

    @Id
    @JsonProperty("id")
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @JsonProperty("group_key")
    @Column(name = "group_key")
    String groupKey;

    @JsonProperty("object_group")
    @Column(name = "object_group")
    String objectGroup;

    @Column(name = "created_on", insertable = false, updatable = false)
    @JsonProperty(value = "created_on", access = JsonProperty.Access.READ_ONLY)
    LocalDate createdOn;


}