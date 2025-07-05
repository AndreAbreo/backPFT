package codigocreativo.uy.servidorapp.ws;

import codigocreativo.uy.servidorapp.filtros.CORSFilter;
import codigocreativo.uy.servidorapp.config.JacksonConfig;
import codigocreativo.uy.servidorapp.filtros.JwtTokenFilter;
import codigocreativo.uy.servidorapp.filtros.AuditoriaFilter;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import java.util.HashSet;
import java.util.Set;
import java.util.Optional;

@ApplicationPath("/api")
public class MiApp extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<>();
        
        // Recursos o web services
        resources.add(UbicacionesResource.class);
        resources.add(ModeloResource.class);
        resources.add(IntervencionesResource.class);
        resources.add(TipoEquipoResource.class);
        resources.add(EquipoResource.class);
        resources.add(PaisesResource.class);
        resources.add(FuncionalidadResource.class);
        resources.add(PerfilResource.class);
        resources.add(MarcaResource.class);
        resources.add(UsuarioResource.class);
        resources.add(TipoIntervencionesResource.class);
        resources.add(ProveedoresResource.class);

        // OpenApi
        resources.add(OpenApiResource.class);
        
        // Filtros y configuraciones
        resources.add(AuditoriaFilter.class);
        String disableAuth = Optional.ofNullable(System.getenv("DISABLE_JWT_AUTH"))
                .orElse("false");
        if (!"true".equalsIgnoreCase(disableAuth)) {
            resources.add(JwtTokenFilter.class);
        }
        resources.add(CORSFilter.class);
        resources.add(JacksonConfig.class);
        
        return resources;
    }
}
