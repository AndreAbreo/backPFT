package codigocreativo.uy.servidorapp.servicios;

import codigocreativo.uy.servidorapp.dtos.BajaEquipoDto;
import codigocreativo.uy.servidorapp.dtos.EquipoDto;
import codigocreativo.uy.servidorapp.dtos.dtomappers.BajaEquipoMapper;
import codigocreativo.uy.servidorapp.dtos.dtomappers.CycleAvoidingMappingContext;
import codigocreativo.uy.servidorapp.dtos.dtomappers.EquipoMapper;
import codigocreativo.uy.servidorapp.entidades.BajaEquipo;
import codigocreativo.uy.servidorapp.entidades.Equipo;
import codigocreativo.uy.servidorapp.excepciones.ServiciosException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipoBeanTest {

    @Mock
    private EntityManager em;

    @Mock
    private EquipoMapper equipoMapper;

    @Mock
    private BajaEquipoMapper bajaEquipoMapper;

    @InjectMocks
    private EquipoBean equipoBean;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        equipoBean = new EquipoBean();

        // Inyectar el EntityManager usando reflexión
        Field emField = EquipoBean.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(equipoBean, em);

        Field mapperField = EquipoBean.class.getDeclaredField("equipoMapper");
        mapperField.setAccessible(true);
        mapperField.set(equipoBean, equipoMapper);
    }

    @Test
    void testCrearEquipo_Success() {
        EquipoDto equipoDto = crearEquipoDtoValido();
        Equipo equipoEntity = new Equipo();

        // Mockear las consultas de validación de unicidad
        @SuppressWarnings("unchecked")
        jakarta.persistence.TypedQuery<Equipo> queryIdInterno = mock(jakarta.persistence.TypedQuery.class);
        @SuppressWarnings("unchecked")
        jakarta.persistence.TypedQuery<Equipo> queryNroSerie = mock(jakarta.persistence.TypedQuery.class);
        
        when(em.createQuery("SELECT e FROM Equipo e WHERE e.idInterno = :idInterno", Equipo.class)).thenReturn(queryIdInterno);
        when(queryIdInterno.setParameter("idInterno", "INT-001")).thenReturn(queryIdInterno);
        when(queryIdInterno.getSingleResult()).thenThrow(new jakarta.persistence.NoResultException());
        
        when(em.createQuery("SELECT e FROM Equipo e WHERE e.nroSerie = :nroSerie", Equipo.class)).thenReturn(queryNroSerie);
        when(queryNroSerie.setParameter("nroSerie", "123456789")).thenReturn(queryNroSerie);
        when(queryNroSerie.getSingleResult()).thenThrow(new jakarta.persistence.NoResultException());

        when(equipoMapper.toEntity(eq(equipoDto), any(CycleAvoidingMappingContext.class))).thenReturn(equipoEntity);

        assertDoesNotThrow(() -> equipoBean.crearEquipo(equipoDto));

        verify(em).persist(equipoEntity);
        verify(em).flush();
    }

    @Test
    void testCrearEquipo_WithNullEquipo() {
        assertThrows(ServiciosException.class, () -> equipoBean.crearEquipo(null));
        verify(em, never()).persist(any());
        verify(em, never()).flush();
    }

    @Test
    void testCrearEquipo_WithNullNombre() {
        EquipoDto equipoDto = crearEquipoDtoValido();
        equipoDto.setNombre(null);

        ServiciosException exception = assertThrows(ServiciosException.class, 
            () -> equipoBean.crearEquipo(equipoDto));
        
        assertEquals("El nombre del equipo es obligatorio", exception.getMessage());
        verify(em, never()).persist(any());
        verify(em, never()).flush();
    }

    @Test
    void testCrearEquipo_WithEmptyNombre() {
        EquipoDto equipoDto = crearEquipoDtoValido();
        equipoDto.setNombre("   ");

        ServiciosException exception = assertThrows(ServiciosException.class, 
            () -> equipoBean.crearEquipo(equipoDto));
        
        assertEquals("El nombre del equipo es obligatorio", exception.getMessage());
        verify(em, never()).persist(any());
        verify(em, never()).flush();
    }

    @Test
    void testCrearEquipo_WithNullTipo() {
        EquipoDto equipoDto = crearEquipoDtoValido();
        equipoDto.setIdTipo(null);

        ServiciosException exception = assertThrows(ServiciosException.class, 
            () -> equipoBean.crearEquipo(equipoDto));
        
        assertEquals("El tipo de equipo es obligatorio", exception.getMessage());
        verify(em, never()).persist(any());
        verify(em, never()).flush();
    }

    @Test
    void testCrearEquipo_WithNullNumeroSerie() {
        EquipoDto equipoDto = crearEquipoDtoValido();
        equipoDto.setNroSerie(null);

        ServiciosException exception = assertThrows(ServiciosException.class, 
            () -> equipoBean.crearEquipo(equipoDto));
        
        assertEquals("El número de serie es obligatorio", exception.getMessage());
        verify(em, never()).persist(any());
        verify(em, never()).flush();
    }

    @Test
    void testCrearEquipo_WithNullGarantia() {
        EquipoDto equipoDto = crearEquipoDtoValido();
        equipoDto.setGarantia(null);

        ServiciosException exception = assertThrows(ServiciosException.class, 
            () -> equipoBean.crearEquipo(equipoDto));
        
        assertEquals("La garantía es obligatoria", exception.getMessage());
        verify(em, never()).persist(any());
        verify(em, never()).flush();
    }

    @Test
    void testCrearEquipo_WithNullFechaAdquisicion() {
        EquipoDto equipoDto = crearEquipoDtoValido();
        equipoDto.setFechaAdquisicion(null);

        ServiciosException exception = assertThrows(ServiciosException.class, 
            () -> equipoBean.crearEquipo(equipoDto));
        
        assertEquals("La fecha de adquisición es obligatoria", exception.getMessage());
        verify(em, never()).persist(any());
        verify(em, never()).flush();
    }

    @Test
    void testCrearEquipo_WithNullIdentificacionInterna() {
        EquipoDto equipoDto = crearEquipoDtoValido();
        equipoDto.setIdInterno(null);

        ServiciosException exception = assertThrows(ServiciosException.class, 
            () -> equipoBean.crearEquipo(equipoDto));
        
        assertEquals("La identificación interna es obligatoria", exception.getMessage());
        verify(em, never()).persist(any());
        verify(em, never()).flush();
    }

    @Test
    void testCrearEquipo_WithNullImagen() {
        EquipoDto equipoDto = crearEquipoDtoValido();
        equipoDto.setImagen(null);

        ServiciosException exception = assertThrows(ServiciosException.class, 
            () -> equipoBean.crearEquipo(equipoDto));
        
        assertEquals("La imagen del equipo es obligatoria", exception.getMessage());
        verify(em, never()).persist(any());
        verify(em, never()).flush();
    }

    @Test
    void testCrearEquipo_WithDuplicateIdentificacionInterna() {
        EquipoDto equipoDto = crearEquipoDtoValido();
        equipoDto.setIdInterno("INT-001");

        // Mockear la consulta para simular que ya existe un equipo con esa identificación
        @SuppressWarnings("unchecked")
        jakarta.persistence.TypedQuery<Equipo> query = mock(jakarta.persistence.TypedQuery.class);
        when(em.createQuery("SELECT e FROM Equipo e WHERE e.idInterno = :idInterno", Equipo.class)).thenReturn(query);
        when(query.setParameter("idInterno", "INT-001")).thenReturn(query);
        when(query.getSingleResult()).thenReturn(new Equipo()); // Simula que encuentra un resultado

        ServiciosException exception = assertThrows(ServiciosException.class, 
            () -> equipoBean.crearEquipo(equipoDto));
        
        assertEquals("Ya existe un equipo con la identificación interna: INT-001", exception.getMessage());
        verify(em, never()).persist(any());
        verify(em, never()).flush();
    }

    @Test
    void testCrearEquipo_WithDuplicateNumeroSerie() {
        EquipoDto equipoDto = crearEquipoDtoValido();
        equipoDto.setNroSerie("123456789");

        // Mockear la consulta para identificación interna (debe pasar)
        @SuppressWarnings("unchecked")
        jakarta.persistence.TypedQuery<Equipo> queryIdInterno = mock(jakarta.persistence.TypedQuery.class);
        when(em.createQuery("SELECT e FROM Equipo e WHERE e.idInterno = :idInterno", Equipo.class)).thenReturn(queryIdInterno);
        when(queryIdInterno.setParameter("idInterno", "INT-001")).thenReturn(queryIdInterno);
        when(queryIdInterno.getSingleResult()).thenThrow(new jakarta.persistence.NoResultException());

        // Mockear la consulta para simular que ya existe un equipo con ese número de serie
        @SuppressWarnings("unchecked")
        jakarta.persistence.TypedQuery<Equipo> query = mock(jakarta.persistence.TypedQuery.class);
        when(em.createQuery("SELECT e FROM Equipo e WHERE e.nroSerie = :nroSerie", Equipo.class)).thenReturn(query);
        when(query.setParameter("nroSerie", "123456789")).thenReturn(query);
        when(query.getSingleResult()).thenReturn(new Equipo()); // Simula que encuentra un resultado

        ServiciosException exception = assertThrows(ServiciosException.class, 
            () -> equipoBean.crearEquipo(equipoDto));
        
        assertEquals("Ya existe un equipo con el número de serie: 123456789", exception.getMessage());
        verify(em, never()).persist(any());
        verify(em, never()).flush();
    }

    /**
     * Método auxiliar para crear un EquipoDto válido con todos los campos obligatorios
     */
    private EquipoDto crearEquipoDtoValido() {
        EquipoDto equipoDto = new EquipoDto();
        equipoDto.setNombre("Equipo Test");
        equipoDto.setIdTipo(new codigocreativo.uy.servidorapp.dtos.TiposEquipoDto());
        equipoDto.setIdModelo(new codigocreativo.uy.servidorapp.dtos.ModelosEquipoDto());
        equipoDto.setNroSerie("123456789");
        equipoDto.setGarantia("2 años");
        equipoDto.setIdPais(new codigocreativo.uy.servidorapp.dtos.PaisDto());
        equipoDto.setIdProveedor(new codigocreativo.uy.servidorapp.dtos.ProveedoresEquipoDto());
        equipoDto.setFechaAdquisicion(LocalDate.now());
        equipoDto.setIdInterno("INT-001");
        equipoDto.setIdUbicacion(new codigocreativo.uy.servidorapp.dtos.UbicacionDto());
        equipoDto.setImagen("imagen.jpg");
        return equipoDto;
    }

    @Test
    void testModificarEquipo() {
        EquipoDto equipoDto = new EquipoDto();
        Equipo equipoEntity = new Equipo();

        when(equipoMapper.toEntity(eq(equipoDto), any(CycleAvoidingMappingContext.class))).thenReturn(equipoEntity);

        equipoBean.modificarEquipo(equipoDto);

        verify(em).merge(equipoEntity);
        verify(em).flush();
    }

    @Test
    void testEliminarEquipo() {
        BajaEquipoDto bajaEquipoDto = new BajaEquipoDto();
        bajaEquipoDto.setIdEquipo(new EquipoDto());
        bajaEquipoDto.getIdEquipo().setId(1L);

        // Mockear Query para la actualización del estado
        @SuppressWarnings("unchecked")
        Query mockedQuery = mock(Query.class);
        when(em.createQuery("UPDATE Equipo equipo SET equipo.estado = 'INACTIVO' WHERE equipo.id = :id")).thenReturn(mockedQuery);
        when(mockedQuery.setParameter("id", 1L)).thenReturn(mockedQuery);
        when(mockedQuery.executeUpdate()).thenReturn(1);

        equipoBean.eliminarEquipo(bajaEquipoDto);

        // Verificar que se ejecuta la consulta de actualización
        verify(em).createQuery("UPDATE Equipo equipo SET equipo.estado = 'INACTIVO' WHERE equipo.id = :id");
        verify(mockedQuery).setParameter("id", 1L);
        verify(mockedQuery).executeUpdate();
        verify(em).flush();
        
        // Verificar que NO se hace merge de BajaEquipo (ya no debería hacerlo)
        verify(em, never()).merge(any(BajaEquipo.class));
    }

    @Test
    void testObtenerEquiposFiltrado() {
        Map<String, String> filtros = new HashMap<>();
        filtros.put("nombre", "test");

        StringBuilder queryStr = new StringBuilder("SELECT e FROM Equipo e WHERE 1=1 AND LOWER(e.nombre) LIKE LOWER(:nombre)");
        @SuppressWarnings("unchecked")
        TypedQuery<Equipo> query = mock(TypedQuery.class);

        when(em.createQuery(queryStr.toString(), Equipo.class)).thenReturn(query);
        when(query.setParameter("nombre", "%test%"))
                .thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(new Equipo()));
        when(equipoMapper.toDto(anyList(), any(CycleAvoidingMappingContext.class))).thenReturn(Collections.singletonList(new EquipoDto()));

        List<EquipoDto> result = equipoBean.obtenerEquiposFiltrado(filtros);

        assertEquals(1, result.size());
        verify(query).setParameter("nombre", "%test%");
    }

    @Test
    void testObtenerEquipo() {
        Long id = 1L;
        Equipo equipoEntity = new Equipo();
        EquipoDto equipoDto = new EquipoDto();

        when(em.find(Equipo.class, id)).thenReturn(equipoEntity);
        when(equipoMapper.toDto(eq(equipoEntity), any(CycleAvoidingMappingContext.class))).thenReturn(equipoDto);

        EquipoDto result = equipoBean.obtenerEquipo(id);

        assertEquals(equipoDto, result);
        verify(em).find(Equipo.class, id);
    }

    @Test
    void testListarEquipos() {
        List<Equipo> equiposEntityList = Collections.singletonList(new Equipo());
        List<EquipoDto> equiposDtoList = Collections.singletonList(new EquipoDto());

        @SuppressWarnings("unchecked")
        TypedQuery<Equipo> mockedQuery = mock(TypedQuery.class);
        when(em.createQuery("SELECT equipo FROM Equipo equipo", Equipo.class)).thenReturn(mockedQuery);
        when(mockedQuery.getResultList()).thenReturn(equiposEntityList);
        when(equipoMapper.toDto(anyList(), any(CycleAvoidingMappingContext.class))).thenReturn(equiposDtoList);

        List<EquipoDto> result = equipoBean.listarEquipos();

        assertEquals(equiposDtoList, result);
        verify(em).createQuery("SELECT equipo FROM Equipo equipo", Equipo.class);
    }
}
