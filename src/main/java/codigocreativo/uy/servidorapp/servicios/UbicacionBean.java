package codigocreativo.uy.servidorapp.servicios;

import codigocreativo.uy.servidorapp.dtos.EquipoDto;
import codigocreativo.uy.servidorapp.dtos.UbicacionDto;
import codigocreativo.uy.servidorapp.dtos.dtomappers.EquipoMapper;
import codigocreativo.uy.servidorapp.dtos.dtomappers.UbicacionMapper;
import codigocreativo.uy.servidorapp.dtos.dtomappers.CycleAvoidingMappingContext;
import codigocreativo.uy.servidorapp.entidades.Ubicacion;
import codigocreativo.uy.servidorapp.enumerados.Estados;
import codigocreativo.uy.servidorapp.excepciones.ServiciosException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class UbicacionBean implements UbicacionRemote {
    @PersistenceContext(unitName = "default")
    private EntityManager em;
    @Inject
    private UbicacionMapper ubicacionMapper;
    @Inject
    private EquipoMapper equipoMapper;

    public UbicacionBean() {
    }

    @Override
    public void crearUbicacion(UbicacionDto ubi) throws ServiciosException {
        try {
            em.persist(ubicacionMapper.toEntity(ubi));
            em.flush();
        } catch (Exception e) {
            throw new ServiciosException("No se pudo crear la ubicacion");
        }
    }

    @Override
    public void modificarUbicacion(UbicacionDto ubi) throws ServiciosException {
        try {
            em.merge(ubicacionMapper.toEntity(ubi));
            em.flush();
        } catch (Exception e) {
            throw new ServiciosException("No se pudo modificar la ubicacion");
        }
    }

    @Override
    public void borrarUbicacion(Long id) throws ServiciosException {
        try {
            em.createQuery("UPDATE Ubicacion ubicacion SET ubicacion.estado = 'INACTIVO' WHERE ubicacion.id = :id")
                    .setParameter("id", id)
                    .executeUpdate();
        } catch (Exception e) {
            throw new ServiciosException(e.getMessage());
        }
    }

    @Override
    public void moverEquipoDeUbicacion(EquipoDto equipo, UbicacionDto ubicacion) throws ServiciosException {
        try {
            equipo.setIdUbicacion(ubicacion);
            em.merge(equipoMapper.toEntity(equipo, new CycleAvoidingMappingContext()));
            em.flush();
        } catch (Exception e) {
            throw new ServiciosException("No se pudo mover el equipo");
        }
    }

    @Override
    public List<UbicacionDto> listarUbicaciones() throws ServiciosException {
        //mostrar la ubicacion solo si tiene estado activo
        List<Ubicacion> ubicaciones = em.createQuery("SELECT u FROM Ubicacion u WHERE u.estado = 'ACTIVO'", Ubicacion.class).getResultList();
        return ubicacionMapper.toDto(ubicaciones);
    }

    @Override
    public UbicacionDto obtenerUbicacionPorId(Long id) throws ServiciosException {
        return ubicacionMapper.toDto(em.createQuery("SELECT u FROM Ubicacion u WHERE u.id = :id", Ubicacion.class).setParameter("id", id).getSingleResult());
    }

    @Override
    public void bajaLogicaUbicacion(UbicacionDto ub) throws ServiciosException {
        Ubicacion ubicacion = ubicacionMapper.toEntity(ub);
        try {
            ubicacion.setEstado(Estados.INACTIVO);
            em.merge(ubicacion);
            em.flush();
        } catch (Exception e) {
            throw new ServiciosException(e.getMessage());
        }
    }
}