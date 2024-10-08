package com.project.email.scheduler.audit;

import com.project.email.scheduler.audit.audit_log.dao.AuditLog;
import com.project.email.scheduler.audit.audit_log.service.AuditLogService;
import com.project.email.scheduler.audit.audit_log_i.dao.AuditLogI;
import com.project.email.scheduler.audit.audit_log_i.service.AuditLogIService;
import com.project.email.scheduler.audit.config_tables.config_audit.dao.ConfigAudit;
import com.project.email.scheduler.audit.config_tables.config_audit.dao.ConfigAuditID;
import com.project.email.scheduler.audit.config_tables.config_audit.service.ConfigAuditService;
import com.project.email.scheduler.audit.config_tables.config_audit_fields.dao.ConfigAuditFields;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Component
@RequestScope
public class AuditListenerProcess {

    @Autowired
    ConfigAuditService configAuditService;
    @Autowired
    AuditLogService auditLogService;

    @Autowired
    AuditLogIService auditLogIService;

    public JSONObject getIds(Object object) {
        JSONObject jsonObject = new JSONObject();
        if (object == null) {
            throw new IllegalArgumentException("Object cannot be null");
        }

        Class<?> clazz = object.getClass();
        if (clazz.isAnnotationPresent(Entity.class)) {
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true);
                    try {
                        Column annotation = field.getAnnotation(Column.class);
                        jsonObject.put(annotation == null ? field.getName() : annotation.name(), field.get(object));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.out.println("The provided object is not a JPA entity.");
        }
        return jsonObject;
    }

    private JSONObject getUpdatedValues(JSONObject oldState, JSONObject newState) {
        JSONObject updatedValues = new JSONObject();

        if (oldState == null) {
            oldState = new JSONObject();
        }

        for (String key : newState.keySet()) {

            Object oldValue = oldState.opt(key);
            Object newValue = newState.get(key);

            //  if values are different or key is new
            if (!oldState.has(key) || !Objects.equals(oldValue, newValue)) {
                updatedValues.put(key, newValue);
            }
        }
        return updatedValues;
    }


