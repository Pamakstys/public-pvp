package com.example.pvpbackend.repositories;

import com.example.pvpbackend.models.Client;
import com.example.pvpbackend.models.Employee;
import com.example.pvpbackend.models.RegisteredRequest;
import com.example.pvpbackend.models.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegisteredRequestRepository extends JpaRepository<RegisteredRequest, Integer> {
    Optional<List<RegisteredRequest>> findByRequest(Request request);

    List<RegisteredRequest> findByClient(Client client);
    List<RegisteredRequest> findByEmployeeIsNotNull();
    List<RegisteredRequest> findByEmployeeAndBusenaIn(Employee employee, List<Integer> busenos);
    List<RegisteredRequest> findByEmployeeIsNull();
    List<RegisteredRequest> findByEmployee(Employee employee);
}
