package com.catchaybk.customer.repository;

import com.catchaybk.customer.entity.CustomerAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface CustomerAccountRepository extends JpaRepository<CustomerAccount, Long> {
    @Lock(LockModeType.OPTIMISTIC)
    Optional<CustomerAccount> findByCustomerId(String customerId);
}