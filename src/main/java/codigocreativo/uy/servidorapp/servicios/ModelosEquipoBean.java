package codigocreativo.uy.servidorapp.servicios;

import codigocreativo.uy.servidorapp.dtos.ModelosEquipoDto;
import codigocreativo.uy.servidorapp.dtos.dtomappers.ModelosEquipoMapper;
import codigocreativo.uy.servidorapp.entidades.ModelosEquipo;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class ModelosEquipoBean implements ModelosEquipoRemote {
    @PersistenceContext(unitName = "default")
    private EntityManager em;
    @Inject
    private ModelosEquipoMapper modelosEquipoMapper;

    public ModelosEquipoBean() {
    }

    @Override
    public void crearModelos(ModelosEquipoDto modelosEquipo) {
        em.persist(modelosEquipoMapper.toEntity(modelosEquipo));
        em.flush();
    }

    @Override
    public void modificarModelos(ModelosEquipoDto modelosEquipo) {
        em.merge(modelosEquipoMapper.toEntity(modelosEquipo));
        em.flush();
    }

    @Override
    public ModelosEquipoDto obtenerModelos(Long id) {
        return modelosEquipoMapper.toDto(em.find(ModelosEquipo.class, id));
    }

    @Override
    public List<ModelosEquipoDto> listarModelos() {
        return modelosEquipoMapper.toDto(em.createQuery("SELECT modelosEquipo FROM ModelosEquipo modelosEquipo", ModelosEquipo.class).getResultList());
    }

    @Override
    public void eliminarModelos(Long id) {
        em.createQuery("UPDATE ModelosEquipo modelosEquipo SET modelosEquipo.estado = 'INACTIVO' WHERE modelosEquipo.id = :id")
                .setParameter("id", id)
                .executeUpdate();
        em.flush();
    }
}