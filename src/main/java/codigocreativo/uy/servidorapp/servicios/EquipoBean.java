package codigocreativo.uy.servidorapp.servicios;

import codigocreativo.uy.servidorapp.dtos.BajaEquipoDto;
import codigocreativo.uy.servidorapp.dtos.EquipoDto;
import codigocreativo.uy.servidorapp.dtos.dtomappers.CycleAvoidingMappingContext;
import codigocreativo.uy.servidorapp.dtos.dtomappers.EquipoMapper;
import codigocreativo.uy.servidorapp.entidades.Equipo;
import codigocreativo.uy.servidorapp.excepciones.ServiciosException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Stateless
public class EquipoBean implements EquipoRemote {
    private static final String ERROR_EQUIPO_NULL = "El equipo no puede ser null";
    private static final String ERROR_NOMBRE_OBLIGATORIO = "El nombre del equipo es obligatorio";
    private static final String ERROR_TIPO_OBLIGATORIO = "El tipo de equipo es obligatorio";
    private static final String ERROR_MARCA_OBLIGATORIO = "La marca del equipo es obligatoria";
    private static final String ERROR_MODELO_OBLIGATORIO = "El modelo del equipo es obligatorio";
    private static final String ERROR_NUMERO_SERIE_OBLIGATORIO = "El número de serie es obligatorio";
    private static final String ERROR_GARANTIA_OBLIGATORIO = "La garantía es obligatoria";
    private static final String ERROR_PAIS_OBLIGATORIO = "El país de origen es obligatorio";
    private static final String ERROR_PROVEEDOR_OBLIGATORIO = "El proveedor es obligatorio";
    private static final String ERROR_FECHA_ADQUISICION_OBLIGATORIO = "La fecha de adquisición es obligatoria";
    private static final String ERROR_IDENTIFICACION_INTERNA_OBLIGATORIO = "La identificación interna es obligatoria";
    private static final String ERROR_UBICACION_OBLIGATORIO = "La ubicación es obligatoria";
    private static final String ERROR_IMAGEN_OBLIGATORIO = "La imagen del equipo es obligatoria";
    private static final String ERROR_IDENTIFICACION_INTERNA_DUPLICADA = "Ya existe un equipo con la identificación interna: ";
    private static final String ERROR_NUMERO_SERIE_DUPLICADO = "Ya existe un equipo con el número de serie: ";
    
    @PersistenceContext(unitName = "default")
    private EntityManager em;

    @Inject
    private EquipoMapper equipoMapper;

    public EquipoBean() {
    }

    @Override
    public void crearEquipo(EquipoDto equipo) throws ServiciosException {
        // Validar que el equipo no sea null
        if (equipo == null) {
            throw new ServiciosException(ERROR_EQUIPO_NULL);
        }

        // Validar campos obligatorios
        validarCampoObligatorio(equipo.getNombre(), ERROR_NOMBRE_OBLIGATORIO);
        validarCampoObligatorio(equipo.getIdTipo(), ERROR_TIPO_OBLIGATORIO);
        validarCampoObligatorio(equipo.getIdModelo(), ERROR_MARCA_OBLIGATORIO);
        validarCampoObligatorio(equipo.getIdModelo(), ERROR_MODELO_OBLIGATORIO);
        validarCampoObligatorio(equipo.getNroSerie(), ERROR_NUMERO_SERIE_OBLIGATORIO);
        validarCampoObligatorio(equipo.getGarantia(), ERROR_GARANTIA_OBLIGATORIO);
        validarCampoObligatorio(equipo.getIdPais(), ERROR_PAIS_OBLIGATORIO);
        validarCampoObligatorio(equipo.getIdProveedor(), ERROR_PROVEEDOR_OBLIGATORIO);
        validarCampoObligatorio(equipo.getFechaAdquisicion(), ERROR_FECHA_ADQUISICION_OBLIGATORIO);
        validarCampoObligatorio(equipo.getIdInterno(), ERROR_IDENTIFICACION_INTERNA_OBLIGATORIO);
        validarCampoObligatorio(equipo.getIdUbicacion(), ERROR_UBICACION_OBLIGATORIO);
        validarCampoObligatorio(equipo.getImagen(), ERROR_IMAGEN_OBLIGATORIO);

        // Validar que la identificación interna sea única
        validarIdentificacionInternaUnica(equipo.getIdInterno());
        
        // Validar que el número de serie sea único
        validarNumeroSerieUnico(equipo.getNroSerie());

        em.persist(equipoMapper.toEntity(equipo, new CycleAvoidingMappingContext()));
        em.flush();
    }

    /**
     * Valida que un campo no sea null o vacío
     */
    private void validarCampoObligatorio(Object campo, String mensajeError) throws ServiciosException {
        if (campo == null) {
            throw new ServiciosException(mensajeError);
        }
        
        if (campo instanceof String string && string.trim().isEmpty()) {
            throw new ServiciosException(mensajeError);
        }
    }

