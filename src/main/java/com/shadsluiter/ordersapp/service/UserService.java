package com.shadsluiter.ordersapp.service;

import com.shadsluiter.ordersapp.data.UserRepository;
import com.shadsluiter.ordersapp.models.UserEntity;
import com.shadsluiter.ordersapp.models.UserModel;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    /**
     * UserRepository provides database access for users and roles.
     * Spring Security itself never talks directly to the database —
     * it relies on this service to do that work.
     */
    private final UserRepository userRepository;

    /**
     * PasswordEncoder is responsible for hashing and verifying passwords.
     * IMPORTANT: passwords are NEVER stored or compared in plain text.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Spring injects both dependencies automatically.
     * This service acts as the bridge between:
     *   - Spring Security
     *   - The database
     */
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Saves a new user.
     * This method is used for traditional (username/password) registration.
     *
     * NOTE:
     * - Passwords MUST be hashed before saving
     * - This method will NOT be used for OAuth users later
     */
    public UserModel save(UserModel userModel) {

        // Hash the raw password before storing it in the database
        userModel.setPassword(passwordEncoder.encode(userModel.getPassword()));

        UserEntity userEntity = convertToEntity(userModel);
        UserEntity savedUser = userRepository.save(userEntity);

        return convertToModel(savedUser);
    }

    /**
     * This is the MOST IMPORTANT method for Spring Security authentication.
     *
     * Spring Security calls this automatically during login.
     * It replaces any custom "findByLoginName" logic.
     *
     * Flow:
     * 1. User submits login form
     * 2. Spring Security intercepts the request
     * 3. Spring calls loadUserByUsername(...)
     * 4. Returned UserDetails is used for authentication & authorization
     */
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        // Fetch the user from the database
        UserEntity userEntity = userRepository.findByLoginName(username);

        // If no user exists, authentication fails
        if (userEntity == null) {
            throw new UsernameNotFoundException("User not found");
        }

        /**
         * Convert application roles (Strings like "ROLE_ADMIN")
         * into Spring Security GrantedAuthority objects.
         *
         * Spring Security does NOT understand your domain roles directly.
         */
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : userEntity.getRoles()) {
            authorities.add((GrantedAuthority) () -> role);
        }

        /**
         * Return a Spring Security User object.
         *
         * This object is NOT your UserEntity.
         * It is a security-specific representation of the authenticated user.
         *
         * username  -> used for identification
         * password  -> hashed password (never plain text)
         * authorities -> roles for authorization checks
         */
        return new User(
                userEntity.getUserName(),
                userEntity.getPassword(),
                authorities
        );
    }

    /**
     * Legacy-style lookup method.
     *
     * This is NOT used by Spring Security for login anymore.
     * It may still be used by controllers or admin pages.
     */
    public UserModel findByLoginName(String loginName) {

        UserEntity userEntity = userRepository.findByLoginName(loginName);
        if (userEntity == null) {
            return null;
        }

        return convertToModel(userEntity);
    }

    /**
     * Verifies a raw password against the stored hash.
     *
     * NOTE:
     * Spring Security normally handles this automatically during login.
     * This method exists mainly for custom validation logic.
     */
    public boolean verifyPassword(UserModel user) {

        UserEntity userEntity = userRepository.findByLoginName(user.getUserName());
        if (userEntity == null) {
            return false;
        }

        // Compare raw password to hashed password
        return passwordEncoder.matches(
                user.getPassword(),
                userEntity.getPassword()
        );
    }

    public UserModel findById(String id) {
        UserEntity userEntity = userRepository.findById(Long.parseLong(id));
        return convertToModel(userEntity);
    }

    public void delete(String id) {
        userRepository.deleteById(Long.parseLong(id));
    }

    public List<UserModel> findAll() {
        List<UserEntity> userEntities = userRepository.findAll();
        return convertToModels(userEntities);
    }

    /**
     * Converts database entities into models used by controllers/views.
     * This separation prevents leaking persistence logic into controllers.
     */
    private List<UserModel> convertToModels(List<UserEntity> userEntities) {
        List<UserModel> userModels = new ArrayList<>();
        for (UserEntity userEntity : userEntities) {
            userModels.add(convertToModel(userEntity));
        }
        return userModels;
    }

    /**
     * Converts UserEntity (database form) into UserModel (application form).
     *
     * NOTE:
     * The password here is still hashed.
     * The application should NEVER display or modify it directly.
     */
    private UserModel convertToModel(UserEntity userEntity) {

        UserModel userModel = new UserModel();
        userModel.setId(userEntity.getId().toString());
        userModel.setUserName(userEntity.getUserName());
        userModel.setPassword(userEntity.getPassword());

        // Account state flags used by Spring Security
        userModel.setEnabled(userEntity.isEnabled());
        userModel.setAccountNonExpired(userEntity.isAccountNonExpired());
        userModel.setCredentialsNonExpired(userEntity.isCredentialsNonExpired());
        userModel.setAccountNonLocked(userEntity.isAccountNonLocked());

        // Roles determine authorization (what the user can access)
        userModel.setRoles(userEntity.getRoles());

        return userModel;
    }

    /**
     * Converts UserModel into UserEntity for persistence.
     *
     * IMPORTANT:
     * - Password is assumed to already be hashed
     * - OAuth users will bypass this method entirely later
     */
    private UserEntity convertToEntity(UserModel userModel) {

        UserEntity userEntity = new UserEntity();

        if (userModel.getId() != null) {
            userEntity.setId(Long.parseLong(userModel.getId()));
        }

        userEntity.setUserName(userModel.getUserName());
        userEntity.setPassword(userModel.getPassword());

        // These flags directly affect login success in Spring Security
        userEntity.setEnabled(userModel.isEnabled());
        userEntity.setAccountNonExpired(userModel.isAccountNonExpired());
        userEntity.setCredentialsNonExpired(userModel.isCredentialsNonExpired());
        userEntity.setAccountNonLocked(userModel.isAccountNonLocked());

        userEntity.setRoles(userModel.getRoles());

        return userEntity;
    }
}
