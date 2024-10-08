package com.project.email.scheduler.audit;

import com.project.email.scheduler.audit.audit_log_i.dao.AuditLogI;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Component
@RequestScope
public class AuditListenerVariables {

    private Map<String, List<JSONObject>> dataMap = new HashMap<>();
    private Map<String, List<JSONObject>> oldValues = new HashMap<>();
    private Map<String, JSONObject> newValues = new HashMap<>();
    private Map<String, Integer> auditLog = new HashMap<>();
    List<AuditLogI> auditLogIList=new ArrayList<>();

}


