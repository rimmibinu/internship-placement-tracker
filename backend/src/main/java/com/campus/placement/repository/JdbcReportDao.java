package com.campus.placement.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Plain JDBC (DriverManager + Statement + ResultSet) report - demonstrates Unit III concepts.
 * Department-wise placement report using LEFT JOIN + GROUP BY.
 */
@Component
public class JdbcReportDao {
    private final String url;
    private final String user;
    private final String password;

    public JdbcReportDao(@Value("${spring.datasource.url}") String url,
                         @Value("${spring.datasource.username}") String user,
                         @Value("${spring.datasource.password:}") String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public List<Map<String, Object>> departmentReport() {
        String sql = "SELECT s.department AS department, COUNT(DISTINCT s.id) AS students, "
                + "COUNT(a.id) AS applications, "
                + "COALESCE(SUM(CASE WHEN a.status = 'OFFERED' THEN 1 ELSE 0 END), 0) AS offers "
                + "FROM students s LEFT JOIN applications a ON a.student_id = s.id "
                + "GROUP BY s.department ORDER BY s.department";
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("department", rs.getString("department"));
                row.put("students", rs.getLong("students"));
                row.put("applications", rs.getLong("applications"));
                row.put("offers", rs.getLong("offers"));
                rows.add(row);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("JDBC report failed: " + e.getMessage(), e);
        }
        return rows;
    }
}
