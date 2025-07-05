package codigocreativo.uy.servidorapp.servicios;

import codigocreativo.uy.servidorapp.dtos.UsuarioDto;
import codigocreativo.uy.servidorapp.dtos.dtomappers.CycleAvoidingMappingContext;
import codigocreativo.uy.servidorapp.dtos.dtomappers.UsuarioMapper;
import codigocreativo.uy.servidorapp.entidades.Usuario;
import codigocreativo.uy.servidorapp.enumerados.Estados;
import codigocreativo.uy.servidorapp.excepciones.ServiciosException;
import com.fabdelgado.ciuy.Validator;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;

@Stateless
public class UsuarioBean implements UsuarioRemote {
    @PersistenceContext (unitName = "default")
    private EntityManager em;
    @Inject
    private UsuarioMapper usuarioMapper;

    public UsuarioBean() {
    }

    private static final String EMAIL = "email";
    private static final String ESTADO = "estado";
    private static final int EDAD_MINIMA = 18;
    private static final String QUERY_USUARIO_POR_EMAIL = "SELECT u FROM Usuario u WHERE u.email = :email";

    @Override
    public void crearUsuario(UsuarioDto u) throws ServiciosException {
        // Validar que el usuario no sea nulo
        if (u == null) {
            throw new ServiciosException("Usuario nulo");
        }
        
        // Validar que el usuario sea mayor de edad
        validarMayorDeEdad(u.getFechaNacimiento());
        
        // Validar que el email sea único
        validarEmailUnico(u.getEmail());
        
        // Validar formato de cédula usando la librería CIUY
        validarFormatoCedula(u.getCedula());
        
        // Validar que la cédula sea única
        validarCedulaUnica(u.getCedula());
        
        u.setEstado(Estados.SIN_VALIDAR);
        em.merge(usuarioMapper.toEntity(u, new CycleAvoidingMappingContext()));
    }

    /**
     * Valida que el usuario sea mayor de edad
     */
    private void validarMayorDeEdad(LocalDate fechaNacimiento) throws ServiciosException {
        if (fechaNacimiento == null) {
            throw new ServiciosException("La fecha de nacimiento es obligatoria");
        }
        
        LocalDate fechaActual = LocalDate.now();
        Period edad = Period.between(fechaNacimiento, fechaActual);
        
        if (edad.getYears() < EDAD_MINIMA) {
            throw new ServiciosException("El usuario debe ser mayor de edad (mínimo " + EDAD_MINIMA + " años)");
        }
    }

    /**
     * Valida que el email sea único en la base de datos
     */
    private void validarEmailUnico(String email) throws ServiciosException {
        if (email == null || email.trim().isEmpty()) {
            throw new ServiciosException("El email es obligatorio");
        }
        
        try {
            em.createQuery(QUERY_USUARIO_POR_EMAIL, Usuario.class)
                    .setParameter(EMAIL, email.trim())
                    .getSingleResult();
            throw new ServiciosException("Ya existe un usuario con el email: " + email);
        } catch (NoResultException e) {
            // El email no existe, es válido
        }
    }

    /**
     * Valida el formato de la cédula uruguaya usando la librería CIUY
     */
    private void validarFormatoCedula(String cedula) throws ServiciosException {
        if (cedula == null || cedula.trim().isEmpty()) {
            throw new ServiciosException("La cédula es obligatoria");
        }
        
        String cedulaLimpia = cedula.trim();
        
        // Validar que la cédula solo contenga dígitos
        if (!cedulaLimpia.matches("\\d+")) {
            throw new ServiciosException("La cédula debe contener solo números: " + cedula);
        }
        
        // Validar que la cédula no exceda 8 dígitos
        if (cedulaLimpia.length() > 8) {
            throw new ServiciosException("La cédula no puede tener más de 8 dígitos: " + cedula);
        }
        
        Validator validator = new Validator();
        
        try {
            // Usar la librería CIUY para validar la cédula uruguaya
            if (!validator.validateCi(cedulaLimpia)) {
                throw new ServiciosException("La cédula no es válida: " + cedula);
            }
        } catch (StringIndexOutOfBoundsException e) {
            // Capturar la excepción específica de la librería CIUY
            throw new ServiciosException("La cédula tiene un formato inválido: " + cedula);
        } catch (Exception e) {
            // Capturar cualquier otra excepción de la librería
            throw new ServiciosException("Error al validar la cédula: " + cedula);
        }
    }

    /**
     * Valida que la cédula sea única en la base de datos
     */
    private void validarCedulaUnica(String cedula) throws ServiciosException {
        try {
            em.createQuery("SELECT u FROM Usuario u WHERE u.cedula = :cedula", Usuario.class)
                    .setParameter("cedula", cedula.trim())
                    .getSingleResult();
            throw new ServiciosException("Ya existe un usuario con la cédula: " + cedula);
        } catch (NoResultException e) {
            // La cédula no existe, es válida
        }
    }

