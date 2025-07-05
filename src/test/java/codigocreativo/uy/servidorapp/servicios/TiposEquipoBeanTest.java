package codigocreativo.uy.servidorapp.servicios;

import codigocreativo.uy.servidorapp.dtos.TiposEquipoDto;
import codigocreativo.uy.servidorapp.dtos.dtomappers.TiposEquipoMapper;
import codigocreativo.uy.servidorapp.entidades.TiposEquipo;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TiposEquipoBeanTest {

    @Mock
    private EntityManager em;

    @Mock
    private TiposEquipoMapper tiposEquipoMapper;

    @InjectMocks
    private TiposEquipoBean tiposEquipoBean;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        tiposEquipoBean = new TiposEquipoBean();

        // Inyectar el EntityManager usando reflexión
        Field emField = TiposEquipoBean.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(tiposEquipoBean, em);

        Field mapperField = TiposEquipoBean.class.getDeclaredField("tiposEquipoMapper");
        mapperField.setAccessible(true);
        mapperField.set(tiposEquipoBean, tiposEquipoMapper);
    }

    @Test
    void testCrearTiposEquipo() {
        TiposEquipoDto tiposEquipoDto = new TiposEquipoDto();
        TiposEquipo tiposEquipoEntity = new TiposEquipo();

        when(tiposEquipoMapper.toEntity(tiposEquipoDto)).thenReturn(tiposEquipoEntity);

        tiposEquipoBean.crearTiposEquipo(tiposEquipoDto);

        verify(em).persist(tiposEquipoEntity);
        verify(em).flush();
    }

    @Test
    void testModificarTiposEquipo() {
        TiposEquipoDto tiposEquipoDto = new TiposEquipoDto();
        TiposEquipo tiposEquipoEntity = new TiposEquipo();

        when(tiposEquipoMapper.toEntity(tiposEquipoDto)).thenReturn(tiposEquipoEntity);

        tiposEquipoBean.modificarTiposEquipo(tiposEquipoDto);

        verify(em).merge(tiposEquipoEntity);
        verify(em).flush();
    }

    @Test
    void testEliminarTiposEquipo() {
        Long id = 1L;
        TiposEquipo tiposEquipoEntity = new TiposEquipo();
        tiposEquipoEntity.setEstado("ACTIVO");

        when(em.find(TiposEquipo.class, id)).thenReturn(tiposEquipoEntity);

        tiposEquipoBean.eliminarTiposEquipo(id);

        assertEquals("INACTIVO", tiposEquipoEntity.getEstado());
        verify(em).merge(tiposEquipoEntity);
    }

    @Test
    void testObtenerPorId() {
        Long id = 1L;
        TiposEquipo tiposEquipoEntity = new TiposEquipo();
        TiposEquipoDto tiposEquipoDto = new TiposEquipoDto();

        when(em.find(TiposEquipo.class, id)).thenReturn(tiposEquipoEntity);
        when(tiposEquipoMapper.toDto(tiposEquipoEntity)).thenReturn(tiposEquipoDto);

        TiposEquipoDto result = tiposEquipoBean.obtenerPorId(id);

        assertEquals(tiposEquipoDto, result);
    }

    @Test
    void testListarTiposEquipo() {
        List<TiposEquipo> tiposEquiposEntity = Collections.singletonList(new TiposEquipo());
        List<TiposEquipoDto> tiposEquiposDto = Collections.singletonList(new TiposEquipoDto());

        @SuppressWarnings("unchecked")
        TypedQuery<TiposEquipo> query = mock(TypedQuery.class);
        when(em.createQuery("SELECT t FROM TiposEquipo t", TiposEquipo.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(tiposEquiposEntity);
        when(tiposEquipoMapper.toDto(tiposEquiposEntity)).thenReturn(tiposEquiposDto);

        List<TiposEquipoDto> result = tiposEquipoBean.listarTiposEquipo();

        assertEquals(tiposEquiposDto, result);
        verify(query).getResultList();
    }
}
