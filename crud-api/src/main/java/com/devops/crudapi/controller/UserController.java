package com.devops.crudapi.controller;

import com.devops.crudapi.dto.ApiResponse;
import com.devops.crudapi.dto.UserCreateRequest;
import com.devops.crudapi.dto.UserResponse;
import com.devops.crudapi.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        logger.info("GET /api/users - Récupération de tous les utilisateurs");

        List<UserResponse> users = userService.getAllUsers();
        ApiResponse<List<UserResponse>> response = ApiResponse.success(
                "Utilisateurs récupérés avec succès",
                users
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String uuid) {
        logger.info("GET /api/users/{} - Récupération utilisateur par UUID", uuid);

        try {
            UUID userUuid = UUID.fromString(uuid);
            Optional<UserResponse> user = userService.getUserById(userUuid);

            if (user.isPresent()) {
                ApiResponse<UserResponse> response = ApiResponse.success(
                        "Utilisateur trouvé",
                        user.get()
                );
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<UserResponse> response = ApiResponse.error("Utilisateur non trouvé");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (IllegalArgumentException e) {
            logger.error("UUID invalide: {}", uuid);
            ApiResponse<UserResponse> response = ApiResponse.error("Format UUID invalide");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request) {
        logger.info("POST /api/users - Création d'un nouvel utilisateur: {}", request.getFullname());

        UserResponse createdUser = userService.createUser(request);
        ApiResponse<UserResponse> response = ApiResponse.success(
                "Utilisateur créé avec succès",
                createdUser
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable String uuid,
            @Valid @RequestBody UserCreateRequest request) {
        logger.info("PUT /api/users/{} - Mise à jour utilisateur", uuid);

        try {
            UUID userUuid = UUID.fromString(uuid);
            Optional<UserResponse> updatedUser = userService.updateUser(userUuid, request);

            if (updatedUser.isPresent()) {
                ApiResponse<UserResponse> response = ApiResponse.success(
                        "Utilisateur mis à jour avec succès",
                        updatedUser.get()
                );
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<UserResponse> response = ApiResponse.error("Utilisateur non trouvé");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (IllegalArgumentException e) {
            logger.error("UUID invalide: {}", uuid);
            ApiResponse<UserResponse> response = ApiResponse.error("Format UUID invalide");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String uuid) {
        logger.info("DELETE /api/users/{} - Suppression utilisateur", uuid);

        try {
            UUID userUuid = UUID.fromString(uuid);
            boolean deleted = userService.deleteUser(userUuid);

            if (deleted) {
                ApiResponse<Void> response = ApiResponse.success("Utilisateur supprimé avec succès");
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<Void> response = ApiResponse.error("Utilisateur non trouvé");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (IllegalArgumentException e) {
            logger.error("UUID invalide: {}", uuid);
            ApiResponse<Void> response = ApiResponse.error("Format UUID invalide");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}