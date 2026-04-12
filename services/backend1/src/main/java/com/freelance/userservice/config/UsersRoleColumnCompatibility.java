package com.freelance.userservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Anciennes bases : colonne {@code users.role} en ENUM ou VARCHAR trop court → échec à l'inscription COLLABORATEUR.
 * Idempotent : MySQL accepte MODIFY vers VARCHAR(32) même si déjà correct.
 */
@Component
@Order(10_000)
public class UsersRoleColumnCompatibility implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UsersRoleColumnCompatibility.class);

    private final DataSource dataSource;

    public UsersRoleColumnCompatibility(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        try (Connection c = dataSource.getConnection(); Statement st = c.createStatement()) {
            st.executeUpdate("ALTER TABLE users MODIFY COLUMN role VARCHAR(32) NOT NULL");
            log.info("Colonne users.role vérifiée (VARCHAR(32), compatible COLLABORATEUR).");
        } catch (SQLException e) {
            if (isMissingTable(e)) {
                log.debug("users.role : table absente pour l'instant, Hibernate la créera (ddl-auto).");
                return;
            }
            log.warn("users.role : impossible d'appliquer MODIFY VARCHAR(32) — {}", e.getMessage());
        }
    }

    private static boolean isMissingTable(SQLException e) {
        // MySQL 1146 = ER_NO_SUCH_TABLE
        return "42S02".equals(e.getSQLState()) || (e.getMessage() != null && e.getMessage().contains("doesn't exist"));
    }
}
