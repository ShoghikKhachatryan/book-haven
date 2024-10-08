package org.dci.bookhaven.controller;

import jakarta.servlet.http.HttpSession;
import org.dci.bookhaven.model.Cart;
import org.dci.bookhaven.model.LineItem;
import org.dci.bookhaven.model.User;
import org.dci.bookhaven.service.CartService;
import org.dci.bookhaven.service.CouponService;
import org.dci.bookhaven.service.LineItemService;
import org.dci.bookhaven.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final LineItemService lineItemService;
    private final UserService userService;
    private final CouponService couponService;

    @Autowired
    public CartController(
            CartService cartService,
            LineItemService lineItemService,
            CouponService couponService,
            UserService userService) {
        this.cartService = cartService;
        this.lineItemService = lineItemService;
        this.couponService = couponService;
        this.userService = userService;
    }

    @GetMapping("")
    public String viewCart(Model model) {
        if(userService.getLoggedInUser() == null) {
            return "redirect:/login";
        }

        boolean isLoggedIn = userService.isLoggedIn();
        model.addAttribute("isLoggedIn", isLoggedIn);

        User user = userService.getLoggedInUser();
        Long userId = user.getId();
        Cart cart = cartService.getOrCreateCart(userId);
        model.addAttribute("cart", cart);

        if(cart.getLineItems() != null && !cart.getLineItems().isEmpty()) {
            model.addAttribute("lineItems", cart.getLineItems());
            Map<Long, BigDecimal> lineTotals = new HashMap<>();
            for(LineItem lineItem : cart.getLineItems()) {
                lineTotals.put(lineItem.getId(), lineItemService.getLineTotal(lineItem));
            }
            model.addAttribute("lineTotals", lineTotals);
        }

        if(cart.getLineItems()!=null && !cart.getLineItems().isEmpty()){
            if(cartService.getCartItemNumber(cart) == 1){
                model.addAttribute("cartSizeText", "item");
            } else{
                model.addAttribute("cartSizeText", "items");
            }
            model.addAttribute("cartSize", cartService.getCartItemNumber(cart));
            model.addAttribute("isEmpty", false);
        }else{
            model.addAttribute("cartSizeText", "items");
            model.addAttribute("cartSize", 0);
            model.addAttribute("isEmpty", true);
        }

        BigDecimal total = cartService.getTotal(cart.getId());
        model.addAttribute("total", total);

        if(cart.getShippingMethod() != null){
            model.addAttribute("shippingMethod", cart.getShippingMethod());
            model.addAttribute("shippingCost", cartService.getShippingCost(cart.getShippingMethod()));
        }else{
            model.addAttribute("shippingMethod", "");
        }

        if(cart.getCoupon() != null && !cart.getCoupon().isEmpty() && couponService.isValid(cart.getCoupon())){
            model.addAttribute("couponCode", cart.getCoupon());
            model.addAttribute("couponIsValid", true);
            model.addAttribute("couponDiscountedValue", cartService.getCouponDiscountedValue(cart.getId(), cart.getCoupon()));
        }else{
            model.addAttribute("couponCode", "");
        }

        BigDecimal orderTotal = cartService.getTotalAfterCouponAndShipping(cart.getId());
        model.addAttribute("orderTotal", orderTotal);

        cartService.sortCartByBookTitle(cart);

        return "cart";
    }


    @RequestMapping(value = "/increaseQuantity", method = RequestMethod.POST)
    public String increaseQuantity(@RequestParam Long lineItemId) {
        LineItem lineItem = lineItemService.getLineItemById(lineItemId);
        lineItemService.updateQuantity(lineItemId, lineItem.getQuantity() + 1);
        return "redirect:/cart";
    }


    @RequestMapping(value = "/decreaseQuantity", method = RequestMethod.POST)
    public String decreaseQuantity(@RequestParam Long lineItemId) {
        LineItem lineItem = lineItemService.getLineItemById(lineItemId);
        if(lineItem.getQuantity() == 1) {
            cartService.deleteLineItemById(lineItemId);
            return "redirect:/cart";
        }
        lineItemService.updateQuantity(lineItemId, lineItem.getQuantity() - 1);
        return "redirect:/cart";
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String addToCart(@RequestParam Long bookId) {

        if(userService.isLoggedIn()) {
            Long userId = userService.getLoggedInUser().getId();
            Cart cart = cartService.getOrCreateCart(userId);
            cartService.addToCart(cart.getId(), bookId);
        } else{
            return "redirect:/login";
        }

        return "redirect:/book/" + bookId;
    }


    @RequestMapping(value = "/applyCoupon", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> applyCoupon(@RequestParam String couponCode, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        if(!userService.isLoggedIn()){
            // redirect to login page
            response.put("status", "redirect:/login");
        }

        boolean isCouponValid = couponService.isValid(couponCode);

        if(isCouponValid){
            Cart cart = cartService.getOrCreateCart(userService.getLoggedInUser().getId());
            cartService.applyCoupon(cart.getId(), couponCode);
            response.put("couponIsValid",true);
            session.setAttribute("couponCode", couponCode);
        }else{
            session.removeAttribute("couponCode");
            response.put("couponIsValid", "false");
        }

        return response;
    }

    @RequestMapping(value = "/chooseShipping", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> chooseShippingMethod(@RequestParam String shippingMethod, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        if(!userService.isLoggedIn()){
            // redirect to login page
            response.put("status", "redirect:/login");
        }

        Cart cart = cartService.getOrCreateCart(userService.getLoggedInUser().getId());
        cartService.updateShipping(cart.getId(), shippingMethod);
        session.setAttribute("shippingMethod", shippingMethod);

        response.put("shippingMethod", shippingMethod);
        return response;
    }


}
