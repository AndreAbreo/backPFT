package codigocreativo.uy.servidorapp.servicios;

import codigocreativo.uy.servidorapp.dtos.IntervencionDto;
import codigocreativo.uy.servidorapp.dtos.dtomappers.CycleAvoidingMappingContext;
import codigocreativo.uy.servidorapp.dtos.dtomappers.IntervencionMapper;
import codigocreativo.uy.servidorapp.entidades.*;
import codigocreativo.uy.servidorapp.excepciones.ServiciosException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Stateless
public class IntervencionBean implements IntervencionRemote {
    @PersistenceContext(unitName = "default")
    private EntityManager em;
    @Inject //Se inyecta el mapper
    private IntervencionMapper intervencionMapper;

    public IntervencionBean() {
    }

    @Override
    public void crear(IntervencionDto intervencion) throws ServiciosException {
        Intervencion intervencionEntity = intervencionMapper.toEntity(intervencion, new CycleAvoidingMappingContext());
        em.persist(intervencionEntity);
    }

    @Override
    public void actualizar(IntervencionDto intervencion) throws ServiciosException {
        //Se "transforma" el DTO a una entidad
        Intervencion intervencionEntity = intervencionMapper.toEntity(intervencion, new CycleAvoidingMappingContext());

        //Se actualiza la entidad (no el DTO)
        em.merge(intervencionEntity);
    }

    @Override
    public List<IntervencionDto> obtenerTodas() throws ServiciosException {
        //Se obtienen todas las entidades
        List<Intervencion> intervenciones = em.createQuery("SELECT i FROM Intervencion i", Intervencion.class).getResultList();

        //Se transforman la lista de entidades en una lista de DTOs (hay un metodo que recibe una lista y devuelve otra lista ya transformada)
        return intervencionMapper.toDto(intervenciones, new CycleAvoidingMappingContext());
    }

    @Override
    public IntervencionDto buscarId(Long id) throws ServiciosException {
        Intervencion intervencion = em.find(Intervencion.class, id);
        return intervencionMapper.toDto(intervencion, new CycleAvoidingMappingContext());
    }

    @Override
    public List<IntervencionDto> obtenerPorRangoDeFecha(LocalDateTime fechaDesde, LocalDateTime fechaHasta, Long idEquipo) throws ServiciosException {
        String queryStr = "SELECT i FROM Intervencion i WHERE i.fechaHora BETWEEN :fechaDesde AND :fechaHasta";
        if (idEquipo != null) {
            queryStr += " AND i.idEquipo.id = :idEquipo";
        }
        TypedQuery<Intervencion> query = em.createQuery(queryStr, Intervencion.class);
        query.setParameter("fechaDesde", fechaDesde);
        query.setParameter("fechaHasta", fechaHasta);
        if (idEquipo != null) {
            query.setParameter("idEquipo", idEquipo);
        }
        List<Intervencion> intervenciones = query.getResultList();
        return intervencionMapper.toDto(intervenciones, new CycleAvoidingMappingContext());
    }

    @Override
    public Map<String, Long> obtenerCantidadPorTipo(LocalDateTime fechaDesde, LocalDateTime fechaHasta, Long idTipo) throws ServiciosException {
        String queryStr = "SELECT i FROM Intervencion i WHERE 1=1";
        if (fechaDesde != null && fechaHasta != null) {
            queryStr += " AND i.fechaHora BETWEEN :fechaDesde AND :fechaHasta";
        }
        if (idTipo != null) {
            queryStr += " AND i.idTipo.id = :idTipo";
        }
        TypedQuery<Intervencion> query = em.createQuery(queryStr, Intervencion.class);
        if (fechaDesde != null && fechaHasta != null) {
            query.setParameter("fechaDesde", fechaDesde);
            query.setParameter("fechaHasta", fechaHasta);
        }
        if (idTipo != null) {
            query.setParameter("idTipo", idTipo);
        }
        List<Intervencion> intervenciones = query.getResultList();
        return intervenciones.stream()
                .filter(i -> i.getIdTipo() != null) // Add null check here
                .collect(Collectors.groupingBy(i -> i.getIdTipo().getNombreTipo(), Collectors.counting()));
    }
}
