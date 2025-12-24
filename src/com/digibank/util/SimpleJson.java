package com.digibank.util;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.OffsetDateTime;
import java.time.Instant;
import java.util.List;

public class SimpleJson {
    
    public static String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
        if (obj instanceof String) return "\"" + escape((String)obj) + "\"";
        if (obj instanceof LocalDate || obj instanceof LocalDateTime || obj instanceof ZonedDateTime || obj instanceof OffsetDateTime || obj instanceof Instant) {
            return "\"" + obj.toString() + "\"";
        }
        if (obj instanceof List) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            List<?> list = (List<?>) obj;
            for (int i = 0; i < list.size(); i++) {
                sb.append(toJson(list.get(i)));
                if (i < list.size() - 1) sb.append(",");
            }
            sb.append("]");
            return sb.toString();
        }
        
        // Simple object reflection
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        Field[] fields = obj.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            try {
                String name = fields[i].getName();
                if (isSensitiveField(name)) {
                    continue; // do not serialize secrets
                }
                sb.append("\"").append(name).append("\":");
                sb.append(toJson(fields[i].get(obj)));
                sb.append(",");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("}");
        return sb.toString();
    }

    private static boolean isSensitiveField(String name) {
        return name.contains("password") || name.contains("Password") || name.contains("secret") || name.contains("Secret") || name.contains("salt") || name.contains("pqPublicKey");
    }

    private static String escape(String raw) {
        return raw.replace("\"", "\\\"");
    }
}
