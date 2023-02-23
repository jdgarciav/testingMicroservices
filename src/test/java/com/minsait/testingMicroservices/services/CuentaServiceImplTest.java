package com.minsait.testingMicroservices.services;

import com.minsait.testingMicroservices.models.Banco;
import com.minsait.testingMicroservices.models.Cuenta;
import com.minsait.testingMicroservices.repositories.BancoRepository;
import com.minsait.testingMicroservices.repositories.CuentaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest
class CuentaServiceImplTest {

    @Mock
    CuentaRepository cuentaRepository;
    @Mock
    BancoRepository bancoRepository;
    @InjectMocks
    CuentaServiceImpl cuentaService;



    @Test
    void findAll() {
        Cuenta cuenta1 = new Cuenta(1L, "Javier", new BigDecimal(1000));
        Cuenta cuenta2 = new Cuenta(2L, "David", new BigDecimal(2000));
        when(cuentaRepository.findAll()).thenReturn(List.of(cuenta1,cuenta2));
        List<Cuenta> cuentas = cuentaService.findAll();
        assertEquals(1L,cuentas.get(0).getId());
        assertEquals("Javier", cuentas.get(0).getPersona());
        assertEquals(1000,cuentas.get(0).getSaldo().intValue());
    }

    @Test
    void findById(){
        Long id = 1L;
        Cuenta cuenta = new Cuenta(id, "David", new BigDecimal(1000));
        when(cuentaRepository.findById(id)).thenReturn(Optional.of(cuenta));
        Cuenta cuenta1 = cuentaService.findById(id);
        assertEquals(id, cuenta1.getId());
        assertEquals("David", cuenta1.getPersona());
        assertEquals(1000, cuenta1.getSaldo().intValue());
    }

    @Test
    void revisarTotalTransferencias() {
        Long id = 1L;
        Banco banco = new Banco(id,"BBVA", 0);
        when(bancoRepository.findById(id)).thenReturn(Optional.of(banco));
        Integer totalTransferencias = cuentaService.revisarTotalTransferencias(id);
        assertEquals(0, totalTransferencias);
    }

    @Test
    void revisarSaldo() {
        Long id = 1L;
        BigDecimal expectedSaldo = new BigDecimal(1000);
        Cuenta cuenta = new Cuenta(id, "David", expectedSaldo);
        when(cuentaRepository.findById(id)).thenReturn(Optional.of(cuenta));
        BigDecimal saldo = cuentaService.revisarSaldo(id);
        assertEquals(expectedSaldo, saldo);
    }

    @Test
    void tranferir() {
        Long idOrigen = 1L;
        Long idDestino = 2L;
        Long idBanco = 1L;
        BigDecimal monto = new BigDecimal(500);
        Cuenta cuentaOrigen = new Cuenta(idOrigen, "Areli", new BigDecimal(20000));
        Cuenta cuentaDestino = new Cuenta(idDestino, "David", new BigDecimal(1000));
        Banco banco = new Banco(idBanco, "BBVA", 0);
        when(cuentaRepository.findById(idOrigen)).thenReturn(Optional.of(cuentaOrigen));
        when(cuentaRepository.findById(idDestino)).thenReturn(Optional.of(cuentaDestino));
        when(bancoRepository.findById(idBanco)).thenReturn(Optional.of(banco));
        cuentaService.tranferir(idOrigen,idDestino,monto,idBanco);
        assertEquals(idOrigen,cuentaOrigen.getId());
        assertEquals(idDestino,cuentaDestino.getId());
        assertEquals(1,banco.getTotalTransferencias());
        assertEquals(19500,cuentaOrigen.getSaldo().intValue());
        assertEquals(1500,cuentaDestino.getSaldo().intValue());
        assertEquals(idBanco, banco.getId());
        assertEquals("BBVA", banco.getNombre());
    }

    @Test
    void save() {
        String nombre = "David";
        BigDecimal monto = new BigDecimal(1000);
        Cuenta cuenta = new Cuenta(null, nombre, monto);
        when(cuentaRepository.save(any(Cuenta.class))).then(
                invocation -> {
                    Cuenta cuenta1 = invocation.getArgument(0);
                    cuenta1.setId(1L);
                    return cuenta1;
                }
        );
        Cuenta cuenta1 = cuentaService.save(cuenta);
        assertEquals(1L, cuenta1.getId());
        assertEquals(nombre, cuenta1.getPersona());
        assertEquals(monto,cuenta1.getSaldo());
    }

    @Test
    void deleteById() {
        Long id = 1L;
        Cuenta cuenta = new Cuenta(id, "David", new BigDecimal(1000));
        doReturn(Optional.of(cuenta)).when(cuentaRepository).findById(id);
        assertTrue(cuentaService.deleteById(id));
    }

    @Test
    void deleteByIdNotFound() {
        Long id = 1L;
        Cuenta cuenta = new Cuenta(id, "David", new BigDecimal(1000));
        doReturn(Optional.of(cuenta)).when(cuentaRepository).findById(id);
        assertFalse(cuentaService.deleteById(2L));
    }
}