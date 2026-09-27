package com.digitalforensics.dao;

import com.digitalforensics.model.Role;
import com.digitalforensics.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class UserDAO extends BaseDAO {

    private static final String SELECT = "SELECT * FROM users ";

    public User findByUsername(String username) {
        return queryOne(SELECT + "WHERE username = ?", UserDAO::map, username);
    }

    public User findById(int id) {
        return queryOne(SELECT + "WHERE id = ?", UserDAO::map, id);
    }

    /** Active users, for "assign investigator" and "hand over to" dropdowns. */
    public List<User> findActive() {
        return queryList(SELECT + "WHERE active = TRUE ORDER BY full_name", UserDAO::map);
    }

    public void updateLastLogin(int userId) {
        update("UPDATE users SET last_login = ? WHERE id = ?", LocalDateTime.now(), userId);
    }

    private static User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setFullName(rs.getString("full_name"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setSalt(rs.getString("salt"));
        u.setActive(rs.getBoolean("active"));
        u.setLastLogin(time(rs, "last_login"));
        return u;
    }
}
