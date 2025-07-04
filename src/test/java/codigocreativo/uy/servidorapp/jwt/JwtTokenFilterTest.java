package codigocreativo.uy.servidorapp.jwt;

import codigocreativo.uy.servidorapp.dtos.FuncionalidadDto;
import codigocreativo.uy.servidorapp.dtos.PerfilDto;
import codigocreativo.uy.servidorapp.filtros.JwtTokenFilter;
import codigocreativo.uy.servidorapp.servicios.FuncionalidadRemote;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.security.Key;
import java.util.Base64;
import java.util.List;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import codigocreativo.uy.servidorapp.jwt.SecretKeyUtil;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtTokenFilterTest {

    @Mock
    private ContainerRequestContext requestContext;

    @Mock
    private UriInfo uriInfo;

    private JwtTokenFilter jwtTokenFilter;
    private FuncionalidadRemote funcionalidadService;

    private Key key;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        jwtTokenFilter = new JwtTokenFilter();
        funcionalidadService = mock(FuncionalidadRemote.class);
        
        Field field = JwtTokenFilter.class.getDeclaredField("funcionalidadService");
        field.setAccessible(true);
        field.set(jwtTokenFilter, funcionalidadService);
        
        when(requestContext.getUriInfo()).thenReturn(uriInfo);


        // Use the same secret key mechanism as the production code to avoid
        // mismatches when generating test tokens
        key = Keys.hmacShaKeyFor(SecretKeyUtil.getSecretKeyBytes());

    }

    @Test
    void testFilter_PublicEndpoint_NoAuthenticationRequired() {
        when(uriInfo.getPath()).thenReturn("/usuarios/login");

        jwtTokenFilter.filter(requestContext);

        verify(requestContext, never()).abortWith(any(Response.class));
    }

    @Test
    void testFilter_NoAuthorizationHeader_ShouldAbortWithUnauthorized() {
        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        when(uriInfo.getPath()).thenReturn("/equipos/crear");

        jwtTokenFilter.filter(requestContext);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(captor.capture());
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), captor.getValue().getStatus());
    }

    @Test
    void testFilter_InvalidAuthorizationHeader_ShouldAbortWithUnauthorized() {
        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn("InvalidToken");
        when(uriInfo.getPath()).thenReturn("/equipos/crear");

        jwtTokenFilter.filter(requestContext);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(captor.capture());
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), captor.getValue().getStatus());
    }

    @Test
    void testFilter_ValidTokenWithMissingClaims_ShouldAbortWithUnauthorized() {
        String token = Jwts.builder()
                .setSubject("testUser")
                .signWith(key)
                .compact();

        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(uriInfo.getPath()).thenReturn("/usuarios/listar");

        jwtTokenFilter.filter(requestContext);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(captor.capture());
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), captor.getValue().getStatus());
    }

    @Test
    void testFilter_PublicEndpointSwagger_ShouldPass() {
        when(uriInfo.getPath()).thenReturn("/swagger-ui");

        jwtTokenFilter.filter(requestContext);

        verify(requestContext, never()).abortWith(any(Response.class));
    }

    @Test
    void testFilter_ValidTokenWithEmptyEmail_ShouldAbortWithUnauthorized() {
        String token = Jwts.builder()
                .setSubject("testUser")
                .claim("perfil", "Aux administrativo")
                .claim("email", "")
                .signWith(key)
                .compact();

        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(uriInfo.getPath()).thenReturn("/usuarios/listar");

        jwtTokenFilter.filter(requestContext);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(captor.capture());
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), captor.getValue().getStatus());
    }

    @Test
    void testFilter_ValidTokenWithEmptyPerfil_ShouldAbortWithUnauthorized() {
        String token = Jwts.builder()
                .setSubject("testUser")
                .claim("perfil", "")
                .claim("email", "test@example.com")
                .signWith(key)
                .compact();

        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(uriInfo.getPath()).thenReturn("/usuarios/listar");

        jwtTokenFilter.filter(requestContext);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(captor.capture());
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), captor.getValue().getStatus());
    }

    @Test
    void testFilter_ValidTokenWithProperPermissions_ShouldPass() {
        String token = Jwts.builder()
                .setSubject("testUser")
                .claim("perfil", "Aux administrativo")
                .claim("email", "test@example.com")
                .signWith(key)
                .compact();

        // Crear una funcionalidad que coincida con el path y tenga el perfil correcto
        FuncionalidadDto funcionalidad = new FuncionalidadDto();
        funcionalidad.setRuta("/usuarios/listar");
        PerfilDto perfil = new PerfilDto();
        perfil.setNombrePerfil("Aux administrativo");
        funcionalidad.setPerfiles(List.of(perfil));

        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(uriInfo.getPath()).thenReturn("/usuarios/listar");
        when(funcionalidadService.obtenerTodas()).thenReturn(List.of(funcionalidad));

        jwtTokenFilter.filter(requestContext);

        verify(requestContext, never()).abortWith(any(Response.class));
    }

    @Test
    void testFilter_ValidTokenWithInvalidPermissions_ShouldAbortWithForbidden() {
        String token = Jwts.builder()
                .setSubject("testUser")
                .claim("perfil", "Aux administrativo")
                .claim("email", "test@example.com")
                .signWith(key)
                .compact();

        // Crear una funcionalidad que no coincida con el path o no tenga el perfil correcto
        FuncionalidadDto funcionalidad = new FuncionalidadDto();
        funcionalidad.setRuta("/other/path");
        PerfilDto perfil = new PerfilDto();
        perfil.setNombrePerfil("Otro perfil");
        funcionalidad.setPerfiles(List.of(perfil));

        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(uriInfo.getPath()).thenReturn("/usuarios/listar");
        when(funcionalidadService.obtenerTodas()).thenReturn(List.of(funcionalidad));

        jwtTokenFilter.filter(requestContext);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(captor.capture());
        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), captor.getValue().getStatus());
    }

    @Test
    void testFilter_InvalidTokenSignature_ShouldAbortWithUnauthorized() {
        Key invalidKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode("YW5vdGhlcl9zZWNyZXRfa2V5X2Zvcl90ZXN0aW5nX3B1cnBvc2VzX29ubHk="));
        String token = Jwts.builder()
                .setSubject("testUser")
                .claim("userId", "123")
                .claim("perfil", "Aux administrativo")
                .claim("email", "test@example.com")
                .claim("permisos", List.of("/usuarios/listar"))
                .signWith(invalidKey)
                .compact();

        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(uriInfo.getPath()).thenReturn("/usuarios/listar");

        jwtTokenFilter.filter(requestContext);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(captor.capture());
        Response response = captor.getValue();
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Token inválido"));
    }

    @Test
    void testFilter_ExpiredToken_ShouldAbortWithUnauthorized() {
        String token = Jwts.builder()
                .setSubject("testUser")
                .claim("userId", "123")
                .claim("perfil", "Aux administrativo")
                .claim("email", "test@example.com")
                .claim("permisos", List.of("/usuarios/listar"))
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000))
                .setExpiration(new Date(System.currentTimeMillis() - 500))
                .signWith(key)
                .compact();

        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(uriInfo.getPath()).thenReturn("/usuarios/listar");

        jwtTokenFilter.filter(requestContext);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(captor.capture());
        Response response = captor.getValue();
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Token inválido"));
    }

    @Test
    void testFilter_PermissionCheckWithDetailedLogging() {
        // Crear un token válido para administrador
        String token = Jwts.builder()
                .setSubject("testUser")
                .claim("perfil", "Administrador")
                .claim("email", "test@example.com")
                .signWith(key)
                .compact();

        // Crear una funcionalidad que coincida con el path
        FuncionalidadDto funcionalidad = new FuncionalidadDto();
        funcionalidad.setRuta("/usuarios/modificar");
        PerfilDto perfil = new PerfilDto();
        perfil.setNombrePerfil("Administrador");
        funcionalidad.setPerfiles(List.of(perfil));

        // Configurar el mock del servicio de funcionalidades
        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(uriInfo.getPath()).thenReturn("/usuarios/modificar");
        when(funcionalidadService.obtenerTodas()).thenReturn(List.of(funcionalidad));

        // Ejecutar el filtro
        jwtTokenFilter.filter(requestContext);

        // Verificar que no se abortó la petición para administrador
        verify(requestContext, never()).abortWith(any(Response.class));

        // Crear un token con perfil Aux administrativo
        String tokenWithDifferentProfile = Jwts.builder()
                .setSubject("testUser")
                .claim("perfil", "Aux administrativo")
                .claim("email", "test@example.com")
                .signWith(key)
                .compact();

        // Configurar el mock para el segundo caso
        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + tokenWithDifferentProfile);

        // Ejecutar el filtro nuevamente
        jwtTokenFilter.filter(requestContext);

        // Verificar que se abortó la petición con FORBIDDEN para Aux administrativo
        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(captor.capture());
        Response response = captor.getValue();
        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("No tiene permisos para modificar usuarios"));
    }

    @Test
    void testFilter_EmptySecretKeyEnv_UsesDefaultSecret() throws Exception {
        setEnv("SECRET_KEY", "");
        JwtTokenFilter filter = new JwtTokenFilter();
        Field field = JwtTokenFilter.class.getDeclaredField("funcionalidadService");
        field.setAccessible(true);
        field.set(filter, funcionalidadService);

        Key defaultKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SecretKeyUtil.DEFAULT_SECRET_KEY));
        String token = Jwts.builder()
                .setSubject("testUser")
                .claim("perfil", "Aux administrativo")
                .claim("email", "test@example.com")
                .signWith(defaultKey)
                .compact();

        FuncionalidadDto funcionalidad = new FuncionalidadDto();
        funcionalidad.setRuta("/usuarios/listar");
        PerfilDto perfil = new PerfilDto();
        perfil.setNombrePerfil("Aux administrativo");
        funcionalidad.setPerfiles(List.of(perfil));

        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(uriInfo.getPath()).thenReturn("/usuarios/listar");
        when(funcionalidadService.obtenerTodas()).thenReturn(List.of(funcionalidad));

        filter.filter(requestContext);

        verify(requestContext, never()).abortWith(any(Response.class));
    }

    @Test
    void testFilter_MalformedSecretKeyEnv_UsesDefaultSecret() throws Exception {
        setEnv("SECRET_KEY", "%%%not_base64%%%");
        JwtTokenFilter filter = new JwtTokenFilter();
        Field field = JwtTokenFilter.class.getDeclaredField("funcionalidadService");
        field.setAccessible(true);
        field.set(filter, funcionalidadService);

        Key defaultKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SecretKeyUtil.DEFAULT_SECRET_KEY));
        String token = Jwts.builder()
                .setSubject("testUser")
                .claim("perfil", "Aux administrativo")
                .claim("email", "test@example.com")
                .signWith(defaultKey)
                .compact();

        FuncionalidadDto funcionalidad = new FuncionalidadDto();
        funcionalidad.setRuta("/usuarios/listar");
        PerfilDto perfil = new PerfilDto();
        perfil.setNombrePerfil("Aux administrativo");
        funcionalidad.setPerfiles(List.of(perfil));

        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(uriInfo.getPath()).thenReturn("/usuarios/listar");
        when(funcionalidadService.obtenerTodas()).thenReturn(List.of(funcionalidad));

        filter.filter(requestContext);

        verify(requestContext, never()).abortWith(any(Response.class));
    }

    private static void setEnv(String key, String value) throws Exception {
        try {
            Class<?> pe = Class.forName("java.lang.ProcessEnvironment");
            Field f = pe.getDeclaredField("theEnvironment");
            f.setAccessible(true);
            @SuppressWarnings("unchecked") Map<String, String> env = (Map<String, String>) f.get(null);
            env.put(key, value);
            Field f2 = pe.getDeclaredField("theCaseInsensitiveEnvironment");
            f2.setAccessible(true);
            @SuppressWarnings("unchecked") Map<String, String> cienv = (Map<String, String>) f2.get(null);
            if (cienv != null) {
                cienv.put(key, value);
            }
        } catch (NoSuchFieldException ignore) {
            @SuppressWarnings("unchecked") Map<String, String> env = System.getenv();
            for (Class<?> cl : Collections.class.getDeclaredClasses()) {
                if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    @SuppressWarnings("unchecked") Map<String, String> map = (Map<String, String>) obj;
                    map.put(key, value);
                }
            }
        }
    }

}
