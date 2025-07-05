package codigocreativo.uy.servidorapp.servicios;

import codigocreativo.uy.servidorapp.dtos.IntervencionDto;
import codigocreativo.uy.servidorapp.dtos.dtomappers.CycleAvoidingMappingContext;
import codigocreativo.uy.servidorapp.dtos.dtomappers.IntervencionMapper;
import codigocreativo.uy.servidorapp.entidades.Intervencion;
import codigocreativo.uy.servidorapp.entidades.TiposIntervencione;
import codigocreativo.uy.servidorapp.excepciones.ServiciosException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class IntervencionBeanTest {

    @Mock
    private EntityManager em;

    @Mock
    private IntervencionMapper intervencionMapper;

    @InjectMocks
    private IntervencionBean intervencionBean;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        intervencionBean = new IntervencionBean();

        // Inyectar el EntityManager usando reflexión
        Field emField = IntervencionBean.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(intervencionBean, em);

        Field mapperField = IntervencionBean.class.getDeclaredField("intervencionMapper");
        mapperField.setAccessible(true);
        mapperField.set(intervencionBean, intervencionMapper);
    }

    @Test
    void testCrearIntervencion() throws ServiciosException {
        IntervencionDto intervencionDto = new IntervencionDto();
        Intervencion intervencionEntity = new Intervencion();

        when(intervencionMapper.toEntity(eq(intervencionDto), any(CycleAvoidingMappingContext.class))).thenReturn(intervencionEntity);

        intervencionBean.crear(intervencionDto);

        verify(em).persist(intervencionEntity);
    }

    @Test
    void testActualizarIntervencion() throws ServiciosException {
        IntervencionDto intervencionDto = new IntervencionDto();
        Intervencion intervencionEntity = new Intervencion();

        when(intervencionMapper.toEntity(eq(intervencionDto), any(CycleAvoidingMappingContext.class))).thenReturn(intervencionEntity);

        intervencionBean.actualizar(intervencionDto);

        verify(em).merge(intervencionEntity);
    }

    @Test
    void testObtenerTodas() throws ServiciosException {
        Intervencion intervencionEntity = new Intervencion();
        List<Intervencion> intervenciones = Collections.singletonList(intervencionEntity);

        @SuppressWarnings("unchecked")
        TypedQuery<Intervencion> query = mock(TypedQuery.class);
        when(em.createQuery("SELECT i FROM Intervencion i", Intervencion.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(intervenciones);
        when(intervencionMapper.toDto(eq(intervenciones), any(CycleAvoidingMappingContext.class))).thenReturn(Collections.singletonList(new IntervencionDto()));

        List<IntervencionDto> result = intervencionBean.obtenerTodas();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testBuscarId() throws ServiciosException {
        Long id = 1L;
        Intervencion intervencionEntity = new Intervencion();
        IntervencionDto intervencionDto = new IntervencionDto();

        when(em.find(Intervencion.class, id)).thenReturn(intervencionEntity);
        when(intervencionMapper.toDto(eq(intervencionEntity), any(CycleAvoidingMappingContext.class))).thenReturn(intervencionDto);

        IntervencionDto result = intervencionBean.buscarId(id);

        assertNotNull(result);
    }

    @Test
    void testObtenerPorRangoDeFecha() throws ServiciosException {
        LocalDateTime fechaDesde = LocalDateTime.now().minusDays(1);
        LocalDateTime fechaHasta = LocalDateTime.now();
        Long idEquipo = 1L;
        Intervencion intervencionEntity = new Intervencion();
        List<Intervencion> intervenciones = Collections.singletonList(intervencionEntity);

        @SuppressWarnings("unchecked")
        TypedQuery<Intervencion> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Intervencion.class))).thenReturn(query);
        when(query.setParameter("fechaDesde", fechaDesde)).thenReturn(query);
        when(query.setParameter("fechaHasta", fechaHasta)).thenReturn(query);
        when(query.setParameter("idEquipo", idEquipo)).thenReturn(query);
        when(query.getResultList()).thenReturn(intervenciones);
        when(intervencionMapper.toDto(eq(intervenciones), any(CycleAvoidingMappingContext.class))).thenReturn(Collections.singletonList(new IntervencionDto()));

        List<IntervencionDto> result = intervencionBean.obtenerPorRangoDeFecha(fechaDesde, fechaHasta, idEquipo);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testObtenerCantidadPorTipo() throws ServiciosException {
        LocalDateTime fechaDesde = LocalDateTime.now().minusDays(1);
        LocalDateTime fechaHasta = LocalDateTime.now();
        Long idTipo = 1L;
        Intervencion intervencionEntity = new Intervencion();
        TiposIntervencione tipoIntervencion = new TiposIntervencione();
        tipoIntervencion.setNombreTipo("Mantenimiento");
        intervencionEntity.setIdTipo(tipoIntervencion);
        List<Intervencion> intervenciones = Collections.singletonList(intervencionEntity);

        @SuppressWarnings("unchecked")
        TypedQuery<Intervencion> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Intervencion.class))).thenReturn(query);
        when(query.setParameter("fechaDesde", fechaDesde)).thenReturn(query);
        when(query.setParameter("fechaHasta", fechaHasta)).thenReturn(query);
        when(query.setParameter("idTipo", idTipo)).thenReturn(query);
        when(query.getResultList()).thenReturn(intervenciones);

        Map<String, Long> result = intervencionBean.obtenerCantidadPorTipo(fechaDesde, fechaHasta, idTipo);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get("Mantenimiento"));
    }
}
