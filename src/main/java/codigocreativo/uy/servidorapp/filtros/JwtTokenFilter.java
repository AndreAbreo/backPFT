package codigocreativo.uy.servidorapp.filtros;

import java.security.Key;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Optional;

import jakarta.annotation.Priority;
import jakarta.ejb.EJB;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import codigocreativo.uy.servidorapp.servicios.FuncionalidadRemote;
import codigocreativo.uy.servidorapp.dtos.FuncionalidadDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import codigocreativo.uy.servidorapp.jwt.SecretKeyUtil;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtTokenFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = Logger.getLogger(JwtTokenFilter.class.getName());

    private static final String ERROR_JSON_FORMAT = "{\"error\":\"%s\"}";

    public static class TokenValidationException extends Exception {
        public TokenValidationException(String message) {
            super(message);
        }
    }

    private static final Set<String> PUBLIC_PATHS = Set.of(
        "/usuarios/login",
        "/usuarios/google-login",
        "/perfiles/listar",
        "/swagger-ui",
        "/usuarios/crear",
        "/menu",
        "/api/login",
        "/api/openapi.json",
        "/api/swagger-ui",
        "/openapi.json",
        "/swagger-ui/index.html"
    );

    @EJB
    private FuncionalidadRemote funcionalidadService;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String disableAuth = Optional.ofNullable(System.getenv("DISABLE_JWT_AUTH"))
                .orElse("false");

        if ("true".equalsIgnoreCase(disableAuth)) {
            LOGGER.warning("JWT authentication disabled via DISABLE_JWT_AUTH");
            return;
        }

        String path = requestContext.getUriInfo().getPath();
        LOGGER.info("Procesando petición para el path: " + path);

        // Verificar si es un endpoint público
        if (isPublicEndpoint(path)) {
            LOGGER.info("Endpoint público detectado, permitiendo acceso sin autenticación");
            return;
        }

        LOGGER.info("Endpoint requiere autenticación, procesando token JWT");
        // Procesar autenticación y autorización
        processAuthenticationAndAuthorization(requestContext, path);
    }

    private void processAuthenticationAndAuthorization(ContainerRequestContext requestContext, String path) {
        // Verificar autenticación
        String token = extractAndValidateToken(requestContext);
        if (token == null) {
            return; // La respuesta de error ya fue enviada
        }

        try {
            // Validar token y extraer claims
            Claims claims = validateToken(token);
            if (claims == null) {
                return;
            }

            // Procesar información del usuario
            processUserInfo(requestContext, claims, path);

        } catch (Exception e) {
            String errorMsg = "Error al procesar el token: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMsg, e);
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .entity(String.format(ERROR_JSON_FORMAT, errorMsg))
                .build());
        }
    }

    private void processUserInfo(ContainerRequestContext requestContext, Claims claims, String path) {
        // Extraer información del token
        String email = claims.get("email", String.class);
        String perfil = claims.get("perfil", String.class);
        LOGGER.log(Level.INFO, "Token válido para usuario: {0} con perfil: {1}", new Object[]{email, perfil});

        // Validar información requerida
        if (!isValidUserInfo(email, perfil)) {
            String errorMsg = "Token inválido: información de usuario incompleta - Email: " + 
                            (email == null ? "null" : email) + 
                            ", Perfil: " + (perfil == null ? "null" : perfil);
            LOGGER.warning(errorMsg);
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .entity(String.format(ERROR_JSON_FORMAT, errorMsg))
                .build());
            return;
        }

        // Almacenar información en el contexto
        storeUserInfo(requestContext, email, perfil);

        // Verificar permisos
        validatePermissions(requestContext, email, perfil, path);
    }

    private void validatePermissions(ContainerRequestContext requestContext, String email, String perfil, String path) {
        try {
            // Verificar permisos basados en el rol
            if (!hasPermission(perfil, path)) {
                String errorMsg = getPermissionErrorMessage(path, email, perfil);
                LOGGER.warning(errorMsg);
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                    .entity(String.format(ERROR_JSON_FORMAT, errorMsg))
                    .build());
            } else {
                LOGGER.log(Level.INFO, "Permisos validados correctamente para el usuario: {0}", email);
            }
        } catch (Exception e) {
            String errorMsg = "Error al verificar permisos: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMsg, e);
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .entity(String.format(ERROR_JSON_FORMAT, errorMsg))
                .build());
        }
    }

    private String getPermissionErrorMessage(String path, String email, String perfil) {
        if (path.equals("/usuarios/modificar")) {
            return "No tiene permisos para modificar usuarios. Solo los administradores pueden realizar esta acción.";
        } else {
            LOGGER.log(Level.WARNING, "Acceso denegado - Usuario: {0}, Perfil: {1}, Path: {2} - No tiene los permisos necesarios", 
                    new Object[]{email, perfil, path});
            return "Acceso denegado - No tiene los permisos necesarios";
        }
    }

    private String extractAndValidateToken(ContainerRequestContext requestContext) {
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            String errorMsg = "Token no proporcionado o formato inválido - Header: " + 
                            (authorizationHeader == null ? "null" : authorizationHeader);
            LOGGER.warning(errorMsg);
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .entity(String.format(ERROR_JSON_FORMAT, errorMsg))
                .build());
            return null;
        }

        return authorizationHeader.substring("Bearer".length()).trim();
    }

    protected Claims validateToken(String token) throws TokenValidationException {
        try {
            Key key = Keys.hmacShaKeyFor(SecretKeyUtil.getSecretKeyBytes());
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            String errorMsg = "Token inválido: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMsg, e);
            throw new TokenValidationException(errorMsg);
        }
    }

    protected boolean isValidUserInfo(String email, String perfil) {
        return  email != null && !email.isEmpty() &&
                perfil != null && !perfil.isEmpty();
    }

    private void storeUserInfo(ContainerRequestContext requestContext, 
                            String email, 
                            String perfil) {
        LOGGER.info("Almacenando información del usuario en el contexto - Email: " + email + ", Perfil: " + perfil);
        requestContext.setProperty("email", email);
        requestContext.setProperty("perfil", perfil);
        LOGGER.info("Información del usuario almacenada correctamente");
    }

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    protected boolean hasPermission(String perfil, String path) {
        if (perfil == null || perfil.isEmpty()) {
            return false;
        }

        // Verificar permisos en la base de datos
        List<FuncionalidadDto> funcionalidades = funcionalidadService.obtenerTodas();

        // Verificar si es una modificación de usuario
        if (path.equals("/usuarios/modificar")) {
            LOGGER.info("Verificando permisos para modificación de usuario");
            // Solo permitir que administradores modifiquen a otros administradores
            if (perfil.equals("Administrador")) {
                LOGGER.info("Usuario con perfil Administrador intentando modificar - Permitiendo acceso");
                return true;
            } else {
                LOGGER.log(Level.WARNING, "Usuario con perfil {0} intentando modificar - Acceso denegado", perfil);
                return false;
            }
        }

        boolean hasPermission = funcionalidades.stream()
                .anyMatch(funcionalidad -> {
                    boolean pathMatches = path.startsWith(funcionalidad.getRuta());
                    boolean profileMatches = funcionalidad.getPerfiles().stream()
                            .anyMatch(perfilDto -> perfilDto.getNombrePerfil().equals(perfil));
                    
                    if (pathMatches) {
                        LOGGER.info("Path coincide con funcionalidad: " + funcionalidad.getRuta());
                        LOGGER.info("Perfiles permitidos: " + funcionalidad.getPerfiles());
                    }
                    
                    return pathMatches && profileMatches;
                });

        if (!hasPermission) {
            LOGGER.log(Level.WARNING, "Permiso denegado - Perfil: {0}, Path: {1}", new Object[]{perfil, path});
        }

        return hasPermission;
    }
}
