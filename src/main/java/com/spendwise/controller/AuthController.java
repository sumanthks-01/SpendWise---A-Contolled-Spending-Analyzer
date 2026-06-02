package com.spendwise.controller;

import com.spendwise.model.User;
import com.spendwise.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.spendwise.service.EmailService;

/**
 * Handles login page serving, signup form submission, and user info API.
 * Spring Security itself handles the actual POST /auth/login authentication.
 */
@Controller
public class AuthController {

    @Autowired private UserService userService;
    @Autowired private AuthenticationManager authenticationManager;

    /** Serve the login page (or redirect to home if already logged in). */
    @GetMapping("/login")
    public String loginPage(Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            return "redirect:/";
        }
        return "forward:/login.html";
    }

    /** Process signup form → create account → auto-login → redirect to app. */
    @PostMapping("/auth/signup")
    public String signup(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam("confirm_password") String confirmPassword,
            HttpServletRequest request,
            RedirectAttributes redirectAttrs) {

        // Validation
        if (name == null || name.isBlank()) {
            redirectAttrs.addFlashAttribute("signupError", "Name is required.");
            return "redirect:/login?mode=signup";
        }
        if (!password.equals(confirmPassword)) {
            redirectAttrs.addFlashAttribute("signupError", "Passwords do not match.");
            return "redirect:/login?mode=signup";
        }
        if (password.length() < 6) {
            redirectAttrs.addFlashAttribute("signupError", "Password must be at least 6 characters.");
            return "redirect:/login?mode=signup";
        }

        try {
            User user = userService.register(name.trim(), email.trim().toLowerCase(), password);

            // Auto-login after signup
            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(user.getEmail(), password);
            Authentication auth = authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(auth);

            return "redirect:/";
        } catch (IllegalArgumentException e) {
            redirectAttrs.addFlashAttribute("signupError", e.getMessage());
            return "redirect:/login?mode=signup";
        }
    }

    /** Returns current user info for the sidebar (name, email, initials). */
    @GetMapping("/api/user/me")
    @ResponseBody
    public Object currentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return java.util.Map.of("error", "Not authenticated");
        }
        User user = userService.findByEmail(auth.getName());
        return java.util.Map.of(
                "name",    user.getName(),
                "email",   user.getEmail(),
                "initials", initials(user.getName())
        );
    }

    @Autowired private EmailService emailService;
    @org.springframework.beans.factory.annotation.Value("${app.base-url}")
    private String appBaseUrl;

    // ── Forgot Password ────────────────────────────────────────────────────────
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forward:/forgot-password.html";
    }

    @PostMapping("/auth/forgot-password")
    public String processForgotPassword(@RequestParam String email) {
        try {
            String token = userService.createPasswordResetToken(email.trim().toLowerCase());
            User user = userService.findByEmail(email.trim().toLowerCase());
            String resetLink = appBaseUrl + "/reset-password?token=" + token;
            
            // Send email
            emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), resetLink);
        } catch (Exception e) {
            // Ignore error or log it. We still return success to prevent email enumeration.
        }
        return "redirect:/forgot-password?success=true";
    }

    // ── Reset Password ────────────────────────────────────────────────────────
    @GetMapping("/reset-password")
    public String resetPasswordPage() {
        return "forward:/reset-password.html";
    }

    @PostMapping("/auth/reset-password")
    public String processResetPassword(
            @RequestParam String token,
            @RequestParam String password,
            @RequestParam("confirm_password") String confirmPassword) {
        
        if (!password.equals(confirmPassword)) {
            return "redirect:/reset-password?token=" + token + "&error=mismatch";
        }
        if (password.length() < 6) {
            return "redirect:/reset-password?token=" + token + "&error=short";
        }

        try {
            userService.resetPassword(token, password);
            return "redirect:/login?resetSuccess=true";
        } catch (IllegalArgumentException e) {
            // e.getMessage() could be "Invalid token.", "expired", etc.
            return "redirect:/reset-password?token=" + token + "&error=invalid";
        }
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
