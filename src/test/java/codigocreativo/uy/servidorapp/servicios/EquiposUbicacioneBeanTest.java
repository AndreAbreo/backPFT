package codigocreativo.uy.servidorapp.servicios;

import codigocreativo.uy.servidorapp.dtos.EquiposUbicacioneDto;
import codigocreativo.uy.servidorapp.dtos.dtomappers.CycleAvoidingMappingContext;
import codigocreativo.uy.servidorapp.dtos.dtomappers.EquiposUbicacioneMapper;
import codigocreativo.uy.servidorapp.entidades.EquiposUbicacione;
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

class EquiposUbicacioneBeanTest {

    @Mock
    private EntityManager em;

    @Mock
    private EquiposUbicacioneMapper equiposUbicacioneMapper;

    @InjectMocks
    private EquiposUbicacioneBean equiposUbicacioneBean;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        equiposUbicacioneBean = new EquiposUbicacioneBean();

        // Inyectar el EntityManager usando reflexión
        Field emField = EquiposUbicacioneBean.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(equiposUbicacioneBean, em);

        Field mapperField = EquiposUbicacioneBean.class.getDeclaredField("equiposUbicacioneMapper");
        mapperField.setAccessible(true);
        mapperField.set(equiposUbicacioneBean, equiposUbicacioneMapper);
    }

    @Test
    void testCrearEquiposUbicacione() {
        EquiposUbicacioneDto equiposUbicacioneDto = new EquiposUbicacioneDto();
        EquiposUbicacione equiposUbicacioneEntity = new EquiposUbicacione();

        when(equiposUbicacioneMapper.toEntity(eq(equiposUbicacioneDto), any(CycleAvoidingMappingContext.class)))
                .thenReturn(equiposUbicacioneEntity);

        equiposUbicacioneBean.crearEquiposUbicacione(equiposUbicacioneDto);

        verify(em).persist(equiposUbicacioneEntity);
        verify(em).flush();
    }

    @Test
    void testObtenerEquiposUbicacione() {
        List<EquiposUbicacione> equiposUbicacioneList = Collections.singletonList(new EquiposUbicacione());
        List<EquiposUbicacioneDto> equiposUbicacioneDtoList = Collections.singletonList(new EquiposUbicacioneDto());

        @SuppressWarnings("unchecked")
        TypedQuery<EquiposUbicacione> query = mock(TypedQuery.class);
        when(em.createQuery("SELECT equiposUbicacione FROM EquiposUbicacione equiposUbicacione", EquiposUbicacione.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(equiposUbicacioneList);
        when(equiposUbicacioneMapper.toDto(eq(equiposUbicacioneList), any(CycleAvoidingMappingContext.class)))
                .thenReturn(equiposUbicacioneDtoList);

        List<EquiposUbicacioneDto> result = equiposUbicacioneBean.obtenerEquiposUbicacione();

        assertEquals(equiposUbicacioneDtoList, result);
        verify(query).getResultList();
    }

    @Test
    void testObtenerEquiposUbicacionePorEquipo() {
        Long id = 1L;
        List<EquiposUbicacione> equiposUbicacioneList = Collections.singletonList(new EquiposUbicacione());
        List<EquiposUbicacioneDto> equiposUbicacioneDtoList = Collections.singletonList(new EquiposUbicacioneDto());

        @SuppressWarnings("unchecked")
        TypedQuery<EquiposUbicacione> query = mock(TypedQuery.class);
        when(em.createQuery("SELECT equiposUbicacione FROM EquiposUbicacione equiposUbicacione WHERE equiposUbicacione.idEquipo.id = :id", EquiposUbicacione.class)).thenReturn(query);
        when(query.setParameter("id", id)).thenReturn(query);
        when(query.getResultList()).thenReturn(equiposUbicacioneList);
        when(equiposUbicacioneMapper.toDto(eq(equiposUbicacioneList), any(CycleAvoidingMappingContext.class)))
                .thenReturn(equiposUbicacioneDtoList);

        List<EquiposUbicacioneDto> result = equiposUbicacioneBean.obtenerEquiposUbicacionePorEquipo(id);

        assertEquals(equiposUbicacioneDtoList, result);
        verify(query).setParameter("id", id);
        verify(query).getResultList();
    }
}
