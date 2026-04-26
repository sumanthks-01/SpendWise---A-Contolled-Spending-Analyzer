package com.spendwise.repository;

import com.spendwise.model.Subscription;
import com.spendwise.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUser(User user);
    Optional<Subscription> findByIdAndUser(Long id, User user);
    void deleteByIdAndUser(Long id, User user);
}
