package codigocreativo.uy.servidorapp.servicios;

import codigocreativo.uy.servidorapp.dtos.ProveedoresEquipoDto;
import codigocreativo.uy.servidorapp.dtos.dtomappers.ProveedoresEquipoMapper;
import codigocreativo.uy.servidorapp.entidades.ProveedoresEquipo;
import codigocreativo.uy.servidorapp.enumerados.Estados;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class ProveedoresEquipoBean implements ProveedoresEquipoRemote {
    @PersistenceContext(unitName = "default")
    private EntityManager em;
    @Inject
    private ProveedoresEquipoMapper proveedoresEquipoMapper;

    public ProveedoresEquipoBean() {
    }

    @Override
    public void crearProveedor(ProveedoresEquipoDto proveedoresEquipo) {
        proveedoresEquipo.setEstado(Estados.ACTIVO);
        ProveedoresEquipo proveedoresEquipoEntity = proveedoresEquipoMapper.toEntity(proveedoresEquipo);
        em.persist(proveedoresEquipoEntity);
        em.flush();
    }

    @Override
    public void modificarProveedor(ProveedoresEquipoDto proveedoresEquipo) {
        ProveedoresEquipo proveedoresEquipoEntity = proveedoresEquipoMapper.toEntity(proveedoresEquipo);
        em.merge(proveedoresEquipoEntity);
        em.flush();
    }

    @Override
    public ProveedoresEquipoDto obtenerProveedor(Long id) {
        ProveedoresEquipo proveedoresEquipo = em.find(ProveedoresEquipo.class, id);
        return proveedoresEquipoMapper.toDto(proveedoresEquipo);
    }

    @Override
    public List<ProveedoresEquipoDto> obtenerProveedores() {
        List<ProveedoresEquipo> proveedoresEquipo = em.createQuery("SELECT p FROM ProveedoresEquipo p WHERE p.estado = 'ACTIVO' ", ProveedoresEquipo.class).getResultList();
        return proveedoresEquipoMapper.toDto(proveedoresEquipo);
    }

    @Override
    public void eliminarProveedor(Long id) {
        em.createQuery("UPDATE ProveedoresEquipo p SET p.estado = 'INACTIVO' WHERE p.id = :id")
                .setParameter("id", id)
                .executeUpdate();
        em.flush();
    }

    @Override
    public List<ProveedoresEquipoDto> buscarProveedores(String nombre, Estados estado) {
        String query = "SELECT p FROM ProveedoresEquipo p WHERE 1=1";
        if (nombre != null && !nombre.isEmpty()) {
            query += " AND LOWER(p.nombre) LIKE :nombre";
        }
        if (estado != null) {
            query += " AND UPPER(p.estado) = :estado";
        }
        var q = em.createQuery(query, ProveedoresEquipo.class);
        if (nombre != null && !nombre.isEmpty()) {
            q.setParameter("nombre", "%" + nombre.toLowerCase() + "%");
        }
        if (estado != null) {
            q.setParameter("estado", estado.name().toUpperCase());
        }
        return proveedoresEquipoMapper.toDto(q.getResultList());
    }
}