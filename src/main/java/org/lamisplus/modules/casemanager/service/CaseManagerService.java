package org.lamisplus.modules.casemanager.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.lamisplus.modules.casemanager.domain.CaseManager;
import org.lamisplus.modules.casemanager.dto.CaseManagerDTO;
import org.lamisplus.modules.casemanager.dto.CaseManagerRequest;
import org.lamisplus.modules.casemanager.dto.PatientListDTO;
import org.lamisplus.modules.casemanager.repository.AsignPatientRepository;
import org.lamisplus.modules.casemanager.repository.CaseManagerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CaseManagerService {
    private final AsignPatientRepository asignPatientRepository;
    private final CaseManagerRepository caseManagerRepository;

    private final CaseManagerDTOMapper caseManagerDTOMapper;

    public CaseManager Save(CaseManagerRequest caseManagerRequest) throws Exception {
        CaseManager caseManagerobj = new CaseManager();
        caseManagerobj.setDesignation(caseManagerRequest.getDesignation());
        caseManagerobj.setFirstName(caseManagerRequest.getFirstName());
        caseManagerobj.setLastName(caseManagerRequest.getLastName());
        caseManagerobj.setSex(caseManagerRequest.getSex());
        caseManagerobj.setPhoneNumber(caseManagerRequest.getPhoneNumber());
        caseManagerobj.setFacilityId(caseManagerRequest.getFacilityId());
        caseManagerobj.setAddress(caseManagerRequest.getAddress());
        caseManagerobj.setCreatedBy(caseManagerRequest.getCreated_by());
        caseManagerobj.setDateModified(caseManagerRequest.getDate_modified());
        caseManagerobj.setArchived(caseManagerRequest.getArchived());
        caseManagerobj.setActive(caseManagerRequest.getActive());
        caseManagerobj.setReligion(caseManagerRequest.getReligion());
        caseManagerobj.setUuid(UUID.randomUUID().toString());
        caseManagerobj.setCreateDate(LocalDateTime.now());
        caseManagerobj.setUsername(caseManagerRequest.getUsername());
        caseManagerobj.setPassword(caseManagerRequest.getPassword());
        caseManagerobj.setUser_id(caseManagerRequest.getUser_id());
        return caseManagerRepository.save(caseManagerobj);
    }

    public CaseManager Update(CaseManagerRequest caseManagerRequest, int id) throws Exception {

            CaseManager existingCaseManager = caseManagerRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException(
                            "Case manager not found"
                    ));

            if (existingCaseManager != null) {

                existingCaseManager.setDesignation(caseManagerRequest.getDesignation());
                existingCaseManager.setFirstName(caseManagerRequest.getFirstName());
                existingCaseManager.setLastName(caseManagerRequest.getLastName());
                existingCaseManager.setSex(caseManagerRequest.getSex());
                existingCaseManager.setPhoneNumber(caseManagerRequest.getPhoneNumber());
                existingCaseManager.setFacilityId(caseManagerRequest.getFacilityId());
                existingCaseManager.setAddress(caseManagerRequest.getAddress());
                existingCaseManager.setModifiedBy(caseManagerRequest.getModified_by());
                existingCaseManager.setDateModified(LocalDateTime.now());
                existingCaseManager.setArchived(caseManagerRequest.getArchived());
                existingCaseManager.setActive(caseManagerRequest.getActive());
                existingCaseManager.setReligion(caseManagerRequest.getReligion());
                existingCaseManager.setUsername(caseManagerRequest.getUsername());
                existingCaseManager.setPassword(caseManagerRequest.getPassword());
                existingCaseManager.setUser_id(caseManagerRequest.getUser_id());

                return caseManagerRepository.save(existingCaseManager);
            }else {
                throw new Exception(id + " not found");
            }
    }
    public CaseManagerDTO FindById(Integer id){
        return caseManagerRepository.findById(id)
                .map(caseManagerDTOMapper)
                .orElseThrow(() -> new NoSuchElementException(
                        "Case manager not found"
                ));
    }

    public String Delete(Integer id){
        caseManagerRepository.deleteById(id);
        return " deleted successfully";
    }
    public List<CaseManagerDTO> FindAll(){
        return caseManagerRepository.findAll()
                .stream()
                .map(caseManagerDTOMapper)
                .collect(Collectors.toList());
    }
    public List<PatientListDTO> getPatientListDTOS(Long facilityId, Integer stateOfResidence, Integer lgaOfResidence) {
//        LOG.info("facilityId: {}", facilityId);
//        LOG.info("stateOfResidence: {}", stateOfResidence);
//        LOG.info("lgaOfResidence: {}", lgaOfResidence);

        String facilityIdStr = facilityId != null ? facilityId.toString() : "";
        String stateStr = stateOfResidence != null ? stateOfResidence.toString() : "";
        String lgaStr = lgaOfResidence != null ? lgaOfResidence.toString() : "";


        List<PatientListDTO> patientListDTOList = asignPatientRepository.getPatientListDTOsByFacility(
                facilityIdStr,
                stateStr,
                lgaStr
        );
        LOG.info("Result size: {}", patientListDTOList.size());
        return getPatientListDTOS(facilityId, patientListDTOList);

//        if(stateOfResidence.isEmpty() && lgaOfResidence.isEmpty()){
//            System.out.println(1);
//            List<PatientListDTO> patientListDTOList = asignPatientRepository.getPatientListDTOsByFacility(facilityId);
//            return getPatientListDTOS(facilityId, patientListDTOList);
//        }
//
//
//        if(!stateOfResidence.isEmpty() && !lgaOfResidence.isEmpty() && !gender.isEmpty() ){
//            System.out.println(2);
//            List<PatientListDTO> patientListDTOList1 = asignPatientRepository.getPatientListDTOsByFacilityAndStateAndLgaAndGender(facilityId, stateOfResidence, lgaOfResidence, gender);
//            List<PatientListDTO> patientListDTOList = getPatientListDTOS(facilityId, patientListDTOList1);
//            return  getPatientListDTOS(facilityId, patientListDTOList);
//        }
//
//        if(!stateOfResidence.isEmpty() && !lgaOfResidence.isEmpty() && !targetGroup.isEmpty() ){
//            System.out.println(3);
//            List<PatientListDTO> patientListDTOList = asignPatientRepository.getPatientListDTOsByFacilityAndStateAndLgaAndTargetGroup(facilityId, stateOfResidence, lgaOfResidence, targetGroup);
//            return getPatientListDTOS(facilityId, patientListDTOList);
//        }
//
//        // state
//        if(!stateOfResidence.isEmpty() && !lgaOfResidence.isEmpty() ){
//            System.out.println(4);
//            List<PatientListDTO> patientListDTOList1 = asignPatientRepository.getPatientListDTOsByFacilityAndStateAndLga(facilityId, stateOfResidence, lgaOfResidence);
//            LOG.info("result size from db {}",patientListDTOList1.size());
//            List<PatientListDTO> patientListDTOList = getPatientListDTOS(facilityId, patientListDTOList1);
//            return  getPatientListDTOS(facilityId, patientListDTOList);
//        }
//
//        if(!stateOfResidence.isEmpty() && !gender.isEmpty()){
//            System.out.println(5);
//            List<PatientListDTO> patientListDTOList = asignPatientRepository.getPatientListDTOsByFacilityAndStateAndGender(facilityId, stateOfResidence, gender);
//            return  getPatientListDTOS(facilityId, patientListDTOList);
//        }
//
//
//        if(!stateOfResidence.isEmpty() && !targetGroup.isEmpty()){
//            System.out.println(6);
//            List<PatientListDTO> patientListDTOList = asignPatientRepository.getPatientListDTOsByFacilityAndStateAndTargetGroup(facilityId, stateOfResidence, targetGroup);
//            return  getPatientListDTOS(facilityId, patientListDTOList);
//        }
//
//
//        //lga
//
//        if( !lgaOfResidence.isEmpty() && !gender.isEmpty() ){
//            System.out.println(7);
//            List<PatientListDTO> patientListDTOList = asignPatientRepository.getPatientListDTOsByFacilityAndLgaAndGender(facilityId, lgaOfResidence, gender);
//            return  getPatientListDTOS(facilityId, patientListDTOList);
//        }
//
//
//
//        if(!lgaOfResidence.isEmpty() && !targetGroup.isEmpty()){
//            System.out.println(9);
//            List<PatientListDTO> patientListDTOList = asignPatientRepository.getPatientListDTOsByFacilityAndLgaAndTargetGroup(facilityId, lgaOfResidence, targetGroup);
//            return  getPatientListDTOS(facilityId, patientListDTOList);
//        }
//
//
//        if(!targetGroup.isEmpty() ){
//            System.out.println(11);
//            List<PatientListDTO> patientListDTOList = asignPatientRepository.getPatientListDTOsByFacilityAndTargetGroup(facilityId, targetGroup);
//            return getPatientListDTOS(facilityId, patientListDTOList);
//        }
//        if(!lgaOfResidence.isEmpty()){
//            System.out.println(12);
//            List<PatientListDTO> patientListDTOList = asignPatientRepository.getPatientListDTOsByFacilityAndLgaOfResidence(facilityId, lgaOfResidence);
//            return getPatientListDTOS(facilityId, patientListDTOList);
//        }
//
//        if(!stateOfResidence.isEmpty()){
//            System.out.println(13);
//            List<PatientListDTO> patientListDTOList = asignPatientRepository.getPatientListDTOsByFacilityAndStateOfResidence(facilityId, stateOfResidence);
//            return getPatientListDTOS(facilityId, patientListDTOList);
//        }
//
//        if(!gender.isEmpty()){
//            System.out.println(14);
//            List<PatientListDTO> patientListDTOList = asignPatientRepository.getPatientListDTOsByFacilityAndGender(facilityId, gender);
//            return getPatientListDTOS(facilityId, patientListDTOList);
//        }
//        System.out.println(15);
//        List<PatientListDTO> patientListDTOList = asignPatientRepository.getPatientListDTOs(facilityId, stateOfResidence, lgaOfResidence, gender, targetGroup);
        //return getPatientListDTOS(facilityId, patientListDTOList);
    }
    
    
    @NotNull
    private List<PatientListDTO> getPatientListDTOS(Long facilityId, List<PatientListDTO> patientListDTOList) {
        List<String> patientUuids = asignPatientRepository.getAssignedPatientPatientUuids(facilityId);
        return patientListDTOList.stream()
                .filter(patient -> !patientUuids.contains(patient.getPersonUuid()))
                .collect(Collectors.toList());
    }
    
}
