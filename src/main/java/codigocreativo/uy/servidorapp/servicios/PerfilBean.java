package codigocreativo.uy.servidorapp.servicios;

import codigocreativo.uy.servidorapp.dtos.PerfilDto;
import codigocreativo.uy.servidorapp.dtos.dtomappers.PerfilMapper;
import codigocreativo.uy.servidorapp.entidades.Perfil;
import codigocreativo.uy.servidorapp.enumerados.Estados;
import codigocreativo.uy.servidorapp.excepciones.ServiciosException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class PerfilBean implements PerfilRemote {
    private static final String ERROR_PERFIL_NULL = "El perfil no puede ser null";
    private static final String ERROR_NOMBRE_OBLIGATORIO = "El nombre del perfil es obligatorio";
    private static final String ERROR_PERFIL_NO_ENCONTRADO = "Perfil no encontrado con ID: ";
    
    @PersistenceContext(unitName = "default")
    private EntityManager em;
    @Inject
    private PerfilMapper perfilMapper;

    public PerfilBean() {
    }

    @Override
    public PerfilDto crearPerfil(PerfilDto p) throws ServiciosException {
        // Validar que el perfil no sea null
        if (p == null) {
            throw new ServiciosException(ERROR_PERFIL_NULL);
        }

        // Validar que el nombre del perfil no sea null o vacío
        if (p.getNombrePerfil() == null || p.getNombrePerfil().trim().isEmpty()) {
            throw new ServiciosException(ERROR_NOMBRE_OBLIGATORIO);
        }

        // Validar que no exista otro perfil con el mismo nombre
        String nombrePerfil = p.getNombrePerfil().trim();
        List<Perfil> perfilesExistentes = em.createQuery(
                "SELECT p FROM Perfil p WHERE UPPER(p.nombrePerfil) = UPPER(:n)", Perfil.class)
                .setParameter("n", nombrePerfil)
                .getResultList();

        if (!perfilesExistentes.isEmpty()) {
            throw new ServiciosException("Ya existe un perfil con el nombre: " + nombrePerfil);
        }

        p.setEstado(Estados.ACTIVO);
        Perfil perfilEntity = perfilMapper.toEntity(p);
        em.persist(perfilEntity);
        em.flush();
        
        // Retornar el DTO con el ID generado
        return perfilMapper.toDto(perfilEntity);
    }

    @Override
    public void modificarPerfil(PerfilDto p) throws ServiciosException {
        // Validar que el perfil no sea null
        if (p == null) {
            throw new ServiciosException(ERROR_PERFIL_NULL);
        }

        // Validar que el perfil tenga ID
        if (p.getId() == null) {
            throw new ServiciosException("El ID del perfil es obligatorio para la modificación");
        }

        // Validar que el nombre del perfil no sea null o vacío
        if (p.getNombrePerfil() == null || p.getNombrePerfil().trim().isEmpty()) {
            throw new ServiciosException(ERROR_NOMBRE_OBLIGATORIO);
        }

        // Obtener la entidad existente desde la base de datos
        Perfil perfilExistente = em.find(Perfil.class, p.getId());
        if (perfilExistente == null) {
            throw new ServiciosException(ERROR_PERFIL_NO_ENCONTRADO + p.getId());
        }

        // Validar que no exista otro perfil con el mismo nombre (excluyendo el actual)
        String nombrePerfil = p.getNombrePerfil().trim();
        List<Perfil> perfilesExistentes = em.createQuery(
                "SELECT p FROM Perfil p WHERE UPPER(p.nombrePerfil) = UPPER(:nombre) AND p.id != :id", Perfil.class)
                .setParameter("nombre", nombrePerfil)
                .setParameter("id", p.getId())
                .getResultList();

        if (!perfilesExistentes.isEmpty()) {
            throw new ServiciosException("Ya existe un perfil con el nombre: " + nombrePerfil);
        }

        // Actualizar solo los campos que se pueden modificar
        perfilExistente.setNombrePerfil(nombrePerfil);
        perfilExistente.setEstado(p.getEstado());
        
        // No modificar la colección funcionalidadesPerfiles aquí
        // Si se necesita modificar las funcionalidades, debe hacerse en un método separado
        
        em.merge(perfilExistente);
        em.flush();
    }

    @Override
    public void eliminarPerfil(PerfilDto p) throws ServiciosException {
        // Validar que el perfil no sea null
        if (p == null) {
            throw new ServiciosException(ERROR_PERFIL_NULL);
        }

        // Validar que el perfil tenga ID
        if (p.getId() == null) {
            throw new ServiciosException("El ID del perfil es obligatorio para la eliminación");
        }

        // Obtener la entidad existente desde la base de datos
        Perfil perfilExistente = em.find(Perfil.class, p.getId());
        if (perfilExistente == null) {
            throw new ServiciosException(ERROR_PERFIL_NO_ENCONTRADO + p.getId());
        }

        // Cambiar el estado a INACTIVO (eliminación lógica)
        perfilExistente.setEstado(Estados.INACTIVO);
        
        // No modificar la colección funcionalidadesPerfiles para evitar problemas de orphanRemoval
        
        em.merge(perfilExistente);
        em.flush();
    }

    @Override
    public PerfilDto obtenerPerfil(Long id) {
        return perfilMapper.toDto(em.find(Perfil.class, id));
    }

    @Override
    public List<PerfilDto> obtenerPerfiles() {
        // Asegúrate de usar el método correcto para mapear una lista
        return perfilMapper.toDtoList(em.createQuery("SELECT p FROM Perfil p", Perfil.class).getResultList());
    }

    @Override
    public List<PerfilDto> listarPerfilesPorNombre(String nombre) {
        // Asegúrate de mapear correctamente una lista de resultados
        return perfilMapper.toDtoList(em.createQuery("SELECT p FROM Perfil p WHERE p.nombrePerfil LIKE :nombre", Perfil.class)
                .setParameter("nombre", "%" + nombre + "%")
                .getResultList());
    }

    @Override
    public List<PerfilDto> listarPerfilesPorEstado(Estados estado) {
        // Asegúrate de mapear correctamente una lista de resultados
        return perfilMapper.toDtoList(em.createQuery("SELECT p FROM Perfil p WHERE p.estado = :estado", Perfil.class)
                .setParameter("estado", estado)
                .getResultList());
    }

    
}
