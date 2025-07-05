package codigocreativo.uy.servidorapp.servicios;

import codigocreativo.uy.servidorapp.dtos.TiposIntervencioneDto;
import codigocreativo.uy.servidorapp.dtos.dtomappers.TiposIntervencioneMapper;
import codigocreativo.uy.servidorapp.entidades.TiposIntervencione;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TipoIntervencioneBeanTest {

    @Mock
    private EntityManager em;

    @Mock
    private TiposIntervencioneMapper tiposIntervencioneMapper;

    @InjectMocks
    private TipoIntervencioneBean tipoIntervencioneBean;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        tipoIntervencioneBean = new TipoIntervencioneBean();

        // Use reflection to set the private EntityManager field
        Field emField = TipoIntervencioneBean.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(tipoIntervencioneBean, em);

        Field mapperField = TipoIntervencioneBean.class.getDeclaredField("tiposIntervencioneMapper");
        mapperField.setAccessible(true);
        mapperField.set(tipoIntervencioneBean, tiposIntervencioneMapper);
    }

    @Test
    void testObtenerTiposIntervenciones() {
        List<TiposIntervencione> tiposIntervenciones = new ArrayList<>();
        tiposIntervenciones.add(new TiposIntervencione()); // Add a mock entity to the list
        List<TiposIntervencioneDto> tiposIntervencionesDto = new ArrayList<>();
        @SuppressWarnings("unchecked")
        TypedQuery<TiposIntervencione> query = mock(TypedQuery.class);

        when(em.createQuery("SELECT t FROM TiposIntervencione t WHERE t.estado = 'ACTIVO'", TiposIntervencione.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(tiposIntervenciones);
        when(tiposIntervencioneMapper.toDto(tiposIntervenciones)).thenReturn(tiposIntervencionesDto);

        List<TiposIntervencioneDto> result = tipoIntervencioneBean.obtenerTiposIntervenciones();

        verify(em, times(1)).createQuery("SELECT t FROM TiposIntervencione t WHERE t.estado = 'ACTIVO'", TiposIntervencione.class);
        verify(query, times(1)).getResultList();
        verify(tiposIntervencioneMapper, times(1)).toDto(tiposIntervenciones);
        assertEquals(tiposIntervencionesDto, result);
    }

    @Test
    void testObtenerTipoIntervencion() {
        Long id = 1L;
        TiposIntervencione tipoIntervencion = new TiposIntervencione();
        TiposIntervencioneDto tipoIntervencionDto = new TiposIntervencioneDto();

        when(em.find(TiposIntervencione.class, id)).thenReturn(tipoIntervencion);
        when(tiposIntervencioneMapper.toDto(tipoIntervencion)).thenReturn(tipoIntervencionDto);

        TiposIntervencioneDto result = tipoIntervencioneBean.obtenerTipoIntervencion(id);

        verify(em, times(1)).find(TiposIntervencione.class, id);
        verify(tiposIntervencioneMapper, times(1)).toDto(tipoIntervencion);
        assertEquals(tipoIntervencionDto, result);
    }

    @Test
    void testCrearTipoIntervencion() {
        TiposIntervencioneDto tipoIntervencionDto = new TiposIntervencioneDto();
        TiposIntervencione tipoIntervencionEntity = new TiposIntervencione();

        when(tiposIntervencioneMapper.toEntity(tipoIntervencionDto)).thenReturn(tipoIntervencionEntity);

        tipoIntervencioneBean.crearTipoIntervencion(tipoIntervencionDto);

        verify(tiposIntervencioneMapper, times(1)).toEntity(tipoIntervencionDto);
        verify(em, times(1)).persist(tipoIntervencionEntity);
    }

    @Test
    void testModificarTipoIntervencion() {
        TiposIntervencioneDto tipoIntervencionDto = new TiposIntervencioneDto();
        TiposIntervencione tipoIntervencionEntity = new TiposIntervencione();

        when(tiposIntervencioneMapper.toEntity(tipoIntervencionDto)).thenReturn(tipoIntervencionEntity);

        tipoIntervencioneBean.modificarTipoIntervencion(tipoIntervencionDto);

        verify(tiposIntervencioneMapper, times(1)).toEntity(tipoIntervencionDto);
        verify(em, times(1)).merge(tipoIntervencionEntity);
    }

    @Test
    void testEliminarTipoIntervencion() {
        Long id = 1L;
        Query query = mock(Query.class);

        when(em.createQuery("UPDATE TiposIntervencione t SET t.estado = 'INACTIVO' WHERE t.id = :id")).thenReturn(query);
        when(query.setParameter("id", id)).thenReturn(query);

        tipoIntervencioneBean.eliminarTipoIntervencion(id);

        verify(em, times(1)).createQuery("UPDATE TiposIntervencione t SET t.estado = 'INACTIVO' WHERE t.id = :id");
        verify(query, times(1)).setParameter("id", id);
        verify(query, times(1)).executeUpdate();
    }
}