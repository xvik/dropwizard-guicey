package ru.vyarus.dropwizard.guice.examples.resource;

import com.google.common.base.Preconditions;
import ru.vyarus.dropwizard.guice.examples.model.User;
import ru.vyarus.dropwizard.guice.examples.repository.UserRepository;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.List;

/**
 * @author Vyacheslav Rusakov
 * @since 01.11.2018
 */
@Path("/users")
@Produces("application/json")
public class UserResource {

    private final UserRepository repository;

    @Inject
    public UserResource(final UserRepository repository) {
        this.repository = repository;
    }

    @POST
    @Path("/")
    public User create(String name) {
        Preconditions.checkState(repository.findByName(name) == null,
                "User with name %s already exists", name);
        User user = new User();
        user.setName(name);
        return repository.save(user);
    }

    @PUT
    @Path("/")
    public void update(User user) {
        repository.save(user);
    }

    @GET
    @Path("/")
    public List<User> findAll() {
        return repository.findAll();
    }
}
