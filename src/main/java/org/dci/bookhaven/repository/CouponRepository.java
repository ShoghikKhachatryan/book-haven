package org.dci.bookhaven.repository;

import org.dci.bookhaven.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Coupon findByCode(String code);

    List<Coupon> findAllByIsActiveTrue();

    List<Coupon> findAllByIsActiveFalse();

    List<Coupon> findAllByIsActiveTrueAndStartDateBefore(LocalDateTime now);
}
