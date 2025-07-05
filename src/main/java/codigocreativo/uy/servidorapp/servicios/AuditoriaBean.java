package codigocreativo.uy.servidorapp.servicios;

import codigocreativo.uy.servidorapp.dtos.AuditoriaDto;
import codigocreativo.uy.servidorapp.dtos.dtomappers.AuditoriaMapper;
import codigocreativo.uy.servidorapp.entidades.Auditoria;
import codigocreativo.uy.servidorapp.excepciones.ServiciosException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class AuditoriaBean implements AuditoriaRemote{

    @PersistenceContext (unitName = "default")
    private EntityManager em;

    @Inject
    private AuditoriaMapper auditoriaMapper;

    public AuditoriaBean() {
    }

    @Override
    public void crearRegistro(AuditoriaDto a){
        em.persist(auditoriaMapper.toEntity(a));
    }
    @Override
    public List<AuditoriaDto> obtenerTodas() throws ServiciosException {
        try {
            return auditoriaMapper.toDto(em.createQuery("SELECT auditoria FROM Auditoria auditoria", Auditoria.class).getResultList());
        } catch (Exception e) {
            throw new ServiciosException("No se pudo listar las auditorias");
        }
    }
}
