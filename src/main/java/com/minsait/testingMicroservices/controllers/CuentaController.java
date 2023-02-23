package com.minsait.testingMicroservices.controllers;

import com.minsait.testingMicroservices.exceptions.DineroInsuficienteException;
import com.minsait.testingMicroservices.models.Cuenta;
import com.minsait.testingMicroservices.models.TransferirDTO;
import com.minsait.testingMicroservices.services.CuentaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.apache.catalina.valves.AbstractAccessLogValve.*;


@RestController
@RequestMapping("/api/v1/cuentas")
@Slf4j
public class CuentaController {
    @Autowired
    private CuentaService service;

    @GetMapping("/listar")
    @ResponseStatus(HttpStatus.OK)
    public List<Cuenta> findAll(){
        log.info("Running");
        return service.findAll();
    }

    @GetMapping("/listar/{id}")
    public ResponseEntity<Cuenta> findById(@PathVariable Long id){
        try{
            Cuenta cuenta = service.findById(id);
            return ResponseEntity.ok(cuenta);
        }catch (NoSuchElementException e){
            return ResponseEntity.notFound().build();
        }
    }

    //Guardar
    @PostMapping("/guardar")
    @ResponseStatus(HttpStatus.CREATED)
    public Cuenta guardar(@RequestBody Cuenta cuenta){
        return service.save(cuenta);
    }

    @DeleteMapping("/borrar/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        if (service.deleteById(id))
            return ResponseEntity.noContent().build();
        return ResponseEntity.notFound().build();
    }

     @PutMapping("/actualizar/{id}")
     public ResponseEntity<Cuenta> actualizar(@PathVariable Long id, @RequestBody Cuenta cuenta){
        try{
            Cuenta cuenta1=service.findById(id);
            cuenta1.setSaldo(cuenta.getSaldo());
            cuenta1.setPersona(cuenta.getPersona());
            return new ResponseEntity<>(service.save(cuenta1),HttpStatus.CREATED);
        }catch (NoSuchElementException e){
            return ResponseEntity.notFound().build();
        }
     }


    @PostMapping
    public ResponseEntity<?> transferir(@RequestBody TransferirDTO dto){
        Map<String, Object> response = new HashMap<>();
        response.put("fecha", LocalDate.now().toString());
        response.put("peticion", dto);
        try{
            service.tranferir(dto.getIdCuentaOrigen(), dto.getIdCuentaDestino(), dto.getMonto(), dto.getIdBanco());
            response.put("status","OK");
            response.put("mensaje", "Transferencia realizada con exito");
        }catch (DineroInsuficienteException e){
            response.put("status", "OK");
            response.put("mensaje", e.getMessage());
        }catch (NoSuchElementException exception){
            response.put("status", "Not Found");
            response.put("mensaje","Not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(response);
    }

    //Borrar
    /*
    @GetMapping("/Borrar/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        if (service.deleteById(id)){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
*/
    //PROPIOS
    /*
    @DeleteMapping("/borrar/{id}")
    public ResponseEntity<?> borrar(@PathVariable Long id){
        if(service.deleteById(id)){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    /*public void borrar(@PathVariable Long id){
        service.deleteById(id);
    }



    //Actualizar
    @PutMapping("/actualizar/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Cuenta actualizar(@PathVariable Long id, @RequestBody Cuenta cuenta){
        Cuenta cuentaDb = service.findById(id);
        cuentaDb.setPersona(cuenta.getPersona());
        cuentaDb.setSaldo(cuenta.getSaldo());
        return service.save(cuentaDb);
    }

    //Guardar, Borrar, Actualizar


     */
}
