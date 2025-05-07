package com.petstore.server.api.impl;

import com.petstore.server.api.*;
import java.io.File;
import com.petstore.server.api.model.ModelApiResponse;
import com.petstore.server.api.model.Pet;

import java.util.Arrays;
import java.util.List;
import com.petstore.server.api.NotFoundException;

import java.io.InputStream;

import com.petstore.server.api.model.Tag;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen", comments = "Generator version: 7.12.0")
public class PetApiServiceImpl extends PetApiService {
    @Override
    public Response addPet(Pet pet, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response deletePet(Long petId, String apiKey, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response findPetsByStatus(String status, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response findPetsByTags(List<String> tags, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response getPetById(Long petId, SecurityContext securityContext) throws NotFoundException {
        final Pet pet = new Pet();
        pet.setId(petId);
        pet.setName("Jack");
        final Tag tag = new Tag();
        tag.setName("puppy");
        pet.getTags().add(tag);
        return Response.ok().entity(pet).build();
    }

    @Override
    public Response updatePet(Pet pet, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response updatePetWithForm(Long petId, String name, String status, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response uploadFile(Long petId, String additionalMetadata, File body, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
