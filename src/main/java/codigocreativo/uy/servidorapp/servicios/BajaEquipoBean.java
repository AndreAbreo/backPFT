package codigocreativo.uy.servidorapp.servicios;

import codigocreativo.uy.servidorapp.dtos.BajaEquipoDto;
import codigocreativo.uy.servidorapp.dtos.UsuarioDto;
import codigocreativo.uy.servidorapp.dtos.EquipoDto;
import codigocreativo.uy.servidorapp.dtos.dtomappers.BajaEquipoMapper;
import codigocreativo.uy.servidorapp.dtos.dtomappers.CycleAvoidingMappingContext;
import codigocreativo.uy.servidorapp.entidades.BajaEquipo;
import codigocreativo.uy.servidorapp.excepciones.ServiciosException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

@Stateless
public class BajaEquipoBean implements BajaEquipoRemote {
    
    private static final Logger LOGGER = Logger.getLogger(BajaEquipoBean.class.getName());
    private static final String ERROR_BAJA_NULL = "La baja de equipo no puede ser null";
    private static final String ERROR_RAZON_OBLIGATORIA = "La razón de la baja es obligatoria";
    private static final String ERROR_FECHA_OBLIGATORIA = "La fecha de baja es obligatoria";
    private static final String ERROR_EQUIPO_OBLIGATORIO = "El equipo es obligatorio";
    private static final String ERROR_USUARIO_SESION = "No se pudo obtener el usuario de la sesión";
    
    @PersistenceContext(unitName = "default")
    private EntityManager em;
    
    @Inject
    private BajaEquipoMapper bajaEquipoMapper;
    
    @EJB
    private UsuarioRemote usuarioRemote;
    
    @EJB
    private EquipoRemote equipoRemote;
    
    public BajaEquipoBean() {
    }
    
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void crearBajaEquipo(BajaEquipoDto bajaEquipo, String emailUsuario) throws ServiciosException {
        LOGGER.info("Iniciando proceso de baja para equipo ID: " + 
                   (bajaEquipo != null && bajaEquipo.getIdEquipo() != null ? bajaEquipo.getIdEquipo().getId() : "null"));
        
        // Validar que la baja no sea null
        if (bajaEquipo == null) {
            throw new ServiciosException(ERROR_BAJA_NULL);
        }
        
        // Validar campos obligatorios
        validarCampoObligatorio(bajaEquipo.getRazon(), ERROR_RAZON_OBLIGATORIA);
        validarCampoObligatorio(bajaEquipo.getIdEquipo(), ERROR_EQUIPO_OBLIGATORIO);
        validarCampoObligatorio(emailUsuario, ERROR_USUARIO_SESION);
        
        // Verificar si ya existe una baja para este equipo
        Long idEquipo = bajaEquipo.getIdEquipo().getId();
        LOGGER.info("Verificando si existe baja para equipo ID: " + idEquipo);
        
        if (existeBajaParaEquipo(idEquipo)) {
            LOGGER.warning("Ya existe una baja para el equipo ID: " + idEquipo);
            throw new ServiciosException("El equipo ya ha sido dado de baja anteriormente");
        }
        
        LOGGER.info("No existe baja previa, procediendo con la creación");
        
        // Obtener el usuario desde la sesión usando el email
        UsuarioDto usuario = usuarioRemote.findUserByEmail(emailUsuario);
        if (usuario == null) {
            throw new ServiciosException("Usuario no encontrado con el email: " + emailUsuario);
        }
        
        // Asignar el usuario obtenido de la sesión
        bajaEquipo.setIdUsuario(usuario);
        
        // Establecer estado como INACTIVO por defecto
        bajaEquipo.setEstado("INACTIVO");
        
        // Si no se especifica fecha, usar la fecha actual
        if (bajaEquipo.getFecha() == null) {
            bajaEquipo.setFecha(LocalDate.now());
        }
        
        LOGGER.info("Persistiendo baja del equipo");
        // Persistir la baja del equipo
        BajaEquipo entity = bajaEquipoMapper.toEntity(bajaEquipo, new CycleAvoidingMappingContext());
        em.persist(entity);
        
        LOGGER.info("Estableciendo estado del equipo como INACTIVO");
        // Establecer el estado del equipo como INACTIVO
        equipoRemote.eliminarEquipo(bajaEquipo);
        
        em.flush();
        LOGGER.info("Baja del equipo completada exitosamente");
    }
    
    /**
     * Verifica si ya existe una baja para el equipo especificado
     */
    private boolean existeBajaParaEquipo(Long idEquipo) {
        try {
            LOGGER.info("Ejecutando consulta para verificar baja del equipo ID: " + idEquipo);
            Long count = em.createQuery(
                "SELECT COUNT(be) FROM BajaEquipo be WHERE be.idEquipo.id = :idEquipo", 
                Long.class)
                .setParameter("idEquipo", idEquipo)
                .getSingleResult();
            LOGGER.info("Resultado de consulta: " + count + " bajas encontradas");
            return count > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al verificar si existe baja para equipo ID: " + idEquipo, e);
            // Si hay algún error en la consulta, asumimos que no existe
            return false;
        }
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
    
    @Override
    public List<BajaEquipoDto> obtenerBajasEquipos() {
        return bajaEquipoMapper.toDto(
            em.createQuery("SELECT be FROM BajaEquipo be ORDER BY be.fecha DESC", BajaEquipo.class)
                .getResultList(), 
            new CycleAvoidingMappingContext()
        );
    }
    
    @Override
    public BajaEquipoDto obtenerBajaEquipo(Long id) {
        if (id == null) {
            return null;
        }
        
        BajaEquipo bajaEquipo = em.find(BajaEquipo.class, id);
        if (bajaEquipo == null) {
            return null;
        }
        
        return bajaEquipoMapper.toDto(bajaEquipo, new CycleAvoidingMappingContext());
    }
}