    public void updatedData(AuditListenerVariables variables, String tableName, Object object) {

        if (tableName != null) {
            final JSONObject[] oldState = new JSONObject[1];

            ConfigAudit configAudit = configAuditService.getById(ConfigAuditID.builder()
                    .tableName(tableName)
                    .build());

            List<JSONObject> allOldValues = variables.getOldValues().get(tableName);
            //set the oldstate
            JSONObject primaryKeys = getIds(object);
            allOldValues.forEach(n -> {
                boolean allPresent = true;
                for (String key : primaryKeys.keySet()) {
                    if (!n.has(key) || !n.opt(key).equals(primaryKeys.opt(key))) {
                        allPresent = false;
                        break;
                    }
                }
                if (allPresent) {
                    oldState[0] = n;
                }
            });
            //set newState
            JSONObject newState = variables.getNewValues().get(tableName);

            //updated values
            JSONObject updatedValues = getUpdatedValues(oldState[0], newState);
            //primary keys
            JSONObject idsValues = getIds(object);
            String groupKey = null;
            if (configAudit != null && configAudit.getGroupKeyField() != null) {
                groupKey = idsValues.has(configAudit.getGroupKeyField().toLowerCase()) ?
                        idsValues.get(configAudit.getGroupKeyField().toLowerCase()).toString() : null;
            }

            Iterator<String> keys = updatedValues.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                if (!(key.equalsIgnoreCase("CREATED_BY") || key.equalsIgnoreCase("CREATED_ON") ||
                        key.equalsIgnoreCase("CREATED_AT") || key.equalsIgnoreCase("CHANGED_BY") ||
                        key.equalsIgnoreCase("CHANGED_AT") || key.equalsIgnoreCase("CHANGED_ON"))) {
                    String newValue = updatedValues.get(key).toString();
                    String oldValue = Arrays.stream(oldState)
                            .filter(jsonObject -> jsonObject.has(key))
                            .map(jsonObject -> {
                                try {
                                    return jsonObject.getString(key);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    return null;
                                }
                            })
                            .findFirst()
                            .orElse(null);

                    variables.getAuditLogIList().add(AuditLogI.builder()
                            .fieldName(key.toUpperCase())
                            .oldValue(oldValue)
                            .newValue(newValue)
                            .tableName(tableName)
                            .tableKey(idsValues.keySet().stream().map(idsValues::getString).collect(Collectors.joining("|")))
                            .groupKey(groupKey)
                            .objectGroup(configAudit != null ? configAudit.getObjectGroup() : null)
                            .action("U")
                            .build());
                }
            }
            //Storing only when enableLog is 1
            if (configAudit != null && configAudit.getEnableLog() != null && configAudit.getEnableLog() &&
                    configAudit.getUpdateFlag() != null && configAudit.getUpdateFlag()) {

                List<ConfigAuditFields> configAuditFieldsList = configAudit.getFields();
                AuditLog auditLog;

                //creating AuditLog
                if (!variables.getAuditLog().containsKey(configAudit.getObjectGroup())) {
                    auditLog = auditLogService.create(AuditLog.builder()
                            .objectGroup(configAudit.getObjectGroup())
                            .groupKey(groupKey)
                            .build());
                } else {
                    auditLog = AuditLog.builder()
                            .objectGroup(configAudit.getObjectGroup())
                            .groupKey(groupKey)
                            .id(variables.getAuditLog().get(configAudit.getObjectGroup()))
                            .build();
                }
                if (auditLog != null) {
                    //added so that for same ObjectGroup only 1 entry should be stored in AuditLog
                    variables.getAuditLog().put(auditLog.getObjectGroup(), auditLog.getId());

                    List<AuditLogI> finalAuditLogIList;
                    //stored the fields which are mentioned in ConfigAuditFields
                    if (configAuditFieldsList != null && !configAuditFieldsList.isEmpty()) {
                        finalAuditLogIList = variables.getAuditLogIList().stream()
                                .filter(auditLogI -> configAuditFieldsList.stream()
                                        .anyMatch(configAuditFields -> configAuditFields.getFieldName().equalsIgnoreCase(auditLogI.getFieldName()) &&
                                                auditLogI.getObjectGroup().equalsIgnoreCase(auditLog.getObjectGroup())))
                                .peek(auditLogI -> auditLogI.setChangeId(auditLog.getId()))
                                .collect(Collectors.toList());
                    } else {
                        finalAuditLogIList = variables.getAuditLogIList().stream().filter(auditLogI -> auditLogI.getObjectGroup().equalsIgnoreCase(auditLog.getObjectGroup()))
                                .peek(n -> n.setChangeId(auditLog.getId()))
                                .collect(Collectors.toList());
                    }
                    auditLogIService.createAll(finalAuditLogIList);
                }
            }
        }
    }

    public void addedData(AuditListenerVariables variables, String tableName, Object object) {
        ConfigAudit configAudit = configAuditService.getById(ConfigAuditID.builder()
                .tableName(tableName)
                .build());

        JSONObject addedValues = variables.getNewValues().get(tableName);
        //primary keys
        JSONObject idsValues = getIds(object);
        String groupKey = null;
        if (configAudit != null && configAudit.getGroupKeyField() != null) {
            groupKey = idsValues.has(configAudit.getGroupKeyField().toLowerCase()) ?
                    idsValues.get(configAudit.getGroupKeyField().toLowerCase()).toString() : null;
        }

        Iterator<String> keys = addedValues.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (!(key.equalsIgnoreCase("CREATED_BY") || key.equalsIgnoreCase("CREATED_ON") ||
                    key.equalsIgnoreCase("CREATED_AT") || key.equalsIgnoreCase("CHANGED_BY") ||
                    key.equalsIgnoreCase("CHANGED_AT") || key.equalsIgnoreCase("CHANGED_ON"))) {
                String newValue = addedValues.get(key).toString();

                variables.getAuditLogIList().add(AuditLogI.builder()
                        //.changeId(auditLog.getId())
                        .fieldName(key.toUpperCase())
                        .oldValue(null)
                        .newValue(newValue)
                        .tableName(tableName)
                        .tableKey(idsValues.keySet().stream().map(idsValues::getString).collect(Collectors.joining("|")))
                        .groupKey(groupKey)
                        .objectGroup(configAudit != null ? configAudit.getObjectGroup() : null)
                        .action("I")
                        .build());
            }
        }

        if (configAudit != null && configAudit.getEnableLog() != null && configAudit.getEnableLog() &&
                configAudit.getInsertFlag() != null && configAudit.getInsertFlag()) {
            List<ConfigAuditFields> configAuditFieldsList = configAudit.getFields();


            AuditLog auditLog;
            if (!variables.getAuditLog().containsKey(configAudit.getObjectGroup())) {
                auditLog = auditLogService.create(AuditLog.builder()
                        .objectGroup(configAudit.getObjectGroup())
                        .groupKey(groupKey)
                        .build());
            } else {
                auditLog = AuditLog.builder()
                        .objectGroup(configAudit.getObjectGroup())
                        .groupKey(groupKey)
                        .id(variables.getAuditLog().get(configAudit.getObjectGroup()))
                        .build();
            }
            if (auditLog != null) {
                variables.getAuditLog().put(auditLog.getObjectGroup(), auditLog.getId());
                List<AuditLogI> finalAuditLogIList;
                //stored the fields which are mentioned in ConfigAuditFields
                if (configAuditFieldsList != null && !configAuditFieldsList.isEmpty()) {
                    finalAuditLogIList = variables.getAuditLogIList().stream()
                            .filter(auditLogI -> configAuditFieldsList.stream()
                                    .anyMatch(configAuditFields -> configAuditFields.getFieldName().equalsIgnoreCase(auditLogI.getFieldName()) &&
                                            auditLogI.getObjectGroup().equalsIgnoreCase(auditLog.getObjectGroup())))
                            .peek(auditLogI -> auditLogI.setChangeId(auditLog.getId()))
                            .collect(Collectors.toList());
                } else {
                    finalAuditLogIList = variables.getAuditLogIList().stream().filter(auditLogI -> auditLogI.getObjectGroup().equalsIgnoreCase(auditLog.getObjectGroup()))
                            .peek(n -> n.setChangeId(auditLog.getId()))
                            .collect(Collectors.toList());
                }
                auditLogIService.createAll(finalAuditLogIList);
            }
        }
    }

    public void deletedData(AuditListenerVariables variables, String tableName, Object object) {

        final JSONObject[] oldState = new JSONObject[1];
        ConfigAudit configAudit = configAuditService.getById(ConfigAuditID.builder()
                .tableName(tableName)
                .build());

        //primary keys
        JSONObject idsValues = getIds(object);
        String groupKey = null;
        if (configAudit != null && configAudit.getGroupKeyField() != null) {
            groupKey = idsValues.has(configAudit.getGroupKeyField().toLowerCase()) ?
                    idsValues.get(configAudit.getGroupKeyField().toLowerCase()).toString() : null;
        }

        List<JSONObject> allOldValues = variables.getOldValues().get(tableName);
        JSONObject primaryKeys = getIds(object);

        allOldValues.forEach(n -> {
            boolean allPresent = true;
            for (String key : primaryKeys.keySet()) {
                if (!n.has(key) || !n.opt(key).equals(primaryKeys.opt(key))) {
                    allPresent = false;
                    break;
                }
            }
            if (allPresent) {
                oldState[0] = n;
            }
        });

        Iterator<String> keys = oldState[0].keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (!(key.equalsIgnoreCase("CREATED_BY") || key.equalsIgnoreCase("CREATED_ON") ||
                    key.equalsIgnoreCase("CREATED_AT") || key.equalsIgnoreCase("CHANGED_BY") ||
                    key.equalsIgnoreCase("CHANGED_AT") || key.equalsIgnoreCase("CHANGED_ON"))) {
                String oldValue = null;
                try {
                    oldValue = oldState[0].getString(key);
                } catch (Exception ignore) {
                }

                variables.getAuditLogIList().add(AuditLogI.builder()
                        //.changeId(auditLog.getId())
                        .fieldName(key.toUpperCase())
                        .oldValue(oldValue)
                        .newValue(null)
                        .tableName(tableName)
                        .tableKey(idsValues.keySet().stream().map(idsValues::getString).collect(Collectors.joining("|")))
                        .groupKey(groupKey)
                        .objectGroup(configAudit != null ? configAudit.getObjectGroup() : null)
                        .action("D")
                        .build());
            }
        }
        if (configAudit != null && configAudit.getEnableLog() != null && configAudit.getEnableLog() &&
                configAudit.getDeleteFlag() != null && configAudit.getDeleteFlag()) {
            List<ConfigAuditFields> configAuditFieldsList = configAudit.getFields();


            AuditLog auditLog;
            if (!variables.getAuditLog().containsKey(configAudit.getObjectGroup())) {
                auditLog = auditLogService.create(AuditLog.builder()
                        .objectGroup(configAudit.getObjectGroup())
                        .groupKey(groupKey)
                        .build());
            } else {
                auditLog = AuditLog.builder()
                        .objectGroup(configAudit.getObjectGroup())
                        .groupKey(groupKey)
                        .id(variables.getAuditLog().get(configAudit.getObjectGroup()))
                        .build();
            }
            if (auditLog != null) {
                variables.getAuditLog().put(auditLog.getObjectGroup(), auditLog.getId());
                List<AuditLogI> finalAuditLogIList;
                //stored the fields which are mentioned in ConfigAuditFields
                if (configAuditFieldsList != null && !configAuditFieldsList.isEmpty()) {
                    finalAuditLogIList = variables.getAuditLogIList().stream()
                            .filter(auditLogI -> configAuditFieldsList.stream()
                                    .anyMatch(configAuditFields -> configAuditFields.getFieldName().equalsIgnoreCase(auditLogI.getFieldName()) &&
                                            auditLogI.getObjectGroup().equalsIgnoreCase(auditLog.getObjectGroup())))
                            .peek(auditLogI -> auditLogI.setChangeId(auditLog.getId()))
                            .collect(Collectors.toList());
                } else {
                    finalAuditLogIList = variables.getAuditLogIList().stream().filter(auditLogI -> auditLogI.getObjectGroup().equalsIgnoreCase(auditLog.getObjectGroup()))
                            .peek(n -> n.setChangeId(auditLog.getId()))
                            .collect(Collectors.toList());
                }
                auditLogIService.createAll(finalAuditLogIList);
            }
        }
    }
}
