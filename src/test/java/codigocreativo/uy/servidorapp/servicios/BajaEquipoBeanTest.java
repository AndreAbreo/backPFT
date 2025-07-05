package codigocreativo.uy.servidorapp.servicios;

import codigocreativo.uy.servidorapp.dtos.BajaEquipoDto;
import codigocreativo.uy.servidorapp.dtos.UsuarioDto;
import codigocreativo.uy.servidorapp.dtos.dtomappers.BajaEquipoMapper;
import codigocreativo.uy.servidorapp.dtos.dtomappers.CycleAvoidingMappingContext;
import codigocreativo.uy.servidorapp.entidades.BajaEquipo;
import codigocreativo.uy.servidorapp.excepciones.ServiciosException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class BajaEquipoBeanTest {

    @Mock
    private EntityManager em;

    @Mock
    private BajaEquipoMapper bajaEquipoMapper;

    @Mock
    private UsuarioRemote usuarioRemote;

    @Mock
    private EquipoRemote equipoRemote;

    @InjectMocks
    private BajaEquipoBean bajaEquipoBean;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        bajaEquipoBean = new BajaEquipoBean();

        // Inyectar el EntityManager usando reflexión
        Field emField = BajaEquipoBean.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(bajaEquipoBean, em);

        // Inyectar el UsuarioRemote usando reflexión
        Field usuarioRemoteField = BajaEquipoBean.class.getDeclaredField("usuarioRemote");
        usuarioRemoteField.setAccessible(true);
        usuarioRemoteField.set(bajaEquipoBean, usuarioRemote);

        // Inyectar el EquipoRemote usando reflexión
        Field equipoRemoteField = BajaEquipoBean.class.getDeclaredField("equipoRemote");
        equipoRemoteField.setAccessible(true);
        equipoRemoteField.set(bajaEquipoBean, equipoRemote);

        Field mapperField = BajaEquipoBean.class.getDeclaredField("bajaEquipoMapper");
        mapperField.setAccessible(true);
        mapperField.set(bajaEquipoBean, bajaEquipoMapper);
    }

    @Test
    void testCrearBajaEquipo_Success() {
        BajaEquipoDto bajaEquipoDto = crearBajaEquipoDtoValido();
        BajaEquipo bajaEquipoEntity = new BajaEquipo();
        UsuarioDto usuario = new UsuarioDto();
        usuario.setEmail("test@test.com");

        when(usuarioRemote.findUserByEmail("test@test.com")).thenReturn(usuario);
        when(bajaEquipoMapper.toEntity(eq(bajaEquipoDto), any(CycleAvoidingMappingContext.class))).thenReturn(bajaEquipoEntity);
        doNothing().when(equipoRemote).eliminarEquipo(any(BajaEquipoDto.class));

        assertDoesNotThrow(() -> bajaEquipoBean.crearBajaEquipo(bajaEquipoDto, "test@test.com"));

        verify(usuarioRemote).findUserByEmail("test@test.com");
        verify(em).persist(bajaEquipoEntity);
        verify(em).flush();
        verify(equipoRemote).eliminarEquipo(bajaEquipoDto);
    }

    @Test
    void testCrearBajaEquipo_WithNullBajaEquipo() {
        assertThrows(ServiciosException.class, () -> bajaEquipoBean.crearBajaEquipo(null, "test@test.com"));
        verify(em, never()).persist(any());
        verify(em, never()).flush();
    }

    @Test
    void testCrearBajaEquipo_WithNullRazon() {
        BajaEquipoDto bajaEquipoDto = crearBajaEquipoDtoValido();
        bajaEquipoDto.setRazon(null);

        ServiciosException exception = assertThrows(ServiciosException.class, 
            () -> bajaEquipoBean.crearBajaEquipo(bajaEquipoDto, "test@test.com"));
        
        assertEquals("La razón de la baja es obligatoria", exception.getMessage());
        verify(em, never()).persist(any());
        verify(em, never()).flush();
    }

    @Test
    void testCrearBajaEquipo_WithEmptyRazon() {
        BajaEquipoDto bajaEquipoDto = crearBajaEquipoDtoValido();
        bajaEquipoDto.setRazon("   ");

        ServiciosException exception = assertThrows(ServiciosException.class, 
            () -> bajaEquipoBean.crearBajaEquipo(bajaEquipoDto, "test@test.com"));
        
        assertEquals("La razón de la baja es obligatoria", exception.getMessage());
        verify(em, never()).persist(any());
        verify(em, never()).flush();
    }

    @Test
    void testCrearBajaEquipo_WithNullFecha() {
        BajaEquipoDto bajaEquipoDto = crearBajaEquipoDtoValido();
        bajaEquipoDto.setFecha(null);
        BajaEquipo bajaEquipoEntity = new BajaEquipo();
        UsuarioDto usuario = new UsuarioDto();
        usuario.setEmail("test@test.com");

        when(usuarioRemote.findUserByEmail("test@test.com")).thenReturn(usuario);
        when(bajaEquipoMapper.toEntity(any(BajaEquipoDto.class), any(CycleAvoidingMappingContext.class))).thenReturn(bajaEquipoEntity);
        doNothing().when(equipoRemote).eliminarEquipo(any(BajaEquipoDto.class));

        assertDoesNotThrow(() -> bajaEquipoBean.crearBajaEquipo(bajaEquipoDto, "test@test.com"));

        // Verificar que se estableció la fecha por defecto
        assertNotNull(bajaEquipoDto.getFecha());
        verify(em).persist(bajaEquipoEntity);
        verify(em).flush();
        verify(equipoRemote).eliminarEquipo(bajaEquipoDto);
    }

    @Test
    void testCrearBajaEquipo_WithNullEquipo() {
        BajaEquipoDto bajaEquipoDto = crearBajaEquipoDtoValido();
        bajaEquipoDto.setIdEquipo(null);

        ServiciosException exception = assertThrows(ServiciosException.class, 
            () -> bajaEquipoBean.crearBajaEquipo(bajaEquipoDto, "test@test.com"));
        
        assertEquals("El equipo es obligatorio", exception.getMessage());
        verify(em, never()).persist(any());
        verify(em, never()).flush();
    }

    @Test
    void testCrearBajaEquipo_WithNullEmailUsuario() {
        BajaEquipoDto bajaEquipoDto = crearBajaEquipoDtoValido();

        ServiciosException exception = assertThrows(ServiciosException.class, 
            () -> bajaEquipoBean.crearBajaEquipo(bajaEquipoDto, null));
        
        assertEquals("No se pudo obtener el usuario de la sesión", exception.getMessage());
        verify(em, never()).persist(any());
        verify(em, never()).flush();
    }

    @Test
    void testCrearBajaEquipo_WithEmptyEmailUsuario() {
        BajaEquipoDto bajaEquipoDto = crearBajaEquipoDtoValido();

        ServiciosException exception = assertThrows(ServiciosException.class, 
            () -> bajaEquipoBean.crearBajaEquipo(bajaEquipoDto, "   "));
        
        assertEquals("No se pudo obtener el usuario de la sesión", exception.getMessage());
        verify(em, never()).persist(any());
        verify(em, never()).flush();
    }

    @Test
    void testCrearBajaEquipo_WithUsuarioNotFound() {
        BajaEquipoDto bajaEquipoDto = crearBajaEquipoDtoValido();

        when(usuarioRemote.findUserByEmail("test@test.com")).thenReturn(null);

        ServiciosException exception = assertThrows(ServiciosException.class, 
            () -> bajaEquipoBean.crearBajaEquipo(bajaEquipoDto, "test@test.com"));
        
        assertEquals("Usuario no encontrado con el email: test@test.com", exception.getMessage());
        verify(usuarioRemote).findUserByEmail("test@test.com");
        verify(em, never()).persist(any());
        verify(em, never()).flush();
    }

    @Test
    void testCrearBajaEquipo_WithDefaultEstado() {
        BajaEquipoDto bajaEquipoDto = crearBajaEquipoDtoValido();
        bajaEquipoDto.setEstado(null);
        BajaEquipo bajaEquipoEntity = new BajaEquipo();
        UsuarioDto usuario = new UsuarioDto();
        usuario.setEmail("test@test.com");

        when(usuarioRemote.findUserByEmail("test@test.com")).thenReturn(usuario);
        when(bajaEquipoMapper.toEntity(any(BajaEquipoDto.class), any(CycleAvoidingMappingContext.class))).thenReturn(bajaEquipoEntity);
        doNothing().when(equipoRemote).eliminarEquipo(any(BajaEquipoDto.class));

        assertDoesNotThrow(() -> bajaEquipoBean.crearBajaEquipo(bajaEquipoDto, "test@test.com"));

        // Verificar que se estableció el estado por defecto
        assertEquals("INACTIVO", bajaEquipoDto.getEstado());
        verify(em).persist(bajaEquipoEntity);
        verify(em).flush();
        verify(equipoRemote).eliminarEquipo(bajaEquipoDto);
    }

    @Test
    void testCrearBajaEquipo_WithDefaultFecha() {
        BajaEquipoDto bajaEquipoDto = crearBajaEquipoDtoValido();
        bajaEquipoDto.setFecha(null);
        BajaEquipo bajaEquipoEntity = new BajaEquipo();
        UsuarioDto usuario = new UsuarioDto();
        usuario.setEmail("test@test.com");

        when(usuarioRemote.findUserByEmail("test@test.com")).thenReturn(usuario);
        when(bajaEquipoMapper.toEntity(any(BajaEquipoDto.class), any(CycleAvoidingMappingContext.class))).thenReturn(bajaEquipoEntity);
        doNothing().when(equipoRemote).eliminarEquipo(any(BajaEquipoDto.class));

        assertDoesNotThrow(() -> bajaEquipoBean.crearBajaEquipo(bajaEquipoDto, "test@test.com"));

        // Verificar que se estableció la fecha por defecto
        assertNotNull(bajaEquipoDto.getFecha());
        verify(em).persist(bajaEquipoEntity);
        verify(em).flush();
        verify(equipoRemote).eliminarEquipo(bajaEquipoDto);
    }

    @Test
    void testObtenerBajasEquipos() {
        List<BajaEquipo> bajasEntityList = Collections.singletonList(new BajaEquipo());
        List<BajaEquipoDto> bajasDtoList = Collections.singletonList(new BajaEquipoDto());

        @SuppressWarnings("unchecked")
        jakarta.persistence.TypedQuery<BajaEquipo> mockedQuery = mock(jakarta.persistence.TypedQuery.class);
        when(em.createQuery("SELECT be FROM BajaEquipo be ORDER BY be.fecha DESC", BajaEquipo.class)).thenReturn(mockedQuery);
        when(mockedQuery.getResultList()).thenReturn(bajasEntityList);
        when(bajaEquipoMapper.toDto(anyList(), any(CycleAvoidingMappingContext.class))).thenReturn(bajasDtoList);

        List<BajaEquipoDto> result = bajaEquipoBean.obtenerBajasEquipos();

        assertEquals(bajasDtoList, result);
        verify(em).createQuery("SELECT be FROM BajaEquipo be ORDER BY be.fecha DESC", BajaEquipo.class);
    }

    @Test
    void testObtenerBajaEquipo() {
        Long id = 1L;
        BajaEquipo bajaEquipoEntity = new BajaEquipo();
        BajaEquipoDto bajaEquipoDto = new BajaEquipoDto();

        when(em.find(BajaEquipo.class, id)).thenReturn(bajaEquipoEntity);
        when(bajaEquipoMapper.toDto(eq(bajaEquipoEntity), any(CycleAvoidingMappingContext.class))).thenReturn(bajaEquipoDto);

        BajaEquipoDto result = bajaEquipoBean.obtenerBajaEquipo(id);

        assertEquals(bajaEquipoDto, result);
        verify(em).find(BajaEquipo.class, id);
    }

    @Test
    void testObtenerBajaEquipo_WithNullId() {
        BajaEquipoDto result = bajaEquipoBean.obtenerBajaEquipo(null);
        assertNull(result);
        verify(em, never()).find(any(), any());
    }

    @Test
    void testObtenerBajaEquipo_NotFound() {
        Long id = 1L;
        when(em.find(BajaEquipo.class, id)).thenReturn(null);

        BajaEquipoDto result = bajaEquipoBean.obtenerBajaEquipo(id);

        assertNull(result);
        verify(em).find(BajaEquipo.class, id);
    }

    @Test
    void testCrearBajaEquipo_WithDuplicateEquipo() throws Exception {
        BajaEquipoDto bajaEquipoDto = crearBajaEquipoDtoValido();
        UsuarioDto usuario = new UsuarioDto();
        usuario.setEmail("test@test.com");

        when(usuarioRemote.findUserByEmail("test@test.com")).thenReturn(usuario);
        
        // Mockear la consulta para simular que ya existe una baja
        jakarta.persistence.TypedQuery<Long> mockQuery = mock(jakarta.persistence.TypedQuery.class);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.getSingleResult()).thenReturn(1L);
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(mockQuery);

        ServiciosException exception = assertThrows(ServiciosException.class, 
            () -> bajaEquipoBean.crearBajaEquipo(bajaEquipoDto, "test@test.com"));
        
        assertEquals("El equipo ya ha sido dado de baja anteriormente", exception.getMessage());
        verify(em, never()).persist(any());
        verify(em, never()).flush();
        verify(equipoRemote, never()).eliminarEquipo(any());
    }

    /**
     * Método auxiliar para crear un BajaEquipoDto válido con todos los campos obligatorios
     */
    private BajaEquipoDto crearBajaEquipoDtoValido() {
        BajaEquipoDto bajaEquipoDto = new BajaEquipoDto();
        bajaEquipoDto.setRazon("Equipo obsoleto");
        bajaEquipoDto.setFecha(LocalDate.now());
        bajaEquipoDto.setIdEquipo(new codigocreativo.uy.servidorapp.dtos.EquipoDto());
        bajaEquipoDto.setEstado("ACTIVO");
        bajaEquipoDto.setComentarios("Equipo en mal estado");
        return bajaEquipoDto;
    }
}
