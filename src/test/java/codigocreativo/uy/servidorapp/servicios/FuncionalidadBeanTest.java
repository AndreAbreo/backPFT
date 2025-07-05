package codigocreativo.uy.servidorapp.servicios;

import codigocreativo.uy.servidorapp.dtos.FuncionalidadDto;
import codigocreativo.uy.servidorapp.dtos.dtomappers.FuncionalidadMapper;
import codigocreativo.uy.servidorapp.dtos.dtomappers.CycleAvoidingMappingContext;
import codigocreativo.uy.servidorapp.dtos.PerfilDto;
import codigocreativo.uy.servidorapp.entidades.Funcionalidad;
import codigocreativo.uy.servidorapp.entidades.FuncionalidadesPerfiles;
import codigocreativo.uy.servidorapp.entidades.Perfil;
import codigocreativo.uy.servidorapp.excepciones.ServiciosException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FuncionalidadBeanTest {

    @Mock
    private EntityManager em;

    @Mock
    private FuncionalidadMapper funcionalidadMapper;

    @InjectMocks
    private FuncionalidadBean funcionalidadBean;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        funcionalidadBean = new FuncionalidadBean();

        // Inyectar el EntityManager usando reflexión
        Field emField = FuncionalidadBean.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(funcionalidadBean, em);

        Field mapperField = FuncionalidadBean.class.getDeclaredField("funcionalidadMapper");
        mapperField.setAccessible(true);
        mapperField.set(funcionalidadBean, funcionalidadMapper);
    }

    @Test
    void testObtenerTodas() {
        List<Funcionalidad> funcionalidades = Collections.singletonList(new Funcionalidad());
        List<FuncionalidadDto> funcionalidadesDto = Collections.singletonList(new FuncionalidadDto());

        @SuppressWarnings("unchecked")
        TypedQuery<Funcionalidad> query = mock(TypedQuery.class);
        when(em.createQuery("SELECT f FROM Funcionalidad f ORDER BY f.ruta", Funcionalidad.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(funcionalidades);
        when(funcionalidadMapper.toDto(anyList(), any(CycleAvoidingMappingContext.class))).thenReturn(funcionalidadesDto);

        List<FuncionalidadDto> result = funcionalidadBean.obtenerTodas();

        assertEquals(funcionalidadesDto, result);
        verify(em).createQuery("SELECT f FROM Funcionalidad f ORDER BY f.ruta", Funcionalidad.class);
        verify(query).getResultList();
    }

    @Test
    void testCrear() {
        Funcionalidad funcionalidad = new Funcionalidad();
        FuncionalidadDto funcionalidadDto = new FuncionalidadDto();

        when(funcionalidadMapper.toEntity(any(FuncionalidadDto.class), any(CycleAvoidingMappingContext.class))).thenReturn(funcionalidad);
        when(funcionalidadMapper.toDto(any(Funcionalidad.class), any(CycleAvoidingMappingContext.class))).thenReturn(funcionalidadDto);

        FuncionalidadDto result = funcionalidadBean.crear(funcionalidadDto);

        verify(em).persist(funcionalidad);
        verify(em).flush();
        assertEquals(funcionalidadDto, result);
    }

    @Test
    void testActualizar() {
        Funcionalidad funcionalidadExistente = new Funcionalidad();
        funcionalidadExistente.setId(1L);
        Funcionalidad funcionalidadActualizada = new Funcionalidad();
        funcionalidadActualizada.setId(1L);

        FuncionalidadDto funcionalidadDto = new FuncionalidadDto();
        funcionalidadDto.setId(1L);

        PerfilDto perfilDto = new PerfilDto();
        perfilDto.setId(2L);

        funcionalidadDto.setPerfiles(Collections.singletonList(perfilDto));

        Perfil perfil = new Perfil();
        perfil.setId(2L);

        when(em.find(Funcionalidad.class, funcionalidadDto.getId())).thenReturn(funcionalidadExistente);
        when(funcionalidadMapper.toEntity(any(FuncionalidadDto.class), any(CycleAvoidingMappingContext.class))).thenReturn(funcionalidadActualizada);
        when(em.find(Perfil.class, perfilDto.getId())).thenReturn(perfil);
        when(funcionalidadMapper.toDto(any(Funcionalidad.class), any(CycleAvoidingMappingContext.class))).thenReturn(funcionalidadDto);

        FuncionalidadDto result = funcionalidadBean.actualizar(funcionalidadDto);

        assertNotNull(result);
        assertEquals(funcionalidadDto, result);
        verify(em).merge(funcionalidadActualizada);
        verify(em).flush();
    }

    @Test
    void testEliminar_Success() throws ServiciosException {
        Funcionalidad funcionalidad = new Funcionalidad();
        funcionalidad.setId(1L);
        funcionalidad.setFuncionalidadesPerfiles(Collections.emptyList());

        when(em.find(Funcionalidad.class, funcionalidad.getId())).thenReturn(funcionalidad);

        assertDoesNotThrow(() -> funcionalidadBean.eliminar(funcionalidad.getId()));

        verify(em).remove(funcionalidad);
        verify(em).flush();
    }

    @Test
    void testEliminar_WithNullId() {
        assertThrows(ServiciosException.class, () -> funcionalidadBean.eliminar(null));
        verify(em, never()).remove(any());
        verify(em, never()).flush();
    }

    @Test
    void testEliminar_FuncionalidadNotFound() {
        Long id = 999L;
        when(em.find(Funcionalidad.class, id)).thenReturn(null);

        assertThrows(ServiciosException.class, () -> funcionalidadBean.eliminar(id));
        verify(em, never()).remove(any());
        verify(em, never()).flush();
    }

    @Test
    void testEliminar_WithAssociatedProfiles() {
        Funcionalidad funcionalidad = new Funcionalidad();
        funcionalidad.setId(1L);
        
        // Crear una lista con perfiles asociados
        FuncionalidadesPerfiles perfilAsociado = new FuncionalidadesPerfiles();
        funcionalidad.setFuncionalidadesPerfiles(Collections.singletonList(perfilAsociado));

        when(em.find(Funcionalidad.class, funcionalidad.getId())).thenReturn(funcionalidad);

        ServiciosException exception = assertThrows(ServiciosException.class, 
            () -> funcionalidadBean.eliminar(funcionalidad.getId()));
        
        assertEquals("No se puede eliminar la funcionalidad porque tiene perfiles asociados", exception.getMessage());
        verify(em, never()).remove(any());
        verify(em, never()).flush();
    }

    @Test
    void testBuscarPorId() {
        Funcionalidad funcionalidad = new Funcionalidad();
        funcionalidad.setId(1L);
        FuncionalidadDto funcionalidadDto = new FuncionalidadDto();
        funcionalidadDto.setId(1L);

        when(em.find(Funcionalidad.class, funcionalidad.getId())).thenReturn(funcionalidad);
        when(funcionalidadMapper.toDto(any(Funcionalidad.class), any(CycleAvoidingMappingContext.class))).thenReturn(funcionalidadDto);

        FuncionalidadDto result = funcionalidadBean.buscarPorId(funcionalidad.getId());

        assertEquals(funcionalidadDto, result);
        verify(em).find(Funcionalidad.class, funcionalidad.getId());
    }
}
