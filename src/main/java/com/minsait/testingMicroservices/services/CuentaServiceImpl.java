package com.minsait.testingMicroservices.services;

import com.minsait.testingMicroservices.models.Banco;
import com.minsait.testingMicroservices.models.Cuenta;
import com.minsait.testingMicroservices.repositories.BancoRepository;
import com.minsait.testingMicroservices.repositories.CuentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CuentaServiceImpl implements CuentaService{
    @Autowired
    private CuentaRepository cuentaRepository;
    @Autowired
    private BancoRepository bancoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Cuenta> findAll() {
        return cuentaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Cuenta findById(Long idCuenta) {
        return cuentaRepository.findById(idCuenta).orElseThrow();
    }

    @Override
    @Transactional(readOnly = true)
    public Integer revisarTotalTransferencias(Long idBanco) {
        Banco banco = bancoRepository.findById(idBanco).orElseThrow();
        return banco.getTotalTransferencias();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal revisarSaldo(Long idCuenta) {
        return cuentaRepository.findById(idCuenta).orElseThrow().getSaldo();

    }

    @Override
    @Transactional
    public void tranferir(Long idCuentaOrigen, Long idCuentaDestino, BigDecimal monto, Long idBanco) {
        Cuenta cuentaOrigen = cuentaRepository.findById(idCuentaOrigen).orElseThrow();
        cuentaOrigen.retirar(monto);
        cuentaRepository.save(cuentaOrigen);

        Cuenta cuentaDestino = cuentaRepository.findById(idCuentaDestino).orElseThrow();
        cuentaDestino.depositar(monto);
        cuentaRepository.save(cuentaDestino);

        Banco banco = bancoRepository.findById(idBanco).orElseThrow();
        int totalTransferencias = banco.getTotalTransferencias();
        banco.setTotalTransferencias(++totalTransferencias);
        bancoRepository.save(banco);

    }

    @Override
    @Transactional
    public Cuenta save(Cuenta cuenta) {
        return cuentaRepository.save(cuenta);
    }

    @Override
    @Transactional
    public boolean deleteById(Long idCuenta) {
        Optional<Cuenta> cuenta=cuentaRepository.findById(idCuenta);
        if(cuenta.isPresent()){
            cuentaRepository.deleteById(idCuenta);
            return true;
        }
        return false;
    }
}
