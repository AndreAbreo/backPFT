package codigocreativo.uy.servidorapp.servicios;

import codigocreativo.uy.servidorapp.dtos.dtomappers.MarcasModeloMapper;
import codigocreativo.uy.servidorapp.dtos.MarcasModeloDto;
import codigocreativo.uy.servidorapp.entidades.MarcasModelo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MarcasModeloBeanTest {

    @Mock
    private EntityManager em;

    @Mock
    private MarcasModeloMapper marcasModeloMapper;

    @InjectMocks
    private MarcasModeloBean marcasModeloBean;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        marcasModeloBean = new MarcasModeloBean();

        // Utiliza reflexión para asignar el EntityManager privado
        Field emField = MarcasModeloBean.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(marcasModeloBean, em);

        Field mapperField = MarcasModeloBean.class.getDeclaredField("marcasModeloMapper");
        mapperField.setAccessible(true);
        mapperField.set(marcasModeloBean, marcasModeloMapper);
    }

    @Test
    void testCrearMarcasModelo_success() {
        MarcasModeloDto marcasModeloDto = new MarcasModeloDto();
        MarcasModelo marcasModeloEntity = new MarcasModelo();
        when(marcasModeloMapper.toEntity(any(MarcasModeloDto.class))).thenReturn(marcasModeloEntity);

        assertDoesNotThrow(() -> marcasModeloBean.crearMarcasModelo(marcasModeloDto));
        assertEquals("ACTIVO", marcasModeloDto.getEstado().name());
        verify(em, times(1)).persist(marcasModeloEntity);
        verify(em, times(1)).flush();
    }

    @Test
    void testModificarMarcasModelo_success() {
        MarcasModeloDto marcasModeloDto = new MarcasModeloDto();
        MarcasModelo marcasModeloEntity = new MarcasModelo();
        when(marcasModeloMapper.toEntity(any(MarcasModeloDto.class))).thenReturn(marcasModeloEntity);

        assertDoesNotThrow(() -> marcasModeloBean.modificarMarcasModelo(marcasModeloDto));
        verify(em, times(1)).merge(marcasModeloEntity);
        verify(em, times(1)).flush();
    }

    @Test
    void testObtenerMarca_success() {
        Long id = 1L;
        MarcasModelo marcasModeloEntity = new MarcasModelo();
        MarcasModeloDto marcasModeloDto = new MarcasModeloDto();

        when(em.find(MarcasModelo.class, id)).thenReturn(marcasModeloEntity);
        when(marcasModeloMapper.toDto(marcasModeloEntity)).thenReturn(marcasModeloDto);

        MarcasModeloDto result = marcasModeloBean.obtenerMarca(id);

        assertEquals(marcasModeloDto, result);
    }

    @Test
    void testObtenerMarcasLista_success() {
        List<MarcasModelo> marcasModeloList = new ArrayList<>();
        @SuppressWarnings("unchecked")
        TypedQuery<MarcasModelo> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(MarcasModelo.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(marcasModeloList);
        when(marcasModeloMapper.toDto(anyList())).thenReturn(new ArrayList<>());

        List<MarcasModeloDto> result = marcasModeloBean.obtenerMarcasLista();

        assertNotNull(result);
        verify(query, times(1)).getResultList();
    }

    @Test
    void testEliminarMarca_success() {
        Long id = 1L;
        @SuppressWarnings("unchecked")
        TypedQuery<MarcasModelo> query = mock(TypedQuery.class);
        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter("id", id)).thenReturn(query);

        assertDoesNotThrow(() -> marcasModeloBean.eliminarMarca(id));

        verify(query, times(1)).executeUpdate();
        verify(em, times(1)).flush();
    }
}
