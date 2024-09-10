package org.dci.bookhaven.service;

import org.dci.bookhaven.model.Coupon;
import org.dci.bookhaven.service.impl.CouponServiceImpl;

import java.util.List;

public interface CouponService {

    Coupon getByCode(String code);

    List<Coupon> getAllCoupons();

    String applyCouponMsg(String couponCode);

    boolean isValid(String couponCode);

    Coupon create(Coupon coupon);

    void updateCouponStatus();

    void deactivate(Long id);

    void reactivate(Long id);

    void delete(Long id);

    Coupon getById(Long id);
}
