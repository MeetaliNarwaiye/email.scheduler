package com.project.email.scheduler.audit;

import jakarta.persistence.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class AuditListener {
    @Autowired
    AuditListenerProcess process;
    @Autowired
    AuditListenerVariables variables;

    @PostLoad
    public void getBeforeUpdate(Object object) {
        if(RequestContextHolder.getRequestAttributes()!=null) {
            if (object == null) {
                return;
            }
            String tableName = getTableName(object);
            if (tableName != null) {
                // Store the state before the update
                List<JSONObject> jsonObjectList;
                jsonObjectList = new ArrayList<>();
                if (variables.getOldValues().get(tableName) != null) {
                    jsonObjectList = variables.getOldValues().get(tableName);
                }
                jsonObjectList.add(convertToJson(object));
                variables.getOldValues().put(tableName, jsonObjectList);
            }
        }
    }

    @PostUpdate
    public void getAfterUpdate(Object object) {
        if(RequestContextHolder.getRequestAttributes()!=null) {
            if (object == null) {
                return;
            }
            String tableName = getTableName(object);
            if (tableName != null) {
                variables.getNewValues().put(tableName, convertToJson(object));
                process.updatedData(variables, tableName, object);
                variables.getNewValues().clear();
            }
        }
    }

    @PrePersist
    public void getAfterInsert(Object object) {
        if(RequestContextHolder.getRequestAttributes()!=null) {
            if (object == null) {
                return;
            }
            String tableName = getTableName(object);
            if (tableName != null) {
                variables.getNewValues().put(tableName, convertToJson(object));
                process.addedData(variables, tableName, object);
                variables.getNewValues().clear();
            }
        }
    }
    @PostRemove
    public void getBeforeDelete(Object object) {
        if(RequestContextHolder.getRequestAttributes()!=null) {
            if (object == null) {
                return;
            }
            String tableName = getTableName(object);
            if (tableName != null) {
                variables.getNewValues().put(tableName, convertToJson(object));
                process.deletedData(variables, tableName, object);
                variables.getNewValues().clear();
            }
        }
    }


    private JSONObject convertToJson(Object object) {
        JSONObject jsonObject = new JSONObject();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {

                Column annotation = field.getAnnotation(Column.class);
                jsonObject.put(annotation == null ? field.getName() : annotation.name(), field.get(object));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    public String getTableName(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("Object cannot be null");
        }
        Class<?> clazz = object.getClass();

        if (clazz.isAnnotationPresent(Entity.class)) {
            if (clazz.isAnnotationPresent(Table.class)) {
                Table table = clazz.getAnnotation(Table.class);
                return table.name();
            } else {
                System.out.println("The entity class does not have a @Table annotation.");
            }
        } else {
            System.out.println("The provided object is not a JPA entity.");
        }
        return null;
    }


}
