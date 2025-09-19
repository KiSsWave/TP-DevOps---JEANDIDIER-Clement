package com.devops.crudapi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    @Autowired
    private DataSource dataSource;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        logger.info("GET /health - Vérification du statut de l'application");

        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());

        Map<String, String> dbStatus = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                dbStatus.put("status", "UP");
                dbStatus.put("database", "MariaDB");
            } else {
                dbStatus.put("status", "DOWN");
                dbStatus.put("error", "Connection invalid");
            }
        } catch (Exception e) {
            logger.error("Erreur de connexion à la base de données", e);
            dbStatus.put("status", "DOWN");
            dbStatus.put("error", e.getMessage());
            health.put("status", "DOWN");
        }

        health.put("database", dbStatus);

        logger.info("Health check terminé - Status: {}", health.get("status"));
        return ResponseEntity.ok(health);
    }
}