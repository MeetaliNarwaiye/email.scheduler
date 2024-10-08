package com.project.email.scheduler.db;

import jakarta.persistence.*;
import lombok.Getter;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.reflections.scanners.Scanners.SubTypes;

@Getter
public class RepoCommonService {

    private Class parentClass;
    private String tableName;
    private String schemaName;
    private List<Field> allFields;
    private List<Field> idFeilds;
    private List<Field> allFieldWithoutID;
    private HashMap<String, String> dataTypeMap = new HashMap<>();

    public RepoCommonService() {

        //Get All Interfaces ------ CustomRepo<T>
        for (Type genericInterfaces : getClass().getGenericInterfaces()) {
            if (genericInterfaces instanceof ParameterizedType parameterizedType) {
                if (parameterizedType.getRawType() instanceof Class<?> rawType) {

                    //take CustomRepo<T>

                    ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
                    configurationBuilder.addUrls(ClasspathHelper.forClass(rawType));

                    //Use Reflection to find all interface where CustomRepo<T> is used
                    Reflections reflections = new Reflections(configurationBuilder);
                    Set<Class<?>> classes = reflections.get(SubTypes.of(rawType).asClass());

                    //Filter class to find main interface where the CustomRepo<T> is defined except the current class
                    Class<?> mainClass = classes.stream()
                            .filter(n -> !getClass().getSimpleName().equals(n.getSimpleName()))
                            .findFirst().orElse(null);

                    if (mainClass != null) {

                        //Get Arguments from the defined repo which is our parent class to operate
                        Type[] types = mainClass.getGenericInterfaces();
                        for (Type type : types) {
                            ParameterizedType typevalue = (ParameterizedType) type;
                            Class<?> rawTypevalue = (Class<?>) typevalue.getRawType();
                            if (rawTypevalue.getSimpleName().equals(rawType.getSimpleName())) {
                                Type[] test = typevalue.getActualTypeArguments();
                                this.parentClass = (Class<?>) test[0];
                            }
                        }

                    }
                }
            }
        }
//        if (getParentClass() == null) {
//            throw new CustomException(02, List.of(getClass().getName()));
//        }

        //fill all the data
        filldata();
    }

    private void filldata() {
        tableName = getTableName(parentClass);
        allFields = removeUnwantedFields(parentClass.getDeclaredFields());
        fillMetadata(allFields);
        dataTypeMap = getDataTypeMap(allFields);
    }

//    public void setParentClass(Class parentClass) {
//        this.parentClass = parentClass;
//    }

    public List<Field> removeUnwantedFields(Field[] allFields) {
        ArrayList<Field> allNewField = new ArrayList<>();
        for (Field each : allFields) {
            Transient aTransient = each.getAnnotation(Transient.class);
            if (aTransient == null) {
                allNewField.add(each);
            }
        }
        return allNewField;
    }

    public void fillMetadata(List<Field> allFields) {
        allFieldWithoutID = new ArrayList<>();
        idFeilds = new ArrayList<>();
        for (Field field : allFields) {
            Id idAnnotation = field.getAnnotation(Id.class);
            if (idAnnotation == null) {
                allFieldWithoutID.add(field);
            } else {
                idFeilds.add(field);
            }
        }

    }

    public HashMap<String, String> getDataTypeMap(List<Field> allFields) {
        HashMap<String, String> dataTypeMap = new HashMap<>();
        for (Field each : allFields) {
            dataTypeMap.put(each.getName(), each.getType().toString());
        }
        return dataTypeMap;
    }

    public String getTableName(Class c) {
        Table t = (Table) c.getAnnotation(Table.class);
        this.schemaName = t.schema();
        return t.name();
    }

    public String getFormattedValue(String key,
                                    Object value) {
        String dataType = dataTypeMap.get(key);
        String result = "";
        if (dataType.contains("Date")) {
            try {
                Field field = parentClass.getDeclaredField(key);
                Temporal temporal = field.getDeclaredAnnotation(Temporal.class);
                switch (temporal.value()) {
                    case DATE:
                        result = new SimpleDateFormat("yyyy-MM-dd").format((Date) value);
                        break;
                    case TIME:
                        result = new SimpleDateFormat("hh:mm:ss").format((Date) value);
                        break;
                    case TIMESTAMP:
                        result = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format((Date) value);
                        break;
                }
            } catch (NoSuchFieldException ex) {
                ex.printStackTrace();
            }
        } else if (dataType.contains("Boolean") || dataType.contains("boolean")) {
            if ((Boolean) value) {
                result = "1";
            } else {
                result = "0";
            }
        } else {
            result = String.valueOf(value);
        }
        return result;
    }

