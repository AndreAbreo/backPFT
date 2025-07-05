package codigocreativo.uy.servidorapp.servicios;

import codigocreativo.uy.servidorapp.dtos.InstitucionDto;
import codigocreativo.uy.servidorapp.dtos.UbicacionDto;
import codigocreativo.uy.servidorapp.dtos.dtomappers.InstitucionMapper;
import codigocreativo.uy.servidorapp.dtos.dtomappers.UbicacionMapper;
import codigocreativo.uy.servidorapp.entidades.Institucion;
import codigocreativo.uy.servidorapp.entidades.Ubicacion;
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

class InstitucionBeanTest {

    @Mock
    private EntityManager em;

    @Mock
    private InstitucionMapper institucionMapper;

    @Mock
    private UbicacionMapper ubicacionMapper;

    @InjectMocks
    private InstitucionBean institucionBean;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        institucionBean = new InstitucionBean();

        // Inyectar el EntityManager usando reflexión
        Field emField = InstitucionBean.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(institucionBean, em);

        Field instMapperField = InstitucionBean.class.getDeclaredField("institucionMapper");
        instMapperField.setAccessible(true);
        instMapperField.set(institucionBean, institucionMapper);

        Field ubiMapperField = InstitucionBean.class.getDeclaredField("ubicacionMapper");
        ubiMapperField.setAccessible(true);
        ubiMapperField.set(institucionBean, ubicacionMapper);
    }

    @Test
    void testAgregar() {
        InstitucionDto institucionDto = new InstitucionDto();
        Institucion institucionEntity = new Institucion();

        when(institucionMapper.toEntity(institucionDto)).thenReturn(institucionEntity);

        institucionBean.agregar(institucionDto);

        verify(em).persist(institucionEntity);
        verify(em).flush();
    }

    @Test
    void testEliminarInstitucion() {
        InstitucionDto institucionDto = new InstitucionDto();
        Institucion institucionEntity = new Institucion();

        when(institucionMapper.toEntity(institucionDto)).thenReturn(institucionEntity);

        institucionBean.eliminarInstitucion(institucionDto);

        verify(em).remove(institucionEntity);
        verify(em).flush();
    }

    @Test
    void testModificar() {
        InstitucionDto institucionDto = new InstitucionDto();
        Institucion institucionEntity = new Institucion();

        when(institucionMapper.toEntity(institucionDto)).thenReturn(institucionEntity);

        institucionBean.modificar(institucionDto);

        verify(em).merge(institucionEntity);
        verify(em).flush();
    }

    @Test
    void testObtenerUbicaciones() {
        List<Ubicacion> ubicaciones = Collections.singletonList(new Ubicacion());
        List<UbicacionDto> ubicacionesDto = Collections.singletonList(new UbicacionDto());

        @SuppressWarnings("unchecked")
        TypedQuery<Ubicacion> query = mock(TypedQuery.class);
        when(em.createQuery("SELECT u FROM Ubicacion u", Ubicacion.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(ubicaciones);
        when(ubicacionMapper.toDto(ubicaciones)).thenReturn(ubicacionesDto);

        List<UbicacionDto> result = institucionBean.obtenerUbicaciones();

        assertEquals(ubicacionesDto, result);
        verify(query).getResultList();
    }

    @Test
    void testObtenerInstituciones() {
        List<Institucion> instituciones = Collections.singletonList(new Institucion());
        List<InstitucionDto> institucionesDto = Collections.singletonList(new InstitucionDto());

        @SuppressWarnings("unchecked")
        TypedQuery<Institucion> query = mock(TypedQuery.class);
        when(em.createQuery("SELECT i FROM Institucion i", Institucion.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(instituciones);
        when(institucionMapper.toDto(instituciones)).thenReturn(institucionesDto);

        List<InstitucionDto> result = institucionBean.obtenerInstituciones();

        assertEquals(institucionesDto, result);
        verify(query).getResultList();
    }

    @Test
    void testObtenerInstitucionPorNombre() {
        String nombre = "Institucion Test";
        Institucion institucion = new Institucion();
        InstitucionDto institucionDto = new InstitucionDto();

        @SuppressWarnings("unchecked")
        TypedQuery<Institucion> query = mock(TypedQuery.class);
        when(em.createQuery("SELECT i FROM Institucion i WHERE i.nombre = :nombre", Institucion.class)).thenReturn(query);
        when(query.setParameter("nombre", nombre)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(institucion);
        when(institucionMapper.toDto(institucion)).thenReturn(institucionDto);

        InstitucionDto result = institucionBean.obtenerInstitucionPorNombre(nombre);

        assertEquals(institucionDto, result);
    }

    @Test
    void testObtenerInstitucionPorId() {
        Long id = 1L;
        Institucion institucion = new Institucion();
        InstitucionDto institucionDto = new InstitucionDto();

        @SuppressWarnings("unchecked")
        TypedQuery<Institucion> query = mock(TypedQuery.class);
        when(em.createQuery("SELECT i FROM Institucion i WHERE i.id = :id", Institucion.class)).thenReturn(query);
        when(query.setParameter("id", id)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(institucion);
        when(institucionMapper.toDto(institucion)).thenReturn(institucionDto);

        InstitucionDto result = institucionBean.obtenerInstitucionPorId(id);

        assertEquals(institucionDto, result);
    }
}
