package codigocreativo.uy.servidorapp.servicios;

import codigocreativo.uy.servidorapp.dtos.EquiposUbicacioneDto;
import codigocreativo.uy.servidorapp.dtos.dtomappers.CycleAvoidingMappingContext;
import codigocreativo.uy.servidorapp.dtos.dtomappers.EquiposUbicacioneMapper;
import codigocreativo.uy.servidorapp.entidades.EquiposUbicacione;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class EquiposUbicacioneBean implements EquiposUbicacioneRemote {
    @PersistenceContext(unitName = "default")
    private EntityManager em;
    @Inject
    private EquiposUbicacioneMapper equiposUbicacioneMapper;

    public EquiposUbicacioneBean() {
    }

    @Override
    public void crearEquiposUbicacione(EquiposUbicacioneDto equiposUbicacione) {
        em.persist(equiposUbicacioneMapper.toEntity(equiposUbicacione, new CycleAvoidingMappingContext()));
        em.flush();
    }

    @Override
    public List<EquiposUbicacioneDto> obtenerEquiposUbicacione() {
        return equiposUbicacioneMapper.toDto(em.createQuery("SELECT equiposUbicacione FROM EquiposUbicacione equiposUbicacione", EquiposUbicacione.class).getResultList(), new CycleAvoidingMappingContext());
    }

    @Override
    public List<EquiposUbicacioneDto> obtenerEquiposUbicacionePorEquipo(Long id) {
        return equiposUbicacioneMapper.toDto(em.createQuery("SELECT equiposUbicacione FROM EquiposUbicacione equiposUbicacione WHERE equiposUbicacione.idEquipo.id = :id", EquiposUbicacione.class)
                .setParameter("id", id)
                .getResultList(), new CycleAvoidingMappingContext());
    }
}