    public String countString(String s) {
        return s.replaceFirst("\\*", "count(*)");
    }

    public String pageableString(Pageable pageable) {

        //convert query into page based on data provided in Pageable object
        StringBuilder select = new StringBuilder();
        if (pageable != null) {

            if (pageable.getSort() != null && !pageable.getSort().isEmpty()) {
                select.append(" ORDER BY ");
                select.append(pageable.getSort()
                        .stream()
                        .map(n -> String.format("%s %s", n.getProperty(), n.getDirection()))
                        .collect(Collectors.joining(",")));
            }

            select.append(" OFFSET ").append(pageable.getOffset()).append(" ROWS ");
            select.append(" FETCH FIRST ").append(pageable.getPageSize()).append(" ROWS ONLY ");

        }

        //final string order by col ASC offset 10 rows fetch first 10 rows only
        return select.toString();
    }

    public <T> String findAllByAnyFieldString(List<T> dataList) {

        //if only one record is present make query with like statement
        if (dataList.size() == 1) {
            return findAllByAnyFieldString(dataList.get(0));
        }

        StringBuilder select = new StringBuilder();
        select.append("SELECT * FROM ");
        if (schemaName != null && !schemaName.isEmpty()) {
            select.append(schemaName).append(".");
        }
        select.append(tableName).append(" WHERE ");

        List<String> columnNames = new ArrayList<>();
        List<String> valuePairs = new ArrayList<>();

        //first flag to get the column name from first record
        boolean first = true;

        //loop on object list for making query
        for (Object data : dataList) {

            List<String> values = new ArrayList<>();

            //loop on all fields of current object
            for (Field f : allFields) {

                try {

                    //make the field accessible for getting value
                    f.setAccessible(true);

                    //get value
                    Object value = f.get(data);

                    //don't operate if value is null, move to next value
                    if (value != null) {

                        //check if Child Class is Embedded in the current class
                        if (f.isAnnotationPresent(Embedded.class)) {

                            //if child class is present loop on child class to fill the column names and value of child class
                            for (Field cf : value.getClass().getDeclaredFields()) {

                                //make the field accessible for getting value
                                cf.setAccessible(true);

                                //get value
                                Object cvalue = cf.get(data);
                                if (first) { //for first record fill the column names
                                    columnNames.add(getColumnName(cf));
                                }

                                //add values
                                values.add(getFormattedValue(cf.getName(), cvalue));
                            }
                        } else { //Normal Field
                            if (first) { //for first record fill the column names
                                columnNames.add(getColumnName(f));
                            }

                            //add values
                            values.add(getFormattedValue(f.getName(), value));
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            //fill all the values in value pair ('1','2','3')
            if (!values.isEmpty()) {
                first = false;
                valuePairs.add("('" + String.join("', '", values) + "')");
            }
        }

        //fill all the column names ('col1','col2')
        select.append("(").append(String.join(", ", columnNames)).append(")");

        //fill all the value pairs (('1','2','3'),('1','2','3'))
        select.append(" IN (").append(String.join(", ", valuePairs)).append(")");

        //final string will be select * from table name where ('col1','col2') in (('1','2','3'),('1','2','3'))
        return select.toString();
    }


    //if column annotation is present take name from column name else take name from field name
    String getColumnName(Field f) {
        Column column = f.getAnnotation(Column.class);
        if (column != null && column.name() != null && !column.name().isEmpty()) {
            return column.name();
        } else {
            return f.getName();
        }
    }


    public <T> String findAllByAnyFieldString(T data) {

        StringBuilder select = new StringBuilder();
        select.append("SELECT * FROM ");
        if (schemaName != null && !schemaName.isEmpty()) {
            select.append(schemaName).append(".");
        }
        select.append(tableName).append(" ");
        select.append(" WHERE ");
        boolean notfirst = false;

        //loop on all fields of current object
        for (Field f : allFields) {
            try {
                //make the field accessible for getting value
                f.setAccessible(true);

                //get value from field
                Object value = f.get(data);

                //don't operate if value is null, move to next value
                if (value != null) {

                    //check if Child Class is Embedded in the current class
                    if (f.isAnnotationPresent(Embedded.class)) {

                        //if child class is present loop on child class to fill the column names and value of child class
                        for (Field cf : value.getClass().getDeclaredFields()) {

                            //make the field accessible for getting value
                            cf.setAccessible(true);

                            //get value
                            Object cvalue = cf.get(data);
                            select.append(getColumnName(cf));
                            select.append(" LIKE ").append("'");
                            select.append(getFormattedValue(cf.getName(), cvalue)
                                    .replace("*", "%"));
                            select.append("'");
                        }
                    } else { //Normal Field

                        //append "And" after first Field present
                        if (notfirst) {
                            select.append(" AND ");
                        }
                        notfirst = true;

                        select.append(getColumnName(f));
                        select.append(" LIKE ").append("'");
                        select.append(getFormattedValue(f.getName(), value)
                                .replace("*", "%"));
                        select.append("'");
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        //final string will be select * from table name where 'col1' like '%1%'
        return select.toString();
    }


//    public String findByAnyIDString(Object id) {
//
//        StringBuilder select = new StringBuilder();
//        select.append("SELECT * FROM ");
//        if (schemaName != null && !schemaName.isEmpty()) {
//            select.append(schemaName).append(".");
//        }
//        select.append(tableName).append(" ");
//        select.append(" WHERE ");
//        boolean notfirst = false;
//        for (Field f : idFeilds) {
//            try {
//                f.setAccessible(true);
//                Object value = f.get(id);
//                if (value != null) {
//                    if (notfirst) {
//                        select.append(" AND ");
//                    }
//                    notfirst = true;
//                    select.append(getColumnName(f)).append(" = ").append("'");
//                    select.append(getFormattedValue(f.getName(), value));
//                    select.append("'");
//                }
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            }
//        }
//
//        return select.toString();
//    }
//
//    public String updateByID(Object data) throws IllegalAccessException, NoSuchFieldException {
//
//        StringBuilder select = new StringBuilder();
//        select.append("UPDATE ");
//        if (schemaName != null && !schemaName.isEmpty()) {
//            select.append(schemaName).append(".");
//        }
//        select.append(tableName).append(" ").append(" SET ");
//        int count = 0;
//        //Set the non Key Fields
//        boolean first = false;
//        for (Field f : allFieldWithoutID) {
//            f.setAccessible(true);
//            Column column = f.getAnnotation(Column.class);
//            if (column == null || column.updatable()) {
//
////            Field idfield = data.getClass().getDeclaredField(f.getName());
////            idfield.setAccessible(true);
////            Object value = idfield.get(data);
//
//                Object value = f.get(data);
//                if (value != null) {
//                    count++;
//                    if (first) {
//                        select.append(" , ");
//                    }
//                    first = true;
//
//                    if (column != null && column.name() != null && !column.name().isEmpty()) {
//                        select.append(column.name());
//                    } else {
//                        select.append(f.getName());
//                    }
//
//                    select.append(" = ")
//                            .append("'")
//                            .append(getFormattedValue(f.getName(), value))
//                            .append("'");
//                }
//            }
//        }
//
////        if (count < 1) throw new CustomException(03, HttpStatus.NOT_MODIFIED);
//
//        select.append(" WHERE ");
//
//        int idcount = 0;
//        //Set Key Fields
//        first = false;
//        for (Field f : idFeilds) {
//
//            f.setAccessible(true);
//            Column column = f.getAnnotation(Column.class);
//            Object value = f.get(data);
//
////            Field idfield = data.getClass().getDeclaredField(f.getName());
////            idfield.setAccessible(true);
////            Object value = idfield.get(data);
//
//            if (value != null) {
//                idcount++;
//                if (first) {
//                    select.append(" AND ");
//                }
//                first = true;
//
//                if (column != null && column.name() != null && !column.name().isEmpty()) {
//                    select.append(column.name());
//                } else {
//                    select.append(f.getName());
//                }
//
//                select.append(" = ")
//                        .append("'")
//                        .append(getFormattedValue(f.getName(), value))
//                        .append("'");
//            }
//        }
//
////        if (idcount < 1) throw new CustomException(04, HttpStatus.BAD_REQUEST);
//
//        return select.toString();
//    }

}