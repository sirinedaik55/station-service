package com.station.api.controller;

import com.station.api.security.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Authentification & Sécurité RBAC", description = "Connexion utilisateur et émission de tokens JWT avec rôles (ROLE_GERANT, ROLE_POMPISTE, ROLE_COMPTABLE)")
public class AuthController {

    private final JwtUtils jwtUtils;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthResponse {
        private String token;
        private String username;
        private String nomComplet;
        private String role;
        private String typeToken;
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion utilisateur", description = "Authentifie un utilisateur de la station et retourne un token JWT porteur de son rôle.")
    @ApiResponse(responseCode = "200", description = "Authentification réussie")
    @ApiResponse(responseCode = "401", description = "Identifiants invalides")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String u = request.getUsername();
        String p = request.getPassword();

        // Comptes de service certifiés pour la station Agil Lafrane
        if ("gerant".equalsIgnoreCase(u) && "gerant123".equals(p)) {
            String token = jwtUtils.generateToken("gerant", "ROLE_GERANT", "Gérant de Station (Admin)");
            return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .username("gerant")
                .nomComplet("Gérant de Station (Admin)")
                .role("ROLE_GERANT")
                .typeToken("Bearer")
                .build());
        }

        if ("pompiste".equalsIgnoreCase(u) && "pompiste123".equals(p)) {
            String token = jwtUtils.generateToken("pompiste", "ROLE_POMPISTE", "Chef de Piste (Service Pistolets & Lavage)");
            return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .username("pompiste")
                .nomComplet("Chef de Piste (Pompiste)")
                .role("ROLE_POMPISTE")
                .typeToken("Bearer")
                .build());
        }

        if ("comptable".equalsIgnoreCase(u) && "comptable123".equals(p)) {
            String token = jwtUtils.generateToken("comptable", "ROLE_COMPTABLE", "Responsable Facturation & Achats B2B");
            return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .username("comptable")
                .nomComplet("Responsable Facturation B2B")
                .role("ROLE_COMPTABLE")
                .typeToken("Bearer")
                .build());
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Identifiant ou mot de passe incorrect");
    }
}
