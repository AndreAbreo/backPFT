package codigocreativo.uy.servidorapp.servicios;

import codigocreativo.uy.servidorapp.dtos.dtomappers.AuditoriaMapper;
import codigocreativo.uy.servidorapp.dtos.AuditoriaDto;
import codigocreativo.uy.servidorapp.entidades.Auditoria;
import codigocreativo.uy.servidorapp.excepciones.ServiciosException;
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

class AuditoriaBeanTest {

    @Mock
    private EntityManager em;

    @Mock
    private AuditoriaMapper auditoriaMapper;

    @InjectMocks
    private AuditoriaBean auditoriaBean;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        auditoriaBean = new AuditoriaBean();

        // Utiliza reflexión para asignar el EntityManager privado
        Field emField = AuditoriaBean.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(auditoriaBean, em);

        Field mapperField = AuditoriaBean.class.getDeclaredField("auditoriaMapper");
        mapperField.setAccessible(true);
        mapperField.set(auditoriaBean, auditoriaMapper);
    }

    @Test
    void testCrearRegistro_success() {
        AuditoriaDto auditoriaDto = new AuditoriaDto();
        Auditoria auditoriaEntity = new Auditoria();
        when(auditoriaMapper.toEntity(any(AuditoriaDto.class))).thenReturn(auditoriaEntity);

        assertDoesNotThrow(() -> auditoriaBean.crearRegistro(auditoriaDto));
        verify(em, times(1)).persist(auditoriaEntity);
    }

    @Test
    void testCrearRegistro_mapperException() {
        AuditoriaDto auditoriaDto = new AuditoriaDto();
        when(auditoriaMapper.toEntity(any(AuditoriaDto.class))).thenThrow(new RuntimeException("Simulated exception"));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> auditoriaBean.crearRegistro(auditoriaDto));
        assertEquals("Simulated exception", thrown.getMessage());
    }

    @Test
    void testObtenerTodas_success() {
        List<Auditoria> auditoriaList = new ArrayList<>();
        @SuppressWarnings("unchecked")
        TypedQuery<Auditoria> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Auditoria.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(auditoriaList);
        when(auditoriaMapper.toDto(anyList())).thenReturn(new ArrayList<>());

        assertDoesNotThrow(() -> auditoriaBean.obtenerTodas());
        verify(query, times(1)).getResultList();
    }

    @Test
    void testObtenerTodas_exception() {
        when(em.createQuery(anyString(), eq(Auditoria.class))).thenThrow(new RuntimeException("Simulated exception"));

        ServiciosException thrown = assertThrows(ServiciosException.class, () -> auditoriaBean.obtenerTodas());
        assertEquals("No se pudo listar las auditorias", thrown.getMessage());
    }
}
