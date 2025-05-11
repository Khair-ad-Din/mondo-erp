package com.mondo.erp.core.service;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> findAll();

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findByCompany(Company company);

    User save(User user);

    void deleteById(Long id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    void updateLastAccess(String username);
}
