// src/main/java/com/mondo/erp/core/service/PermissionService.java
package com.mondo.erp.core.service;

import com.mondo.erp.core.model.Permission;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PermissionService {

    List<Permission> findAll();

    List<Permission> findByModule(String module);

    Optional<Permission> findById(Long id);

    Optional<Permission> findByName(String name);

    Set<String> findAllModules();
}