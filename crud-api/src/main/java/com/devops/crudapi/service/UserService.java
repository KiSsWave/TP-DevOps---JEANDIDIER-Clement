package com.devops.crudapi.service;

import com.devops.crudapi.dto.UserCreateRequest;
import com.devops.crudapi.dto.UserResponse;
import com.devops.crudapi.entity.User;
import com.devops.crudapi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    /**
     * Récupérer tous les utilisateurs
     */
    public List<UserResponse> getAllUsers() {
        logger.info("Récupération de tous les utilisateurs");
        List<User> users = userRepository.findAll();
        logger.info("Nombre d'utilisateurs récupérés: {}", users.size());

        return users.stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer un utilisateur par UUID
     */
    public Optional<UserResponse> getUserById(UUID uuid) {
        logger.info("Récupération de l'utilisateur avec UUID: {}", uuid);
        Optional<User> user = userRepository.findById(uuid);

        if (user.isPresent()) {
            logger.info("Utilisateur trouvé: {}", user.get().getFullname());
            return Optional.of(new UserResponse(user.get()));
        } else {
            logger.warn("Aucun utilisateur trouvé avec l'UUID: {}", uuid);
            return Optional.empty();
        }
    }

    /**
     * Créer un nouvel utilisateur
     */
    public UserResponse createUser(UserCreateRequest request) {
        logger.info("Création d'un nouvel utilisateur: {}", request.getFullname());

        User user = new User(
                request.getFullname(),
                request.getStudyLevel(),
                request.getAge()
        );

        User savedUser = userRepository.save(user);
        logger.info("Utilisateur créé avec succès avec l'UUID: {}", savedUser.getUuid());

        return new UserResponse(savedUser);
    }

    /**
     * Mettre à jour un utilisateur existant
     */
    public Optional<UserResponse> updateUser(UUID uuid, UserCreateRequest request) {
        logger.info("Mise à jour de l'utilisateur avec UUID: {}", uuid);

        Optional<User> existingUser = userRepository.findById(uuid);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setFullname(request.getFullname());
            user.setStudyLevel(request.getStudyLevel());
            user.setAge(request.getAge());

            User updatedUser = userRepository.save(user);
            logger.info("Utilisateur mis à jour avec succès: {}", updatedUser.getFullname());

            return Optional.of(new UserResponse(updatedUser));
        } else {
            logger.warn("Impossible de mettre à jour: utilisateur non trouvé avec l'UUID: {}", uuid);
            return Optional.empty();
        }
    }

    /**
     * Supprimer un utilisateur
     */
    public boolean deleteUser(UUID uuid) {
        logger.info("Suppression de l'utilisateur avec UUID: {}", uuid);

        if (userRepository.existsById(uuid)) {
            userRepository.deleteById(uuid);
            logger.info("Utilisateur supprimé avec succès: {}", uuid);
            return true;
        } else {
            logger.warn("Impossible de supprimer: utilisateur non trouvé avec l'UUID: {}", uuid);
            return false;
        }
    }
}