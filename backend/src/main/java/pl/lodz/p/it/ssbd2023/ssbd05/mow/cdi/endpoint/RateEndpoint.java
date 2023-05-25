package pl.lodz.p.it.ssbd2023.ssbd05.mow.cdi.endpoint;

import static pl.lodz.p.it.ssbd2023.ssbd05.shared.Roles.MANAGER;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pl.lodz.p.it.ssbd2023.ssbd05.exceptions.AppBaseException;
import pl.lodz.p.it.ssbd2023.ssbd05.mow.ejb.managers.RateManagerLocal;

@RequestScoped
@Path("/rates")
@DenyAll
public class RateEndpoint {

    @Inject
    private RateManagerLocal rateManagerLocal;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(MANAGER)
    public Response createRate() throws AppBaseException {
        throw new UnsupportedOperationException();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(MANAGER)
    public Response removeFutureRate(@PathParam("id") Long id) throws AppBaseException {
        throw new UnsupportedOperationException();
    }
}
