package codigocreativo.uy.servidorapp.servicios;

import codigocreativo.uy.servidorapp.dtos.dtomappers.PerfilMapper;
import codigocreativo.uy.servidorapp.dtos.PerfilDto;
import codigocreativo.uy.servidorapp.entidades.Perfil;
import codigocreativo.uy.servidorapp.enumerados.Estados;
import codigocreativo.uy.servidorapp.excepciones.ServiciosException;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class PerfilBeanTest {

    @Mock
    private EntityManager em;

    @Mock
    private PerfilMapper perfilMapper;

    @InjectMocks
    private PerfilBean perfilBean;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        perfilBean = new PerfilBean();

        // Use reflection to set the private EntityManager field
        Field emField = PerfilBean.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(perfilBean, em);

        Field mapperField = PerfilBean.class.getDeclaredField("perfilMapper");
        mapperField.setAccessible(true);
        mapperField.set(perfilBean, perfilMapper);
    }

    @Test
    void testCrearPerfilExitoso() throws ServiciosException {
        PerfilDto perfilDto = new PerfilDto();
        perfilDto.setNombrePerfil("Admin");
        perfilDto.setEstado(Estados.ACTIVO);
        Perfil perfilEntity = new Perfil();
        perfilEntity.setId(1L);
        perfilEntity.setNombrePerfil("Admin");
        perfilEntity.setEstado(Estados.ACTIVO);

        @SuppressWarnings("unchecked")
        TypedQuery<Perfil> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Perfil.class))).thenReturn(query);
        when(query.setParameter(anyString(), anyString())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());
        when(perfilMapper.toEntity(perfilDto)).thenReturn(perfilEntity);
        when(perfilMapper.toDto(perfilEntity)).thenReturn(perfilDto);

        PerfilDto resultado = perfilBean.crearPerfil(perfilDto);

        verify(em, times(1)).persist(perfilEntity);
        verify(em, times(1)).flush();
        assertEquals(Estados.ACTIVO, perfilDto.getEstado());
        assertNotNull(resultado);
        assertEquals(perfilDto, resultado);
    }

    @Test
    void testCrearPerfilNull() {
        ServiciosException exception = assertThrows(ServiciosException.class, () -> {
            perfilBean.crearPerfil(null);
        });
        assertEquals("El perfil no puede ser null", exception.getMessage());
    }

    @Test
    void testCrearPerfilNombreNull() {
        PerfilDto perfilDto = new PerfilDto();
        perfilDto.setNombrePerfil(null);

        ServiciosException exception = assertThrows(ServiciosException.class, () -> {
            perfilBean.crearPerfil(perfilDto);
        });
        assertEquals("El nombre del perfil es obligatorio", exception.getMessage());
    }

    @Test
    void testCrearPerfilNombreVacio() {
        PerfilDto perfilDto = new PerfilDto();
        perfilDto.setNombrePerfil("");

        ServiciosException exception = assertThrows(ServiciosException.class, () -> {
            perfilBean.crearPerfil(perfilDto);
        });
        assertEquals("El nombre del perfil es obligatorio", exception.getMessage());
    }

    @Test
    void testCrearPerfilNombreSoloEspacios() {
        PerfilDto perfilDto = new PerfilDto();
        perfilDto.setNombrePerfil("   ");

        ServiciosException exception = assertThrows(ServiciosException.class, () -> {
            perfilBean.crearPerfil(perfilDto);
        });
        assertEquals("El nombre del perfil es obligatorio", exception.getMessage());
    }

    @Test
    void testCrearPerfilNombreDuplicado() {
        PerfilDto perfilDto = new PerfilDto();
        perfilDto.setNombrePerfil("Admin");
        Perfil perfilExistente = new Perfil();
        perfilExistente.setNombrePerfil("Admin");

        @SuppressWarnings("unchecked")
        TypedQuery<Perfil> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Perfil.class))).thenReturn(query);
        when(query.setParameter(anyString(), anyString())).thenReturn(query);
        when(query.getResultList()).thenReturn(Arrays.asList(perfilExistente));

        ServiciosException exception = assertThrows(ServiciosException.class, () -> {
            perfilBean.crearPerfil(perfilDto);
        });
        assertEquals("Ya existe un perfil con el nombre: Admin", exception.getMessage());
    }

    @Test
    void testCrearPerfilNombreDuplicadoCaseInsensitive() {
        PerfilDto perfilDto = new PerfilDto();
        perfilDto.setNombrePerfil("admin");
        Perfil perfilExistente = new Perfil();
        perfilExistente.setNombrePerfil("Admin");

        @SuppressWarnings("unchecked")
        TypedQuery<Perfil> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Perfil.class))).thenReturn(query);
        when(query.setParameter(anyString(), anyString())).thenReturn(query);
        when(query.getResultList()).thenReturn(Arrays.asList(perfilExistente));

        ServiciosException exception = assertThrows(ServiciosException.class, () -> {
            perfilBean.crearPerfil(perfilDto);
        });
        assertEquals("Ya existe un perfil con el nombre: admin", exception.getMessage());
    }

    @Test
    void testModificarPerfilExitoso() throws ServiciosException {
        PerfilDto perfilDto = new PerfilDto();
        perfilDto.setId(1L);
        perfilDto.setNombrePerfil("Admin Modificado");
        perfilDto.setEstado(Estados.ACTIVO);
        
        Perfil perfilExistente = new Perfil();
        perfilExistente.setId(1L);
        perfilExistente.setNombrePerfil("Admin Original");
        perfilExistente.setEstado(Estados.ACTIVO);

        when(em.find(Perfil.class, 1L)).thenReturn(perfilExistente);
        
        @SuppressWarnings("unchecked")
        TypedQuery<Perfil> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Perfil.class))).thenReturn(query);
        when(query.setParameter(anyString(), anyString())).thenReturn(query);
        when(query.setParameter(anyString(), anyLong())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        perfilBean.modificarPerfil(perfilDto);

        verify(em, times(1)).find(Perfil.class, 1L);
        verify(em, times(1)).merge(perfilExistente);
        verify(em, times(1)).flush();
        assertEquals("Admin Modificado", perfilExistente.getNombrePerfil());
    }

    @Test
    void testModificarPerfilNull() {
        ServiciosException exception = assertThrows(ServiciosException.class, () -> {
            perfilBean.modificarPerfil(null);
        });
        assertEquals("El perfil no puede ser null", exception.getMessage());
    }

    @Test
    void testModificarPerfilSinId() {
        PerfilDto perfilDto = new PerfilDto();
        perfilDto.setNombrePerfil("Admin");

        ServiciosException exception = assertThrows(ServiciosException.class, () -> {
            perfilBean.modificarPerfil(perfilDto);
        });
        assertEquals("El ID del perfil es obligatorio para la modificación", exception.getMessage());
    }

    @Test
    void testModificarPerfilNombreNull() {
        PerfilDto perfilDto = new PerfilDto();
        perfilDto.setId(1L);
        perfilDto.setNombrePerfil(null);

        ServiciosException exception = assertThrows(ServiciosException.class, () -> {
            perfilBean.modificarPerfil(perfilDto);
        });
        assertEquals("El nombre del perfil es obligatorio", exception.getMessage());
    }

    @Test
    void testModificarPerfilNoEncontrado() {
        PerfilDto perfilDto = new PerfilDto();
        perfilDto.setId(999L);
        perfilDto.setNombrePerfil("Admin");

        when(em.find(Perfil.class, 999L)).thenReturn(null);

        ServiciosException exception = assertThrows(ServiciosException.class, () -> {
            perfilBean.modificarPerfil(perfilDto);
        });
        assertEquals("Perfil no encontrado con ID: 999", exception.getMessage());
    }

    @Test
    void testModificarPerfilNombreDuplicado() {
        PerfilDto perfilDto = new PerfilDto();
        perfilDto.setId(1L);
        perfilDto.setNombrePerfil("Admin Duplicado");
        
        Perfil perfilExistente = new Perfil();
        perfilExistente.setId(1L);
        perfilExistente.setNombrePerfil("Admin Original");
        
        Perfil perfilOtro = new Perfil();
        perfilOtro.setId(2L);
        perfilOtro.setNombrePerfil("Admin Duplicado");

        when(em.find(Perfil.class, 1L)).thenReturn(perfilExistente);
        
        @SuppressWarnings("unchecked")
        TypedQuery<Perfil> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Perfil.class))).thenReturn(query);
        when(query.setParameter(anyString(), anyString())).thenReturn(query);
        when(query.setParameter(anyString(), anyLong())).thenReturn(query);
        when(query.getResultList()).thenReturn(Arrays.asList(perfilOtro));

        ServiciosException exception = assertThrows(ServiciosException.class, () -> {
            perfilBean.modificarPerfil(perfilDto);
        });
        assertEquals("Ya existe un perfil con el nombre: Admin Duplicado", exception.getMessage());
    }

    @Test
    void testModificarPerfil() throws ServiciosException {
        PerfilDto perfilDto = new PerfilDto();
        perfilDto.setId(1L);
        perfilDto.setNombrePerfil("Admin Modificado");
        perfilDto.setEstado(Estados.ACTIVO);
        
        Perfil perfilExistente = new Perfil();
        perfilExistente.setId(1L);
        perfilExistente.setNombrePerfil("Admin Original");
        perfilExistente.setEstado(Estados.ACTIVO);

        when(em.find(Perfil.class, 1L)).thenReturn(perfilExistente);
        
        @SuppressWarnings("unchecked")
        TypedQuery<Perfil> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Perfil.class))).thenReturn(query);
        when(query.setParameter(anyString(), anyString())).thenReturn(query);
        when(query.setParameter(anyString(), anyLong())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        perfilBean.modificarPerfil(perfilDto);

        verify(em, times(1)).merge(perfilExistente);
        verify(em, times(1)).flush();
    }

    @Test
    void testEliminarPerfilExitoso() throws ServiciosException {
        PerfilDto perfilDto = new PerfilDto();
        perfilDto.setId(1L);
        perfilDto.setNombrePerfil("Admin");
        perfilDto.setEstado(Estados.ACTIVO);
        
        Perfil perfilExistente = new Perfil();
        perfilExistente.setId(1L);
        perfilExistente.setNombrePerfil("Admin");
        perfilExistente.setEstado(Estados.ACTIVO);

        when(em.find(Perfil.class, 1L)).thenReturn(perfilExistente);

        perfilBean.eliminarPerfil(perfilDto);

        verify(em, times(1)).find(Perfil.class, 1L);
        verify(em, times(1)).merge(perfilExistente);
        verify(em, times(1)).flush();
        assertEquals(Estados.INACTIVO, perfilExistente.getEstado());
    }

    @Test
    void testEliminarPerfilNull() {
        ServiciosException exception = assertThrows(ServiciosException.class, () -> {
            perfilBean.eliminarPerfil(null);
        });
        assertEquals("El perfil no puede ser null", exception.getMessage());
    }

    @Test
    void testEliminarPerfilSinId() {
        PerfilDto perfilDto = new PerfilDto();
        perfilDto.setNombrePerfil("Admin");

        ServiciosException exception = assertThrows(ServiciosException.class, () -> {
            perfilBean.eliminarPerfil(perfilDto);
        });
        assertEquals("El ID del perfil es obligatorio para la eliminación", exception.getMessage());
    }

    @Test
    void testEliminarPerfilNoEncontrado() {
        PerfilDto perfilDto = new PerfilDto();
        perfilDto.setId(999L);
        perfilDto.setNombrePerfil("Admin");

        when(em.find(Perfil.class, 999L)).thenReturn(null);

        ServiciosException exception = assertThrows(ServiciosException.class, () -> {
            perfilBean.eliminarPerfil(perfilDto);
        });
        assertEquals("Perfil no encontrado con ID: 999", exception.getMessage());
    }

    @Test
    void testEliminarPerfil() throws ServiciosException {
        PerfilDto perfilDto = new PerfilDto();
        perfilDto.setId(1L);
        perfilDto.setNombrePerfil("Admin");
        perfilDto.setEstado(Estados.ACTIVO);
        
        Perfil perfilExistente = new Perfil();
        perfilExistente.setId(1L);
        perfilExistente.setNombrePerfil("Admin");
        perfilExistente.setEstado(Estados.ACTIVO);

        when(em.find(Perfil.class, 1L)).thenReturn(perfilExistente);

        perfilBean.eliminarPerfil(perfilDto);

        assertEquals(Estados.INACTIVO, perfilExistente.getEstado());
        verify(em, times(1)).merge(perfilExistente);
    }

    @Test
    void testObtenerPerfil() {
        Long id = 1L;
        Perfil perfilEntity = new Perfil();
        PerfilDto perfilDto = new PerfilDto();

        when(em.find(Perfil.class, id)).thenReturn(perfilEntity);
        when(perfilMapper.toDto(perfilEntity)).thenReturn(perfilDto);

        PerfilDto result = perfilBean.obtenerPerfil(id);

        assertEquals(perfilDto, result);
        verify(em, times(1)).find(Perfil.class, id);
    }

    @Test
    void testObtenerPerfiles() {
        List<Perfil> perfilesEntity = Arrays.asList(new Perfil(), new Perfil());
        List<PerfilDto> perfilesDto = Arrays.asList(new PerfilDto(), new PerfilDto());

        @SuppressWarnings("unchecked")
        TypedQuery<Perfil> query = mock(TypedQuery.class);

        when(em.createQuery("SELECT p FROM Perfil p", Perfil.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(perfilesEntity);
        when(perfilMapper.toDtoList(perfilesEntity)).thenReturn(perfilesDto);

        List<PerfilDto> result = perfilBean.obtenerPerfiles();

        assertEquals(perfilesDto, result);
        verify(em, times(1)).createQuery("SELECT p FROM Perfil p", Perfil.class);
        verify(query, times(1)).getResultList();
    }

    @Test
    void testListarPerfilesPorNombre() {
        String nombre = "Admin";
        List<Perfil> perfilesEntity = Arrays.asList(new Perfil(), new Perfil());
        List<PerfilDto> perfilesDto = Arrays.asList(new PerfilDto(), new PerfilDto());

        @SuppressWarnings("unchecked")
        TypedQuery<Perfil> query = mock(TypedQuery.class);

        when(em.createQuery("SELECT p FROM Perfil p WHERE p.nombrePerfil LIKE :nombre", Perfil.class)).thenReturn(query);
        when(query.setParameter("nombre", "%" + nombre + "%")).thenReturn(query);
        when(query.getResultList()).thenReturn(perfilesEntity);
        when(perfilMapper.toDtoList(perfilesEntity)).thenReturn(perfilesDto);

        List<PerfilDto> result = perfilBean.listarPerfilesPorNombre(nombre);

        assertEquals(perfilesDto, result);
    }

    @Test
    void testListarPerfilesPorEstado() {
        Estados estado = Estados.ACTIVO;
        List<Perfil> perfilesEntity = Arrays.asList(new Perfil(), new Perfil());
        List<PerfilDto> perfilesDto = Arrays.asList(new PerfilDto(), new PerfilDto());

        @SuppressWarnings("unchecked")
        TypedQuery<Perfil> query = mock(TypedQuery.class);

        when(em.createQuery("SELECT p FROM Perfil p WHERE p.estado = :estado", Perfil.class)).thenReturn(query);
        when(query.setParameter("estado", estado)).thenReturn(query);
        when(query.getResultList()).thenReturn(perfilesEntity);
        when(perfilMapper.toDtoList(perfilesEntity)).thenReturn(perfilesDto);

        List<PerfilDto> result = perfilBean.listarPerfilesPorEstado(estado);

        assertEquals(perfilesDto, result);
    }
}