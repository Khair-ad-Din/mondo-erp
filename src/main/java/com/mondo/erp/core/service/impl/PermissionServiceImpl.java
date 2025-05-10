// src/main/java/com/mondo/erp/core/service/impl/PermissionServiceImpl.java
package com.mondo.erp.core.service.impl;

import com.mondo.erp.core.model.Permission;
import com.mondo.erp.core.repository.PermissionRepository;
import com.mondo.erp.core.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    @Autowired
    public PermissionServiceImpl(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    public List<Permission> findAll() {
        return permissionRepository.findAll();
    }

    @Override
    public List<Permission> findByModule(String module) {
        return permissionRepository.findByModule(module);
    }

    @Override
    public Optional<Permission> findById(Long id) {
        return permissionRepository.findById(id);
    }

    @Override
    public Optional<Permission> findByName(String name) {
        return permissionRepository.findByName(name);
    }

    @Override
    public Set<String> findAllModules() {
        return permissionRepository.findAll().stream()
                .map(Permission::getModule)
                .filter(module -> module != null && !module.isEmpty())
                .collect(Collectors.toSet());
    }
}