    @Override
    public void modificarUsuario(UsuarioDto u) {
        em.merge(usuarioMapper.toEntity(u, new CycleAvoidingMappingContext()));
        em.flush();
    }

    @Override
    public void eliminarUsuario(UsuarioDto u) {
        em.createQuery("UPDATE Usuario u SET u.estado = 'INACTIVO' WHERE u.id = :id")
                .setParameter("id", u.getId())
                .executeUpdate();
        em.flush();
    }

    @Override
    public UsuarioDto obtenerUsuario(Long id) {
        return usuarioMapper.toDto(em.find(Usuario.class, id), new CycleAvoidingMappingContext());
    }

    @Override
    public UsuarioDto obtenerUsuarioDto(Long id) {
        return usuarioMapper.toDto(em.createQuery("SELECT u FROM Usuario u WHERE u.id = :id", Usuario.class)
                .setParameter("id", id)
                .getSingleResult(), new CycleAvoidingMappingContext());
    }

    @Override
    public UsuarioDto obtenerUsuarioPorCI(String ci) {
        return usuarioMapper.toDto(em.createQuery("SELECT u FROM Usuario u WHERE u.cedula = :ci", Usuario.class)
                .setParameter("ci", ci)
                .getSingleResult(), new CycleAvoidingMappingContext());
    }

    @Override
    public List<UsuarioDto> obtenerUsuarios() {
        return usuarioMapper.toDto(em.createQuery("SELECT u FROM Usuario u", Usuario.class).getResultList(), new CycleAvoidingMappingContext());
    }

    @Override
    public List<UsuarioDto> obtenerUsuariosFiltrados(String filtro, Object valor) {
        return usuarioMapper.toDto(em.createQuery("SELECT u FROM Usuario u WHERE u." + filtro + " = :valor", Usuario.class)
                .setParameter("valor", valor)
                .getResultList(), new CycleAvoidingMappingContext());
    }

    @Override
    public List<UsuarioDto> obtenerUsuariosPorEstado(Estados estado) {
        return usuarioMapper.toDto(em.createQuery("SELECT u FROM Usuario u WHERE u.estado = :estado", Usuario.class)
                .setParameter(ESTADO, estado)
                .getResultList(), new CycleAvoidingMappingContext());
    }

    @Override
    public UsuarioDto login(String usuario, String password) {
        try {
            return usuarioMapper.toDto(em.createQuery("SELECT u FROM Usuario u WHERE u.email = :usuario AND u.contrasenia = :password", Usuario.class)
                    .setParameter("usuario", usuario)
                    .setParameter("password", password)
                    .getSingleResult(), new CycleAvoidingMappingContext());
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public UsuarioDto findUserByEmail(String email) {
        try {
            return usuarioMapper.toDto(em.createQuery(QUERY_USUARIO_POR_EMAIL, Usuario.class)
                    .setParameter(EMAIL, email)
                    .getSingleResult(), new CycleAvoidingMappingContext());
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean hasPermission(String email, String requiredRole) {
        try {
            UsuarioDto usuario = em.createQuery(QUERY_USUARIO_POR_EMAIL, UsuarioDto.class)
                    .setParameter(EMAIL, email)
                    .getSingleResult();
            return usuario.getIdPerfil().getNombrePerfil().equals(requiredRole);
        } catch (NoResultException e) {
            return false;
        }
    }

    @Override
    public List<UsuarioDto> obtenerUsuariosFiltrado(Map<String, String> filtros) {
        StringBuilder queryStr = new StringBuilder("SELECT u FROM Usuario u WHERE 1=1");

        // Añadir condiciones de filtrado
        if (filtros.containsKey("nombre")) {
            queryStr.append(" AND LOWER(u.nombre) LIKE LOWER(:nombre)");
        }
        if (filtros.containsKey("apellido")) {
            queryStr.append(" AND LOWER(u.apellido) LIKE LOWER(:apellido)");
        }
        if (filtros.containsKey("nombreUsuario")) {
            queryStr.append(" AND LOWER(u.nombreUsuario) LIKE LOWER(:nombreUsuario)");
        }
        if (filtros.containsKey(EMAIL)) {
            queryStr.append(" AND LOWER(u.email) LIKE LOWER(:email)");
        }
        if (filtros.containsKey("tipoUsuario")) {
            queryStr.append(" AND LOWER(u.idPerfil.nombrePerfil) LIKE LOWER(:tipoUsuario)");
        }
        if (filtros.containsKey(ESTADO)) {
            queryStr.append(" AND u.estado = :estado");
        }

        var query = em.createQuery(queryStr.toString(), Usuario.class);

        // Establecer parámetros de la consulta
        filtros.forEach((key, value) -> {
            if (!value.isEmpty()) {
                if (key.equals(ESTADO)) {
                    query.setParameter(key, Estados.valueOf(value));  // Si es el estado, usamos el enum
                } else {
                    query.setParameter(key, "%" + value + "%");
                }
            }
        });
        return usuarioMapper.toDto(query.getResultList(), new CycleAvoidingMappingContext());
    }
}