    /**
     * Valida que la identificación interna sea única en la base de datos
     */
    private void validarIdentificacionInternaUnica(String idInterno) throws ServiciosException {
        try {
            em.createQuery("SELECT e FROM Equipo e WHERE e.idInterno = :idInterno", Equipo.class)
                    .setParameter("idInterno", idInterno.trim())
                    .getSingleResult();
            throw new ServiciosException(ERROR_IDENTIFICACION_INTERNA_DUPLICADA + idInterno);
        } catch (jakarta.persistence.NoResultException e) {
            // La identificación interna no existe, es válida
        }
    }

    /**
     * Valida que el número de serie sea único en la base de datos
     */
    private void validarNumeroSerieUnico(String nroSerie) throws ServiciosException {
        try {
            em.createQuery("SELECT e FROM Equipo e WHERE e.nroSerie = :nroSerie", Equipo.class)
                    .setParameter("nroSerie", nroSerie.trim())
                    .getSingleResult();
            throw new ServiciosException(ERROR_NUMERO_SERIE_DUPLICADO + nroSerie);
        } catch (jakarta.persistence.NoResultException e) {
            // El número de serie no existe, es válido
        }
    }

    @Override
    public void modificarEquipo(EquipoDto equipo) {
        em.merge(equipoMapper.toEntity(equipo, new CycleAvoidingMappingContext()));
        em.flush();
    }

    @Override
    public void eliminarEquipo(BajaEquipoDto bajaEquipo) {
        // Solo actualizar el estado del equipo a INACTIVO
        em.createQuery("UPDATE Equipo equipo SET equipo.estado = 'INACTIVO' WHERE equipo.id = :id")
                .setParameter("id", bajaEquipo.getIdEquipo().getId())
                .executeUpdate();
        em.flush();
    }

    @Override
    public List<EquipoDto> obtenerEquiposFiltrado(Map<String, String> filtros) {
        StringBuilder queryStr = new StringBuilder("SELECT e FROM Equipo e WHERE 1=1");

        // Añadir condiciones de filtrado
        agregarCondicionesDeFiltrado(queryStr, filtros);

        var query = em.createQuery(queryStr.toString(), Equipo.class);

        // Establecer parámetros de la consulta
        establecerParametrosDeConsulta(query, filtros);

        return equipoMapper.toDto(query.getResultList(), new CycleAvoidingMappingContext());
    }

    // Método auxiliar para agregar condiciones de filtrado
    private void agregarCondicionesDeFiltrado(StringBuilder queryStr, Map<String, String> filtros) {
        agregarCondicion(queryStr, filtros, "nombre", "LOWER(e.nombre) LIKE LOWER(:nombre)");
        agregarCondicion(queryStr, filtros, "tipo", "LOWER(e.idTipo.nombreTipo) LIKE LOWER(:tipo)");
        agregarCondicion(queryStr, filtros, "marca", "LOWER(e.idModelo.marca.nombre) LIKE LOWER(:marca)");
        agregarCondicion(queryStr, filtros, "modelo", "LOWER(e.idModelo.nombre) LIKE LOWER(:modelo)");
        agregarCondicion(queryStr, filtros, "numeroSerie", "LOWER(e.nroSerie) LIKE LOWER(:numeroSerie)");
        agregarCondicion(queryStr, filtros, "paisOrigen", "LOWER(e.idPais.nombre) LIKE LOWER(:paisOrigen)");
        agregarCondicion(queryStr, filtros, "proveedor", "LOWER(e.idProveedor.nombre) LIKE LOWER(:proveedor)");
        agregarCondicion(queryStr, filtros, "fechaAdquisicion", "e.fechaAdquisicion = :fechaAdquisicion");
        agregarCondicion(queryStr, filtros, "identificacionInterna", "LOWER(e.idInterno) LIKE LOWER(:identificacionInterna)");
        agregarCondicion(queryStr, filtros, "ubicacion", "LOWER(e.idUbicacion.nombre) LIKE LOWER(:ubicacion)");
    }

    // Método auxiliar para agregar una condición individualmente
    private void agregarCondicion(StringBuilder queryStr, Map<String, String> filtros, String filtroClave, String condicion) {
        if (filtros.containsKey(filtroClave)) {
            queryStr.append(" AND ").append(condicion);
        }
    }

    // Método auxiliar para establecer los parámetros de consulta
    private void establecerParametrosDeConsulta(Query query, Map<String, String> filtros) {
        filtros.forEach((key, value) -> {
            if (!value.isEmpty()) {
                if (key.equals("fechaAdquisicion")) {
                    query.setParameter(key, LocalDate.parse(value));  // Si es fecha, se parsea
                } else {
                    query.setParameter(key, "%" + value + "%");
                }
            }
        });
    }

    @Override
    public EquipoDto obtenerEquipo(Long id) {
        if (id == null) {
            return null;
        }
        
        Equipo equipo = em.find(Equipo.class, id);
        if (equipo == null) {
            return null;
        }
        
        return equipoMapper.toDto(equipo, new CycleAvoidingMappingContext());
    }

    @Override
    public List<EquipoDto> listarEquipos() {
        return equipoMapper.toDto(em.createQuery("SELECT equipo FROM Equipo equipo", Equipo.class).getResultList(), new CycleAvoidingMappingContext());
    }
}