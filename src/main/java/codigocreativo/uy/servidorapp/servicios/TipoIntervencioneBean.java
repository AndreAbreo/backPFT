package codigocreativo.uy.servidorapp.servicios;

import codigocreativo.uy.servidorapp.dtos.TiposIntervencioneDto;
import codigocreativo.uy.servidorapp.dtos.dtomappers.TiposIntervencioneMapper;
import codigocreativo.uy.servidorapp.entidades.TiposIntervencione;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class TipoIntervencioneBean implements TipoIntervencioneRemote {
    @PersistenceContext(unitName = "default")
    private EntityManager em;
    @Inject
    private TiposIntervencioneMapper tiposIntervencioneMapper;

    public TipoIntervencioneBean() {
    }

    @Override
    public List<TiposIntervencioneDto> obtenerTiposIntervenciones() {
        List<TiposIntervencione> tiposIntervenciones = em.createQuery("SELECT t FROM TiposIntervencione t WHERE t.estado = 'ACTIVO'", TiposIntervencione.class).getResultList();
        return tiposIntervencioneMapper.toDto(tiposIntervenciones);
    }

    @Override
    public TiposIntervencioneDto obtenerTipoIntervencion(Long id) {
        TiposIntervencione tipoIntervencion = em.find(TiposIntervencione.class, id);
        return tiposIntervencioneMapper.toDto(tipoIntervencion);
    }

    @Override
    public void crearTipoIntervencion(TiposIntervencioneDto tipoIntervencion) {
        TiposIntervencione tipoIntervencionEntity = tiposIntervencioneMapper.toEntity(tipoIntervencion);
        em.persist(tipoIntervencionEntity);
    }

    @Override
    public void modificarTipoIntervencion(TiposIntervencioneDto tipoIntervencion) {
        TiposIntervencione tipoIntervencionEntity = tiposIntervencioneMapper.toEntity(tipoIntervencion);
        em.merge(tipoIntervencionEntity);
    }

    @Override
    public void eliminarTipoIntervencion(Long id) {
        //no puede borrar solo poner como estado inactivo
        em.createQuery("UPDATE TiposIntervencione t SET t.estado = 'INACTIVO' WHERE t.id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }
}
