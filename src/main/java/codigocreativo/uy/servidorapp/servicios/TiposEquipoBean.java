package codigocreativo.uy.servidorapp.servicios;

import codigocreativo.uy.servidorapp.dtos.TiposEquipoDto;
import codigocreativo.uy.servidorapp.dtos.dtomappers.TiposEquipoMapper;
import codigocreativo.uy.servidorapp.entidades.TiposEquipo;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class TiposEquipoBean implements TiposEquipoRemote{
    @PersistenceContext (unitName = "default")
    private EntityManager em;
    @Inject
    private TiposEquipoMapper tiposEquipoMapper;

    public TiposEquipoBean() {
    }

    @Override
    public void crearTiposEquipo(TiposEquipoDto tiposEquipo) {
        TiposEquipo tiposEquipoEntity = tiposEquipoMapper.toEntity(tiposEquipo);
        em.persist(tiposEquipoEntity);
        em.flush();
    }

    @Override
    public void modificarTiposEquipo(TiposEquipoDto tiposEquipo) {
        TiposEquipo tiposEquipoEntity = tiposEquipoMapper.toEntity(tiposEquipo);
        em.merge(tiposEquipoEntity);
        em.flush();
    }

    @Override
    public void eliminarTiposEquipo(Long id) {
        //solo cambia a estado INACTIVO
        TiposEquipo tiposEquipoEntity = em.find(TiposEquipo.class, id);
        tiposEquipoEntity.setEstado("INACTIVO");
        em.merge(tiposEquipoEntity);
    }

    @Override
    public TiposEquipoDto obtenerPorId(Long id) {
        TiposEquipo tiposEquipoEntity = em.find(TiposEquipo.class, id);
        return tiposEquipoMapper.toDto(tiposEquipoEntity);
    }

    @Override
    public List<TiposEquipoDto> listarTiposEquipo() {
        List<TiposEquipo> tiposEquipos = em.createQuery("SELECT t FROM TiposEquipo t", TiposEquipo.class).getResultList();
        return tiposEquipoMapper.toDto(tiposEquipos);
    }
}

