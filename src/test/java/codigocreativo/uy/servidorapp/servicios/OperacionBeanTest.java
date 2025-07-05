package codigocreativo.uy.servidorapp.servicios;

import codigocreativo.uy.servidorapp.dtos.OperacionDto;
import codigocreativo.uy.servidorapp.dtos.dtomappers.OperacionMapper;
import codigocreativo.uy.servidorapp.entidades.Operacion;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

class OperacionBeanTest {

    @Mock
    private EntityManager em;

    @Mock
    private OperacionMapper operacionMapper;

    @InjectMocks
    private OperacionBean operacionBean;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        operacionBean = new OperacionBean();

        // Inyectar el EntityManager usando reflexión
        Field emField = OperacionBean.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(operacionBean, em);

        Field mapperField = OperacionBean.class.getDeclaredField("operacionMapper");
        mapperField.setAccessible(true);
        mapperField.set(operacionBean, operacionMapper);
    }

    @Test
    void testCrearOperacion() {
        OperacionDto operacionDto = new OperacionDto();
        Operacion operacionEntity = new Operacion();

        when(operacionMapper.toEntity(operacionDto)).thenReturn(operacionEntity);

        operacionBean.crearOperacion(operacionDto);

        verify(em).persist(operacionEntity);
        verify(em).flush();
    }

    @Test
    void testModificarOperacion() {
        OperacionDto operacionDto = new OperacionDto();
        Operacion operacionEntity = new Operacion();

        when(operacionMapper.toEntity(operacionDto)).thenReturn(operacionEntity);

        operacionBean.modificarOperacion(operacionDto);

        verify(em).merge(operacionEntity);
        verify(em).flush();
    }

    @Test
    void testEliminarOperacion() {
        OperacionDto operacionDto = new OperacionDto();
        Operacion operacionEntity = new Operacion();

        when(operacionMapper.toEntity(operacionDto)).thenReturn(operacionEntity);

        operacionBean.eliminarOperacion(operacionDto);

        verify(em).remove(operacionEntity);
        verify(em).flush();
    }
}
