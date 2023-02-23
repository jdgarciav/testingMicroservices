package com.minsait.testingMicroservices.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.testingMicroservices.exceptions.DineroInsuficienteException;
import com.minsait.testingMicroservices.models.Cuenta;
import com.minsait.testingMicroservices.models.TransferirDTO;
import com.minsait.testingMicroservices.services.CuentaService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
@WebMvcTest(CuentaController.class)
class CuentaControllerTest {
    private String uri = "/api/v1/cuentas";
    ObjectMapper mapper;

    @BeforeEach
    void SetUp(){
        mapper=new ObjectMapper();
    }

    @Autowired
    private MockMvc mvc;
    @MockBean
    private CuentaService service;
    @Test
    void findAll() throws Exception {
        //Given
        when(service.findAll()).thenReturn(List.of(Datos.crearCuenta1().get(),Datos.crearCuenta2().get()));

        //When
        mvc.perform(get(uri + "/listar").contentType(MediaType.APPLICATION_JSON))

        //Then
                .andExpect(jsonPath("$[0].persona").value("Ricardo"))
                .andExpect(jsonPath("$[1].persona").value("Yamani"));
    }

    @Test
    void findById() throws Exception{
        when(service.findById(1L)).thenReturn(Datos.crearCuenta1().get());
        mvc.perform(get(uri+"/listar/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                //.andExpect(jsonPath("$.persona").value("Ricardo"))
                .andExpect(jsonPath("$.persona", Matchers.is("Ricardo")))
                .andExpect(jsonPath("$.saldo").value("1000"));
    }

    @Test
    void testFindByIdIfDoesntExist() throws Exception{
        when(service.findById(1L)).thenThrow(NoSuchElementException.class);
        mvc.perform(get(uri + "/listar/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    void testguardar() throws Exception{
        Cuenta cuenta = new Cuenta(null, "Daniel", new BigDecimal(100000));
        when(service.save(any(Cuenta.class))).then(
            invocationOnMock->{
                Cuenta cuenta1=invocationOnMock.getArgument(0);
                cuenta1.setId(3L);
                return cuenta1;
            }
        );
        mvc.perform(post(uri+"/guardar").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(cuenta)))
                .andExpectAll(
                        jsonPath("$.id",Matchers.is(3)),
                        jsonPath("$.persona",Matchers.is("Daniel")),
                        jsonPath("$.saldo",Matchers.is(100000)),
                        status().isCreated()
                );
    }

    @Test
    void testActualizar() throws Exception {
        Cuenta cuenta = new Cuenta(1L, "Daniel", new BigDecimal(100000));
        when(service.findById(1L)).thenReturn(Datos.crearCuenta1().get());
        when(service.save(any())).then(
                invocationOnMock->{
                    Cuenta cuenta1=cuenta;
                    cuenta1.setId(1L);
                    return cuenta1;
                }
        );
        mvc.perform((put(uri+"/actualizar/1").contentType(MediaType.APPLICATION_JSON))
                .content(mapper.writeValueAsString(cuenta)))
                .andExpectAll(
                        jsonPath("$.id",Matchers.is(1)),
                        jsonPath("$.persona",Matchers.is("Daniel")),
                        jsonPath("$.saldo",Matchers.is(100000)),
                        status().isCreated()
                );
    }

    @Test
    void testActualizarSinExistir() throws Exception {
        Cuenta cuenta = new Cuenta(null, "Daniel", new BigDecimal(100000));
        when(service.findById(3L)).thenThrow(NoSuchElementException.class);
        mvc.perform(put(uri+"/actualizar/3").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(cuenta)))
                .andExpectAll(
                        status().isNotFound()
                );

    }

    @Test
    void testdeleteById() throws Exception{
        when(service.deleteById(1L)).thenReturn(true);
        mvc.perform(delete(uri+"/borrar/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteSinEncontrar() throws Exception{
        when(service.findById(3L)).thenReturn(Datos.crearCuenta1().get());
        mvc.perform(delete(uri+"/borrar/3").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testTransferencias()throws Exception{

        TransferirDTO dto = new TransferirDTO();
        dto.setIdCuentaOrigen(1L);
        dto.setIdCuentaDestino(2L);
        dto.setMonto(new BigDecimal(1000));
        dto.setIdBanco(1L);

        Map<String, Object> response = new HashMap<>();
        response.put("fecha", LocalDate.now().toString());
        response.put("mensaje", dto);
        response.put("status","OK");
        response.put("mensaje", "Transferencia realizada con exito");

        //When
        mvc.perform(post(uri).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpectAll(
                        status().isOk(),
                        content().json(mapper.writeValueAsString(response))
                );
    }

    @Test
    void testTransferirDineroInsuficienteException() throws Exception {
        BigDecimal monto = new BigDecimal(1001);
        Cuenta cuenta = Datos.crearCuenta1().get();
        //DineroInsuficienteException exception =new DineroInsuficienteException("Ya no tienes dinero para transferir");
        Exception exception = assertThrows(DineroInsuficienteException.class,()->cuenta.retirar(monto));
        doThrow(exception).when(service).tranferir(anyLong(),anyLong(),any(),anyLong());

        TransferirDTO dto = new TransferirDTO();
        dto.setIdCuentaOrigen(1L);
        dto.setIdCuentaDestino(2L);
        dto.setMonto(monto);
        dto.setIdBanco(1L);

        Map<String, Object> response = new HashMap<>();
        response.put("fecha", LocalDate.now().toString());
        response.put("peticion", dto);
        response.put("status","OK");
        response.put("mensaje", exception.getMessage());

        mvc.perform(post(uri).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(mapper.writeValueAsString(response)),
                        jsonPath("$.mensaje").value(exception.getMessage())
                );
    }

    @Test
    void testTransferirIdNoEncontrado()throws Exception{
        BigDecimal monto = new BigDecimal(1000);
        doThrow(NoSuchElementException.class).when(service).tranferir(anyLong(),anyLong(),any(),anyLong());

        TransferirDTO dto = new TransferirDTO();
        dto.setIdCuentaOrigen(4L);
        dto.setIdCuentaDestino(2L);
        dto.setMonto(monto);
        dto.setIdBanco(1L);

        Map<String, Object> response = new HashMap<>();
        response.put("fecha", LocalDate.now().toString());
        response.put("peticion", dto);
        response.put("status", "Not Found");
        response.put("mensaje","Not found");

        mvc.perform(post(uri).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(mapper.writeValueAsString(response))
                );

    }
/*
    @Test
    void testRetirarConSaldo() throws Exception{
        BigDecimal monto = new BigDecimal(500);
        Cuenta cuenta = Datos.crearCuenta1().get();
        cuenta.retirar(monto);

        mvc.perform(put(uri).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        (ResultMatcher) jsonPath("500",cuenta.getSaldo().toString())
                );

    }

 */

}