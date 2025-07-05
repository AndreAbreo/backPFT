package codigocreativo.uy.servidorapp.servicios;

import codigocreativo.uy.servidorapp.dtos.BajaUbicacionDto;
import codigocreativo.uy.servidorapp.dtos.UbicacionDto;
import codigocreativo.uy.servidorapp.dtos.dtomappers.BajaUbicacionMapper;
import codigocreativo.uy.servidorapp.dtos.dtomappers.UbicacionMapper;
import codigocreativo.uy.servidorapp.dtos.dtomappers.CycleAvoidingMappingContext;
import codigocreativo.uy.servidorapp.entidades.BajaUbicacion;
import codigocreativo.uy.servidorapp.entidades.Ubicacion;
import codigocreativo.uy.servidorapp.enumerados.Estados;
import codigocreativo.uy.servidorapp.excepciones.ServiciosException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.lang.reflect.Field;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BajaUbicacionBeanTest {
    @Mock EntityManager em;
    @Mock BajaUbicacionMapper bajaUbicacionMapper;
    @Mock UbicacionMapper ubicacionMapper;
    @Mock UbicacionRemote ubicacionBean;
    @Mock TypedQuery<BajaUbicacion> query;
    
    BajaUbicacionBean bean;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        bean = new BajaUbicacionBean();
        
        // Inyectar mocks usando reflexión
        Field emField = BajaUbicacionBean.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(bean, em);
        
        Field ubicacionBeanField = BajaUbicacionBean.class.getDeclaredField("ubicacionBean");
        ubicacionBeanField.setAccessible(true);
        ubicacionBeanField.set(bean, ubicacionBean);

        Field bajaMapperField = BajaUbicacionBean.class.getDeclaredField("bajaUbicacionMapper");
        bajaMapperField.setAccessible(true);
        bajaMapperField.set(bean, bajaUbicacionMapper);

        Field ubiMapperField = BajaUbicacionBean.class.getDeclaredField("ubicacionMapper");
        ubiMapperField.setAccessible(true);
        ubiMapperField.set(bean, ubicacionMapper);
    }

    @Test
    void testCrearBajaUbicacion() throws Exception {
        BajaUbicacionDto dto = new BajaUbicacionDto();
        BajaUbicacion entity = new BajaUbicacion();
        when(bajaUbicacionMapper.toEntity(any(BajaUbicacionDto.class), any(CycleAvoidingMappingContext.class))).thenReturn(entity);
        
        bean.crearBajaUbicacion(dto);
        
        verify(em).persist(entity);
    }

    @Test
    void testCrearBajaUbicacionThrowsException() {
        BajaUbicacionDto dto = new BajaUbicacionDto();
        when(bajaUbicacionMapper.toEntity(any(BajaUbicacionDto.class), any(CycleAvoidingMappingContext.class))).thenThrow(new RuntimeException("DB Error"));
        
        assertThrows(ServiciosException.class, () -> bean.crearBajaUbicacion(dto));
    }

    @Test
    void testBorrarUbicacion() throws Exception {
        Long id = 1L;
        Ubicacion ubicacion = new Ubicacion();
        when(em.find(Ubicacion.class, id)).thenReturn(ubicacion);
        
        bean.borrarUbicacion(id);
        
        verify(em).remove(ubicacion);
        verify(em).flush();
    }

    @Test
    void testBorrarUbicacionNotFound() {
        Long id = 1L;
        when(em.find(Ubicacion.class, id)).thenReturn(null);
        
        assertThrows(ServiciosException.class, () -> bean.borrarUbicacion(id));
    }

    @Test
    void testBorrarUbicacionThrowsException() {
        Long id = 1L;
        when(em.find(Ubicacion.class, id)).thenThrow(new RuntimeException("DB Error"));
        
        assertThrows(ServiciosException.class, () -> bean.borrarUbicacion(id));
    }

    @Test
    void testListarBajaUbicaciones() throws Exception {
        List<BajaUbicacion> entities = List.of(new BajaUbicacion());
        List<BajaUbicacionDto> dtos = List.of(new BajaUbicacionDto());
        
        when(em.createQuery(anyString(), eq(BajaUbicacion.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(entities);
        when(bajaUbicacionMapper.toDto(anyList(), any(CycleAvoidingMappingContext.class))).thenReturn(dtos);
        
        List<BajaUbicacionDto> result = bean.listarBajaUbicaciones();
        
        assertEquals(dtos, result);
    }

    @Test
    void testListarBajaUbicacionesThrowsException() {
        when(em.createQuery(anyString(), eq(BajaUbicacion.class))).thenThrow(new RuntimeException("DB Error"));
        
        assertThrows(ServiciosException.class, () -> bean.listarBajaUbicaciones());
    }

    @Test
    void testBajaLogicaUbicacion() throws Exception {
        UbicacionDto dto = new UbicacionDto();
        Ubicacion entity = new Ubicacion();
        when(ubicacionMapper.toEntity(any(UbicacionDto.class))).thenReturn(entity);
        
        bean.bajaLogicaUbicacion(dto);
        
        assertEquals(Estados.INACTIVO, entity.getEstado());
        verify(em).merge(entity);
        verify(em).flush();
    }

    @Test
    void testBajaLogicaUbicacionThrowsException() {
        UbicacionDto dto = new UbicacionDto();
        when(ubicacionMapper.toEntity(any(UbicacionDto.class))).thenThrow(new RuntimeException("DB Error"));
        
        assertThrows(ServiciosException.class, () -> bean.bajaLogicaUbicacion(dto));
    }
}
