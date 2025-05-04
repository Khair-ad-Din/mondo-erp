package com.mondo.erp.core.repository;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findByCompany(Company company);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);
}
