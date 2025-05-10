// src/main/java/com/mondo/erp/core/service/RoleService.java
package com.mondo.erp.core.service;

import com.mondo.erp.core.model.Role;
import java.util.List;
import java.util.Optional;

public interface RoleService {

    List<Role> findAll();

    Optional<Role> findById(Long id);

    Optional<Role> findByName(String name);

    Role save(Role role);

    void deleteById(Long id);

    boolean existsByName(String name);
}