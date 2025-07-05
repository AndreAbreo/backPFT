package codigocreativo.uy.servidorapp.servicios;

import codigocreativo.uy.servidorapp.dtos.InstitucionDto;
import codigocreativo.uy.servidorapp.dtos.UbicacionDto;
import codigocreativo.uy.servidorapp.dtos.dtomappers.InstitucionMapper;
import codigocreativo.uy.servidorapp.dtos.dtomappers.UbicacionMapper;
import codigocreativo.uy.servidorapp.entidades.Institucion;
import codigocreativo.uy.servidorapp.entidades.Ubicacion;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
@Stateless
public class InstitucionBean implements InstitucionRemote{

    @PersistenceContext (unitName = "default")
    private EntityManager em;
    @Inject //Se inyecta el mapper
    private InstitucionMapper institucionMapper;
    @Inject
    private UbicacionMapper ubicacionMapper;

    public InstitucionBean() {
    }

    @Override
    public void agregar(InstitucionDto i) {
        Institucion institucionEntity = institucionMapper.toEntity(i);
        em.persist(institucionEntity);
        em.flush();
    }

    @Override
    public void eliminarInstitucion(InstitucionDto i) {
        Institucion institucionEntity = institucionMapper.toEntity(i);
        em.remove(institucionEntity);
        em.flush();

    }
    @Override
    public void modificar(InstitucionDto i) {
        Institucion institucionEntity = institucionMapper.toEntity(i);
        em.merge(institucionEntity);
        em.flush();

    }

    @Override
    public List<UbicacionDto> obtenerUbicaciones() {
        List<Ubicacion> ubicaciones = em.createQuery("SELECT u FROM Ubicacion u", Ubicacion.class).getResultList();
        return ubicacionMapper.toDto(ubicaciones);
    }

    @Override
    public List<InstitucionDto> obtenerInstituciones() {
        List<Institucion> instituciones = em.createQuery("SELECT i FROM Institucion i", Institucion.class).getResultList();
        return institucionMapper.toDto(instituciones);
    }

    @Override
    public InstitucionDto obtenerInstitucionPorNombre(String nombre) {
        Institucion institucion = em.createQuery("SELECT i FROM Institucion i WHERE i.nombre = :nombre", Institucion.class)
                .setParameter("nombre", nombre)
                .getSingleResult();
        return institucionMapper.toDto(institucion);
    }

    @Override
    public InstitucionDto obtenerInstitucionPorId(Long id) {
        Institucion institucion = em.createQuery("SELECT i FROM Institucion i WHERE i.id = :id", Institucion.class)
                .setParameter("id", id)
                .getSingleResult();
        return institucionMapper.toDto(institucion);
    }
}
