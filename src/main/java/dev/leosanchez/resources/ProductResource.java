package dev.leosanchez.resources;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import dev.leosanchez.models.Product;
import dev.leosanchez.services.ProductService;
import io.vertx.core.json.JsonObject;

@Path("/products")
public class ProductResource {

    @Inject
    ProductService productService;

    @GET
    @Operation(summary = "Obtiene todos los productos almacenados", description = "Este endpoint permite obtener todos los productos almacenados en la base de datos")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Lista de productos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product[].class))),
            @APIResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response findAll() {
        try {
            return Response.ok(productService.findAll()).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().build();
        }
    }

    @GET
    @Operation(summary = "Obtiene un producto por su id específico", description = "Este endpoint permite buscar un producto existente a través de su identificador, el cual debe ser brindado como parámetro.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Se encuentra el producto", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class)) }),
            @APIResponse(responseCode = "404", description = "No se encuentra el producto con el ID deseado"),
            @APIResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findById(@Parameter( description = "Identificador del producto, generado al momento de la creación", example = "3187b908-62f8-4ce8-ad90-2fa4f475b899") @PathParam("id") String id) {
        try {
            Optional<Product> product = productService.findById(id);
            if (product.isPresent()) {
                return Response.ok(product.get()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().build();
        }
    }

    @POST
    @Operation(summary = "Crea un nuevo producto", description = "Este endpoint permite crear un nuevo producto. Para ello se requiere suministrar su nombre, una breve descripción, su marca, modelo y categoría, y el precio de venta. Al momento de la creación, se le asigna un identificador y se genera un descuento del 10% del precio definido originalmente. Retorna el producto ingresado con los campos generados.")
    @APIResponses(value = {
            @APIResponse(responseCode = "201", description = "Se creó el producto", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class)) }),
            @APIResponse(responseCode = "400", description = "Petición inválida"),
            @APIResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response save(@Parameter(description = "Datos del producto a ingresar") Product product) {
        try {
            productService.save(product);
            return Response.status(201).entity(product).build();
        } catch (ConstraintViolationException e) {
            List<String> errors = e.getConstraintViolations().stream().map(cv -> {
                String propertyPath = cv.getPropertyPath().toString();
                String property = propertyPath.substring(propertyPath.lastIndexOf(".") + 1);
                return property + " " + cv.getMessage();
            }).collect(Collectors.toList());
            JsonObject payload = new JsonObject();
            payload.put("errors", errors);
            return Response.status(Response.Status.BAD_REQUEST).entity(payload).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().build();
        }
    }
}
