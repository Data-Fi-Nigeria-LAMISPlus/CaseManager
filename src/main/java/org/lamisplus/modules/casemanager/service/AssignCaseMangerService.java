package org.lamisplus.modules.casemanager.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.casemanager.domain.AssignedPatient;
import org.lamisplus.modules.casemanager.dto.AssignCaseManagerDTO;
import org.lamisplus.modules.casemanager.repository.AsignPatientRepository;
import org.lamisplus.modules.casemanager.repository.AssignCaseManagerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssignCaseMangerService {
    private final AssignCaseManagerRepository assignCaseManagerRepository;
    private final AsignPatientRepository asignPatientRepository;

    public List<AssignedPatient> save(AssignCaseManagerDTO assignCaseManagerDTO) {
        try {

            List<AssignedPatient> assignedPatients = new ArrayList<>();

            for(AssignedPatient patient: assignCaseManagerDTO.getPatients()) {
               assignedPatients.add(dtoToEntity(patient, assignCaseManagerDTO));
            }

            return asignPatientRepository.saveAll(assignedPatients);

        }catch (Exception ignored) {
            return null;
        }
    }

    private static AssignedPatient dtoToEntity(AssignedPatient patient, AssignCaseManagerDTO assignCaseManagerDTO){
        AssignedPatient assignedPatient = new AssignedPatient();
        assignedPatient.setAge(patient.getAge());
        assignedPatient.setSex(patient.getSex());
        assignedPatient.setCurrentStatus(patient.getCurrentStatus());
        assignedPatient.setHospitalNo(patient.getHospitalNo());
        assignedPatient.setDob(patient.getDob());
        assignedPatient.setBiometricStatus(patient.getBiometricStatus());
        assignedPatient.setFullName(patient.getFullName());
        assignedPatient.setFacilityId(patient.getFacilityId());
        assignedPatient.setPersonUuid(patient.getPersonUuid());
        assignedPatient.setPhone(patient.getPhone());
        assignedPatient.setState(patient.getState());
        assignedPatient.setLga(patient.getLga());
        assignedPatient.setDate_assigned(LocalDateTime.now());
        assignedPatient.setDate_created(LocalDateTime.now());
        assignedPatient.setDate_modified(patient.getDate_modified());
        assignedPatient.setCreatedBy(patient.getCreatedBy());
        assignedPatient.setModifiedBy(patient.getModifiedBy());
        assignedPatient.setAction(patient.getAction());
        assignedPatient.setUuid(patient.getUuid());
        assignedPatient.setCreateDate(LocalDateTime.now());
        assignedPatient.setCaseManagerId(assignCaseManagerDTO.getCaseManagerId());
        return assignedPatient;
    }

    public List<AssignedPatient> findAll()
    {
        return assignCaseManagerRepository.findAll();
    }

    public List<AssignedPatient> reassign(AssignCaseManagerDTO assignCaseManagerDTO) {
        try {

            List<AssignedPatient> assignedPatients = new ArrayList<>();

            for(AssignedPatient patient: assignCaseManagerDTO.getPatients()) {
                assignedPatients.add(dtoToEntity(patient, assignCaseManagerDTO));

                AssignedPatient oldPatient = asignPatientRepository.findById(patient.getId()).orElseThrow(() -> new NoSuchElementException(
                        "Patient not found"
                ));

                asignPatientRepository.deleteById(oldPatient.getId());
            }

            return asignPatientRepository.saveAll(assignedPatients);

        }catch (Exception ignored) {
            return null;
        }
    }
    public String delete(Integer patientId) {
        asignPatientRepository.deleteById(patientId);
        return "Patient unassigned successfully";
    }

    public List<AssignedPatient> getAllAssignedByUserid(String userId) {
        return asignPatientRepository.getAssignedPatientsByUserIds(userId);
    }
}
