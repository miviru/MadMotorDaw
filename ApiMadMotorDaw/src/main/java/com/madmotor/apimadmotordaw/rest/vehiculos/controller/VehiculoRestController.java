package com.madmotor.apimadmotordaw.rest.vehiculos.controller;


import com.madmotor.apimadmotordaw.rest.vehiculos.dto.VehiculoCreateDto;
import com.madmotor.apimadmotordaw.rest.vehiculos.dto.VehiculoUpdateDto;
import com.madmotor.apimadmotordaw.utils.pagination.PageResponse;
import com.madmotor.apimadmotordaw.rest.vehiculos.models.Vehiculo;
import com.madmotor.apimadmotordaw.rest.vehiculos.services.VehiculoService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;


@Slf4j
@RestController
@RequestMapping("${api.version}/vehiculos")
public class VehiculoRestController {
    private VehiculoService vehiculoService;

    @Autowired
    public VehiculoRestController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @GetMapping()
    public ResponseEntity<PageResponse<Vehiculo>> getAllVehiculos(
            @RequestParam(required = false) Optional<String> marca,
            @RequestParam(required = false) Optional<String> categoria,
            @RequestParam(required = false) Optional<String> modelo,
            @RequestParam(required = false) Optional<Integer> minYear,
            @RequestParam(required = false) Optional<Boolean> isDelete,
            @RequestParam(required = false) Optional<Double> kmMax,
            @RequestParam(required = false) Optional<Double> precioMax,
            @RequestParam(required = false) Optional<Integer> stockMin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction

    ) {
        log.info("Buscando todos los vehiculos por los parametros: marca: {}, categoria: {}, minYear: {}, isDelete: {}, kmMax: {}, precioMax: {}, stockMin: {}, page: {}, size: {}, sortBy: {}, direction: {}",
                marca, categoria, minYear, isDelete, kmMax, precioMax, stockMin, page, size, sortBy, direction);
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(PageResponse.of(vehiculoService.findAll(marca, categoria, modelo, minYear, isDelete, kmMax, precioMax, stockMin, pageable), sortBy, direction));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vehiculo> getVehiculoById(@PathVariable UUID id) {
        log.info("Buscando vehiculo por id: " + id);
        return ResponseEntity.ok(vehiculoService.findById(id.toString()));
    }

    @PostMapping()
    public ResponseEntity<Vehiculo> createVehiculo(@Valid @RequestBody VehiculoCreateDto vehiculo) {
        log.info("Creando vehiculo: " + vehiculo);
        return ResponseEntity.ok(vehiculoService.save(vehiculo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vehiculo> updateVehiculo(@PathVariable UUID id, @Valid @RequestBody VehiculoUpdateDto vehiculoUpdateDto) {
        log.info("Actualizando vehiculo con id: " + id);
        return ResponseEntity.ok(vehiculoService.update(id.toString(), vehiculoUpdateDto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Vehiculo> patchVehiculo(@PathVariable UUID id, @Valid @RequestBody VehiculoUpdateDto vehiculoUpdateDto) {
        log.info("Actualizando vehiculo de manera parcial, con id: " + id);
        return ResponseEntity.ok(vehiculoService.update(id.toString(), vehiculoUpdateDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehiculo(@PathVariable UUID id) {
        log.info("Borrando vehiculo con id: " + id);
        vehiculoService.deleteById(id.toString());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping(value = "/image/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Vehiculo> patchVehiculoImage(
            @PathVariable UUID id, @RequestPart("image") MultipartFile image) {

        log.info("Actualizando imagen de vehiculo con id: " + id);
        List<String> permittedContentTypes = List.of("image/png", "image/jpg", "image/jpeg", "image/gif");
        try {
            String contentType = image.getContentType();
            log.info("Content type: " + contentType);
            if (!image.isEmpty() && contentType != null && permittedContentTypes.contains(contentType.toLowerCase())){
                return ResponseEntity.ok(vehiculoService.updateImage(id.toString(), image, true));
            } else {
                return ResponseEntity.badRequest().build();
            }
        }catch (Exception e){
            log.error("Error al actualizar la imagen del vehiculo con id: " + id);
            return ResponseEntity.badRequest().build();
        }
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

}
