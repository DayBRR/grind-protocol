package com.davidrr.grindprotocol.security.auth.controller;

import com.davidrr.security.auth.dto.*;
import com.davidrr.security.auth.exception.RefreshTokenMissingException;
import com.davidrr.security.auth.service.AuthService;
import com.davidrr.security.auth.util.FingerprintUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints de autenticación y gestión de sesiones")
public class AuthController {

    private final AuthService authService;

    @Value("${security.cookie.refresh-name:refresh_token}")
    private String refreshCookieName;

    @Value("${security.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${security.cookie.same-site:Lax}")
    private String cookieSameSite;

    @Value("${security.cookie.path:/auth}")
    private String cookiePath;

    @Value("${security.cookie.max-age-days:14}")
    private long cookieMaxAgeDays;

    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica usuario y contraseña, devuelve access token y establece refresh token en cookie HttpOnly."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login correcto",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "token": "eyJhbGciOiJIUzI1NiJ9..."
                                    }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    public ResponseEntity<AuthResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Credenciales de acceso",
                    content = @Content(
                            schema = @Schema(implementation = AuthRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "username": "admin",
                                      "password": "admin"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody AuthRequest request,
            @Parameter(hidden = true) HttpServletRequest httpRequest
    ) {
        SessionFingerprint fingerprint = new SessionFingerprint(
                FingerprintUtils.extractIp(httpRequest),
                FingerprintUtils.extractUserAgent(httpRequest)
        );

        AuthTokens tokens = authService.login(request, fingerprint);

        ResponseCookie refreshCookie = buildRefreshCookie(tokens.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new AuthResponse(tokens.accessToken()));
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refrescar sesión",
            description = "Genera un nuevo access token y rota el refresh token usando la cookie HttpOnly."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Refresh correcto",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Refresh token ausente, inválido, expirado o reutilizado")
    })
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request) {
        String refreshToken = extractCookie(request);

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RefreshTokenMissingException();
        }

        SessionFingerprint fingerprint = new SessionFingerprint(
                FingerprintUtils.extractIp(request),
                FingerprintUtils.extractUserAgent(request)
        );

        AuthTokens tokens = authService.refresh(refreshToken, fingerprint);

        ResponseCookie refreshCookie = buildRefreshCookie(tokens.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new AuthResponse(tokens.accessToken()));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Cerrar sesión actual",
            description = "Revoca el refresh token actual y elimina la cookie HttpOnly."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Logout correcto"),
            @ApiResponse(responseCode = "401", description = "Refresh token ausente o inválido")
    })
    public ResponseEntity<Void> logout(
            @Parameter(hidden = true) HttpServletRequest request
    ) {
        String refreshToken = extractCookie(request);

        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logout(refreshToken);
        }

        ResponseCookie deleteCookie = buildDeleteRefreshCookie();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }

    @PostMapping("/logout-all")
    @Operation(
            summary = "Cerrar todas las sesiones",
            description = "Revoca todas las sesiones activas del usuario y elimina la cookie actual."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Logout global correcto"),
            @ApiResponse(responseCode = "401", description = "Refresh token ausente o inválido")
    })
    public ResponseEntity<Void> logoutAll(
            @Parameter(hidden = true) HttpServletRequest request
    ) {
        String refreshToken = extractCookie(request);

        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logoutAll(refreshToken);
        }

        ResponseCookie deleteCookie = buildDeleteRefreshCookie();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }

    @GetMapping("/sessions")
    @Operation(
            summary = "Listar sesiones activas",
            description = "Devuelve las sesiones activas asociadas al usuario autenticado a partir del refresh token en cookie."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de sesiones activas",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = SessionResponse.class))
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Refresh token ausente, inválido o expirado")
    })
    public ResponseEntity<List<SessionResponse>> sessions(
            @Parameter(hidden = true) HttpServletRequest request
    ) {
        String refreshToken = extractCookie(request);

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RefreshTokenMissingException();
        }

        return ResponseEntity.ok(authService.getSessions(refreshToken));
    }

    @DeleteMapping("/sessions/{id}")
    @Operation(
            summary = "Cerrar una sesión concreta",
            description = "Revoca una sesión activa concreta del usuario autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sesión revocada correctamente"),
            @ApiResponse(responseCode = "401", description = "Refresh token ausente o inválido"),
            @ApiResponse(responseCode = "404", description = "Sesión no encontrada")
    })
    public ResponseEntity<Void> revokeSession(
            @CookieValue(name = "refresh_token") String refreshToken,
            @PathVariable("id") Long id
    ) {
        authService.revokeSession(refreshToken, id);
        return ResponseEntity.noContent().build();
    }

    private ResponseCookie buildRefreshCookie(String refreshTokenPlain) {
        return ResponseCookie.from(refreshCookieName, refreshTokenPlain)
                .httpOnly(true)
                .secure(cookieSecure)
                .path(cookiePath)
                .maxAge(Duration.ofDays(cookieMaxAgeDays))
                .sameSite(cookieSameSite)
                .build();
    }

    private ResponseCookie buildDeleteRefreshCookie() {
        return ResponseCookie.from(refreshCookieName, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path(cookiePath)
                .maxAge(0)
                .sameSite(cookieSameSite)
                .build();
    }

    private String extractCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        for (var cookie : request.getCookies()) {
            if (cookie.getName().equals(refreshCookieName)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}