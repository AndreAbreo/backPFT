package codigocreativo.uy.servidorapp.servicios;

import codigocreativo.uy.servidorapp.dtos.dtomappers.ModelosEquipoMapper;
import codigocreativo.uy.servidorapp.dtos.ModelosEquipoDto;
import codigocreativo.uy.servidorapp.entidades.ModelosEquipo;
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

class ModelosEquipoBeanTest {

    @Mock
    private EntityManager em;

    @Mock
    private ModelosEquipoMapper modelosEquipoMapper;

    @InjectMocks
    private ModelosEquipoBean modelosEquipoBean;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        modelosEquipoBean = new ModelosEquipoBean();

        // Utiliza reflexión para asignar el EntityManager privado
        Field emField = ModelosEquipoBean.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(modelosEquipoBean, em);

        Field mapperField = ModelosEquipoBean.class.getDeclaredField("modelosEquipoMapper");
        mapperField.setAccessible(true);
        mapperField.set(modelosEquipoBean, modelosEquipoMapper);
    }

    @Test
    void testCrearModelos_success() {
        ModelosEquipoDto modelosEquipoDto = new ModelosEquipoDto();
        ModelosEquipo modelosEquipoEntity = new ModelosEquipo();
        when(modelosEquipoMapper.toEntity(any(ModelosEquipoDto.class))).thenReturn(modelosEquipoEntity);

        assertDoesNotThrow(() -> modelosEquipoBean.crearModelos(modelosEquipoDto));
        verify(em, times(1)).persist(modelosEquipoEntity);
        verify(em, times(1)).flush();
    }

    @Test
    void testModificarModelos_success() {
        ModelosEquipoDto modelosEquipoDto = new ModelosEquipoDto();
        ModelosEquipo modelosEquipoEntity = new ModelosEquipo();
        when(modelosEquipoMapper.toEntity(any(ModelosEquipoDto.class))).thenReturn(modelosEquipoEntity);

        assertDoesNotThrow(() -> modelosEquipoBean.modificarModelos(modelosEquipoDto));
        verify(em, times(1)).merge(modelosEquipoEntity);
        verify(em, times(1)).flush();
    }

    @Test
    void testObtenerModelos_success() {
        Long id = 1L;
        ModelosEquipo modelosEquipoEntity = new ModelosEquipo();
        ModelosEquipoDto modelosEquipoDto = new ModelosEquipoDto();

        when(em.find(ModelosEquipo.class, id)).thenReturn(modelosEquipoEntity);
        when(modelosEquipoMapper.toDto(modelosEquipoEntity)).thenReturn(modelosEquipoDto);

        ModelosEquipoDto result = modelosEquipoBean.obtenerModelos(id);

        assertEquals(modelosEquipoDto, result);
    }

    @Test
    void testListarModelos_success() {
        List<ModelosEquipo> modelosEquipoList = new ArrayList<>();
        @SuppressWarnings("unchecked")
        TypedQuery<ModelosEquipo> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(ModelosEquipo.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(modelosEquipoList);
        when(modelosEquipoMapper.toDto(anyList())).thenReturn(new ArrayList<>());

        List<ModelosEquipoDto> result = modelosEquipoBean.listarModelos();

        assertNotNull(result);
        verify(query, times(1)).getResultList();
    }

    @Test
    void testEliminarModelos_success() {
        Long id = 1L;
        @SuppressWarnings("unchecked")
        TypedQuery<ModelosEquipo> query = mock(TypedQuery.class);
        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter("id", id)).thenReturn(query);

        assertDoesNotThrow(() -> modelosEquipoBean.eliminarModelos(id));

        verify(query, times(1)).executeUpdate();
        verify(em, times(1)).flush();
    }
}
