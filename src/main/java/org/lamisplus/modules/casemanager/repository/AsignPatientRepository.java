package org.lamisplus.modules.casemanager.repository;

import org.lamisplus.modules.casemanager.domain.AssignedPatient;
import org.lamisplus.modules.casemanager.dto.PatientListDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Repository
public interface AsignPatientRepository extends JpaRepository<AssignedPatient, Integer> {
	
	@Query(value = "SELECT person_uuid FROM case_manager_patients\n" +
			" WHERE facility_id = ?1", nativeQuery = true)
	List<String> getAssignedPatientPatientUuids(Long patientId);

	@Query(value = "SELECT cmp.* from case_manager cm\n" +
			"JOIN case_manager_patients cmp on cmp.case_manager_id = cm.id\n" +
			"WHERE user_id = ?1", nativeQuery = true)
	List<AssignedPatient> getAssignedPatientsByUserIds(String userId);
	
	@Query(value = "WITH bio_data AS (SELECT p.facility_id as facilityId, p.id, p.uuid as personUuid, CAST(p.archived as BOOLEAN) as archived, p.uuid,p.hospital_number as hospitalNumber, \n" +
			"\t\t\t  p.surname, p.first_name as firstName,\n" +
			"\t\t\t  EXTRACT(YEAR from AGE(NOW(),  date_of_birth)) as age,\n" +
			"\t\t\t  p.other_name as otherName, p.sex as gender, p.date_of_birth as dateOfBirth, \n" +
			"\t\t\t  p.date_of_registration as dateOfRegistration, p.marital_status->>'display' as maritalStatus, \n" +
			"\t\t\t  education->>'display' as education, p.employment_status->>'display' as occupation, \n" +
			"\t\t\t  facility.name as facilityName, facility_lga.name as lga, facility_state.name as state, \n" +
			"\t\t\t  boui.code as datimId, res_state.name as residentialState, res_lga.name as residentialLga,\n" +
			"\t\t\t  r.address as address, p.contact_point->'contactPoint'->0->'value'->>0 AS phone\n" +
			"\t\t\t  FROM patient_person p\n" +
			"\t\t\t  INNER JOIN (\n" +
			"\t\t\t  SELECT * FROM (SELECT p.id, REPLACE(REPLACE(REPLACE(CAST(address_object->>'line' AS text), '\\', ''), ']', ''), '[', '') AS address, \n" +
			"\t\t\tCASE WHEN address_object->>'stateId'  ~ '^\\d+(\\.\\d+)?$' THEN address_object->>'stateId' ELSE null END  AS stateId,\n" +
			"\t\t\tCASE WHEN address_object->>'district'  ~ '^\\d+(\\.\\d+)?$' THEN address_object->>'district' ELSE null END  AS lgaId\n" +
			"\t\t\t      FROM patient_person p,\n" +
			"\t\t\tjsonb_array_elements(p.address-> 'address') with ordinality l(address_object)) as result\n" +
			"\t\t\t  ) r ON r.id=p.id\n" +
			"\t\t\t INNER JOIN base_organisation_unit facility ON facility.id=facility_id\n" +
			"\t\t\t  INNER JOIN base_organisation_unit facility_lga ON facility_lga.id=facility.parent_organisation_unit_id\n" +
			"\t\t\t  INNER JOIN base_organisation_unit facility_state ON facility_state.id=facility_lga.parent_organisation_unit_id\n" +
			"\t\t\t  LEFT JOIN base_organisation_unit res_state ON res_state.id=CAST(r.stateid AS BIGINT)\n" +
			"\t\t\t  LEFT JOIN base_organisation_unit res_lga ON res_lga.id=CAST(r.lgaid AS BIGINT)\n" +
			"\t\t\t INNER JOIN base_organisation_unit_identifier boui ON boui.organisation_unit_id=facility_id\n" +
			"\t\t\t INNER JOIN hiv_enrollment h ON h.person_uuid = p.uuid\n" +
			"\t\t\tWHERE h.archived=0 AND h.facility_id=?1),\n" +
			"\t\t\t\n" +
			"\t\t\tenrollment_details AS (\n" +
			"\t\t\tSELECT h.person_uuid,h.unique_id as uniqueId, tGroup.display as targetGroup,  sar.display as statusAtRegistration, date_confirmed_hiv as dateOfConfirmedHiv,\n" +
			"\t\t\tep.display as entryPoint, date_of_registration as dateOfEnrollment\n" +
			"\t\t\tFROM hiv_enrollment h\n" +
			"\t\t\tLEFT JOIN base_application_codeset sar ON sar.id=h.status_at_registration_id\n" +
			"\t\t\tLEFT JOIN base_application_codeset tGroup ON tGroup.id=h.target_group_id\n" +
			"\t\t\tLEFT JOIN base_application_codeset ep ON ep.id=h.entry_point_id\n" +
			"\t\t\tWHERE h.archived=0 AND h.facility_id=?1) \n" +
			"\n" +
			"            Select e.*, b.*  FROM enrollment_details e\n" +
			"\t\t\tINNER JOIN bio_data b ON e.person_uuid=b.personUuid\n" +
			"\t\t    WHERE b.facilityId = ?1 AND (e.targetgroup = ?5 OR  e.targetgroup IS NULL ) AND ( b.residentiallga = ?3 OR  b.residentiallga IS NULL)\n" +
			"\t\t\t AND (b.residentialstate = ?2 OR b.residentialstate IS NULL ) AND ( b.gender =?4 OR  b.gender IS NULL )", nativeQuery = true)
	List<PatientListDTO> getPatientListDTOs(Long facilityId,
	                                        String stateOfResidence,
	                                        String lgaOfResidence,
	                                        String gender,
	                                        String targetGroup);
	
	@Query(value = "WITH address_data AS (\n" +
			"    SELECT DISTINCT ON (p.id)\n" +
			"        p.id,\n" +
			"        REPLACE(REPLACE(REPLACE(address_object->>'line', '\\', ''), ']', ''), '[', '') AS address,\n" +
			"        CASE \n" +
			"            WHEN address_object->>'stateId' ~ '^\\d+$' THEN address_object->>'stateId'\n" +
			"            ELSE NULL\n" +
			"        END AS stateId,\n" +
			"        CASE \n" +
			"            WHEN address_object->>'district' ~ '^\\d+$' THEN address_object->>'district'\n" +
			"            ELSE NULL\n" +
			"        END AS lgaId\n" +
			"    FROM patient_person p\n" +
			"    LEFT JOIN LATERAL jsonb_array_elements(p.address->'address') address_object ON TRUE\n" +
			"    ORDER BY p.id\n" +
			"),\n" +
			"\n" +
			"facility_identifier AS (\n" +
			"    SELECT DISTINCT ON (organisation_unit_id)\n" +
			"        organisation_unit_id,\n" +
			"        code\n" +
			"    FROM base_organisation_unit_identifier\n" +
			"    ORDER BY organisation_unit_id\n" +
			"),\n" +
			"\n" +
			"bio_data AS (\n" +
			"    SELECT DISTINCT ON (p.uuid)\n" +
			"        p.facility_id AS facilityId,\n" +
			"        p.id,\n" +
			"        p.uuid AS personUuid,\n" +
			"        p.hospital_number AS hospitalNumber,\n" +
			"        p.surname,\n" +
			"        p.first_name AS firstName,\n" +
			"        EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) AS age,\n" +
			"        p.sex AS gender,\n" +
			"        facility.name AS facilityName,\n" +
			"        fi.code AS datimId,\n" +
			"        ad.address\n" +
			"    FROM patient_person p\n" +
			"    LEFT JOIN address_data ad ON ad.id = p.id\n" +
			"    INNER JOIN base_organisation_unit facility ON facility.id = p.facility_id\n" +
			"    LEFT JOIN facility_identifier fi ON fi.organisation_unit_id = p.facility_id\n" +
			"    INNER JOIN hiv_enrollment h ON h.person_uuid = p.uuid\n" +
			"    WHERE h.archived = 0\n" +
			"      AND h.facility_id = ?1\n" +
			"      AND NOT EXISTS (\n" +
			"          SELECT 1\n" +
			"          FROM case_manager_patients cmp\n" +
			"          WHERE cmp.person_uuid = p.uuid\n" +
			"             OR cmp.hospital_no = p.hospital_number\n" +
			"      )\n" +
			"\n" +
			"    ORDER BY p.uuid\n" +
			"),\n" +
			"\n" +
			"enrollment_details AS (\n" +
			"    SELECT DISTINCT ON (h.person_uuid)\n" +
			"        h.person_uuid,\n" +
			"        h.unique_id AS uniqueId,\n" +
			"        h.date_of_registration AS dateOfEnrollment\n" +
			"    FROM hiv_enrollment h\n" +
			"    WHERE h.archived = 0\n" +
			"      AND h.facility_id = ?1\n" +
			"    ORDER BY h.person_uuid, h.date_of_registration DESC\n" +
			")\n" +
			"\n" +
			"SELECT DISTINCT ON (b.personUuid)\n" +
			"    e.*,\n" +
			"    b.*\n" +
			"FROM bio_data b\n" +
			"INNER JOIN enrollment_details e\n" +
			"    ON e.person_uuid = b.personUuid\n" +
			"ORDER BY b.personUuid;", nativeQuery = true)
	List<PatientListDTO> getPatientListDTOsByFacility(Long facilityId);
	
	@Query(value = "WITH bio_data AS (SELECT p.facility_id as facilityId, p.id, p.uuid as personUuid, CAST(p.archived as BOOLEAN) as archived, p.uuid,p.hospital_number as hospitalNumber, \n" +
			"\t\t\t  p.surname, p.first_name as firstName,\n" +
			"\t\t\t  EXTRACT(YEAR from AGE(NOW(),  date_of_birth)) as age,\n" +
			"\t\t\t  p.other_name as otherName, p.sex as gender, p.date_of_birth as dateOfBirth, \n" +
			"\t\t\t  p.date_of_registration as dateOfRegistration, p.marital_status->>'display' as maritalStatus, \n" +
			"\t\t\t  education->>'display' as education, p.employment_status->>'display' as occupation, \n" +
			"\t\t\t  facility.name as facilityName, facility_lga.name as lga, facility_state.name as state, \n" +
			"\t\t\t  boui.code as datimId, res_state.name as residentialState, res_lga.name as residentialLga,\n" +
			"\t\t\t  r.address as address, p.contact_point->'contactPoint'->0->'value'->>0 AS phone\n" +
			"\t\t\t  FROM patient_person p\n" +
			"\t\t\t  INNER JOIN (\n" +
			"\t\t\t  SELECT * FROM (SELECT p.id, REPLACE(REPLACE(REPLACE(CAST(address_object->>'line' AS text), '\\', ''), ']', ''), '[', '') AS address, \n" +
			"\t\t\tCASE WHEN address_object->>'stateId'  ~ '^\\d+(\\.\\d+)?$' THEN address_object->>'stateId' ELSE null END  AS stateId,\n" +
			"\t\t\tCASE WHEN address_object->>'district'  ~ '^\\d+(\\.\\d+)?$' THEN address_object->>'district' ELSE null END  AS lgaId\n" +
			"\t\t\t      FROM patient_person p,\n" +
			"\t\t\tjsonb_array_elements(p.address-> 'address') with ordinality l(address_object)) as result\n" +
			"\t\t\t  ) r ON r.id=p.id\n" +
			"\t\t\t INNER JOIN base_organisation_unit facility ON facility.id=facility_id\n" +
			"\t\t\t  INNER JOIN base_organisation_unit facility_lga ON facility_lga.id=facility.parent_organisation_unit_id\n" +
			"\t\t\t  INNER JOIN base_organisation_unit facility_state ON facility_state.id=facility_lga.parent_organisation_unit_id\n" +
			"\t\t\t  LEFT JOIN base_organisation_unit res_state ON res_state.id=CAST(r.stateid AS BIGINT)\n" +
			"\t\t\t  LEFT JOIN base_organisation_unit res_lga ON res_lga.id=CAST(r.lgaid AS BIGINT)\n" +
			"\t\t\t INNER JOIN base_organisation_unit_identifier boui ON boui.organisation_unit_id=facility_id\n" +
			"\t\t\t INNER JOIN hiv_enrollment h ON h.person_uuid = p.uuid\n" +
			"\t\t\tWHERE h.archived=0 AND h.facility_id=?1),\n" +
			"\t\t\t\n" +
			"\t\t\tenrollment_details AS (\n" +
			"\t\t\tSELECT h.person_uuid,h.unique_id as uniqueId, tGroup.display as targetGroup,  sar.display as statusAtRegistration, date_confirmed_hiv as dateOfConfirmedHiv,\n" +
			"\t\t\tep.display as entryPoint, date_of_registration as dateOfEnrollment\n" +
			"\t\t\tFROM hiv_enrollment h\n" +
			"\t\t\tLEFT JOIN base_application_codeset sar ON sar.id=h.status_at_registration_id\n" +
			"\t\t\tLEFT JOIN base_application_codeset tGroup ON tGroup.id=h.target_group_id\n" +
			"\t\t\tLEFT JOIN base_application_codeset ep ON ep.id=h.entry_point_id\n" +
			"\t\t\tWHERE h.archived=0 AND h.facility_id=?1) \n" +
			"\n" +
			"            Select e.*, b.*  FROM enrollment_details e\n" +
			"\t\t\tINNER JOIN bio_data b ON e.person_uuid=b.personUuid\n" +
			"\t\t    WHERE b.facilityId = ?1 AND e.targetgroup = ?2 ", nativeQuery = true)
	List<PatientListDTO> getPatientListDTOsByFacilityAndTargetGroup(Long facilityId, String targetGroup);
	
	@Query(value = "WITH bio_data AS (SELECT p.facility_id as facilityId, p.id, p.uuid as personUuid, CAST(p.archived as BOOLEAN) as archived, p.uuid,p.hospital_number as hospitalNumber, \n" +
			"\t\t\t  p.surname, p.first_name as firstName,\n" +
			"\t\t\t  EXTRACT(YEAR from AGE(NOW(),  date_of_birth)) as age,\n" +
			"\t\t\t  p.other_name as otherName, p.sex as gender, p.date_of_birth as dateOfBirth, \n" +
			"\t\t\t  p.date_of_registration as dateOfRegistration, p.marital_status->>'display' as maritalStatus, \n" +
			"\t\t\t  education->>'display' as education, p.employment_status->>'display' as occupation, \n" +
			"\t\t\t  facility.name as facilityName, facility_lga.name as lga, facility_state.name as state, \n" +
			"\t\t\t  boui.code as datimId, res_state.name as residentialState, res_lga.name as residentialLga,\n" +
			"\t\t\t  r.address as address, p.contact_point->'contactPoint'->0->'value'->>0 AS phone\n" +
			"\t\t\t  FROM patient_person p\n" +
			"\t\t\t  INNER JOIN (\n" +
			"\t\t\t  SELECT * FROM (SELECT p.id, REPLACE(REPLACE(REPLACE(CAST(address_object->>'line' AS text), '\\', ''), ']', ''), '[', '') AS address, \n" +
			"\t\t\tCASE WHEN address_object->>'stateId'  ~ '^\\d+(\\.\\d+)?$' THEN address_object->>'stateId' ELSE null END  AS stateId,\n" +
			"\t\t\tCASE WHEN address_object->>'district'  ~ '^\\d+(\\.\\d+)?$' THEN address_object->>'district' ELSE null END  AS lgaId\n" +
			"\t\t\t      FROM patient_person p,\n" +
			"\t\t\tjsonb_array_elements(p.address-> 'address') with ordinality l(address_object)) as result\n" +
			"\t\t\t  ) r ON r.id=p.id\n" +
			"\t\t\t INNER JOIN base_organisation_unit facility ON facility.id=facility_id\n" +
			"\t\t\t  INNER JOIN base_organisation_unit facility_lga ON facility_lga.id=facility.parent_organisation_unit_id\n" +
			"\t\t\t  INNER JOIN base_organisation_unit facility_state ON facility_state.id=facility_lga.parent_organisation_unit_id\n" +
			"\t\t\t  LEFT JOIN base_organisation_unit res_state ON res_state.id=CAST(r.stateid AS BIGINT)\n" +
			"\t\t\t  LEFT JOIN base_organisation_unit res_lga ON res_lga.id=CAST(r.lgaid AS BIGINT)\n" +
			"\t\t\t INNER JOIN base_organisation_unit_identifier boui ON boui.organisation_unit_id=facility_id\n" +
			"\t\t\t INNER JOIN hiv_enrollment h ON h.person_uuid = p.uuid\n" +
			"\t\t\tWHERE h.archived=0 AND h.facility_id=?1),\n" +
			"\t\t\t\n" +
			"\t\t\tenrollment_details AS (\n" +
			"\t\t\tSELECT h.person_uuid,h.unique_id as uniqueId, tGroup.display as targetGroup,  sar.display as statusAtRegistration, date_confirmed_hiv as dateOfConfirmedHiv,\n" +
			"\t\t\tep.display as entryPoint, date_of_registration as dateOfEnrollment\n" +
			"\t\t\tFROM hiv_enrollment h\n" +
			"\t\t\tLEFT JOIN base_application_codeset sar ON sar.id=h.status_at_registration_id\n" +
			"\t\t\tLEFT JOIN base_application_codeset tGroup ON tGroup.id=h.target_group_id\n" +
			"\t\t\tLEFT JOIN base_application_codeset ep ON ep.id=h.entry_point_id\n" +
			"\t\t\tWHERE h.archived=0 AND h.facility_id=?1) \n" +
			"\n" +
			"            Select e.*, b.*  FROM enrollment_details e\n" +
			"\t\t\tINNER JOIN bio_data b ON e.person_uuid=b.personUuid\n" +
			"\t\t    WHERE b.facilityId = ?1 AND b.residentialstate = ?2 ", nativeQuery = true)
	List<PatientListDTO> getPatientListDTOsByFacilityAndStateOfResidence(Long facilityId, String stateOfResidence);
	
	@Query(value = "WITH bio_data AS (SELECT p.facility_id as facilityId, p.id, p.uuid as personUuid, CAST(p.archived as BOOLEAN) as archived, p.uuid,p.hospital_number as hospitalNumber, \n" +
			"\t\t\t  p.surname, p.first_name as firstName,\n" +
			"\t\t\t  EXTRACT(YEAR from AGE(NOW(),  date_of_birth)) as age,\n" +
			"\t\t\t  p.other_name as otherName, p.sex as gender, p.date_of_birth as dateOfBirth, \n" +
			"\t\t\t  p.date_of_registration as dateOfRegistration, p.marital_status->>'display' as maritalStatus, \n" +
			"\t\t\t  education->>'display' as education, p.employment_status->>'display' as occupation, \n" +
			"\t\t\t  facility.name as facilityName, facility_lga.name as lga, facility_state.name as state, \n" +
			"\t\t\t  boui.code as datimId, res_state.name as residentialState, res_lga.name as residentialLga,\n" +
			"\t\t\t  r.address as address, p.contact_point->'contactPoint'->0->'value'->>0 AS phone\n" +
			"\t\t\t  FROM patient_person p\n" +
			"\t\t\t  INNER JOIN (\n" +
			"\t\t\t  SELECT * FROM (SELECT p.id, REPLACE(REPLACE(REPLACE(CAST(address_object->>'line' AS text), '\\', ''), ']', ''), '[', '') AS address, \n" +
			"\t\t\tCASE WHEN address_object->>'stateId'  ~ '^\\d+(\\.\\d+)?$' THEN address_object->>'stateId' ELSE null END  AS stateId,\n" +
			"\t\t\tCASE WHEN address_object->>'district'  ~ '^\\d+(\\.\\d+)?$' THEN address_object->>'district' ELSE null END  AS lgaId\n" +
			"\t\t\t      FROM patient_person p,\n" +
			"\t\t\tjsonb_array_elements(p.address-> 'address') with ordinality l(address_object)) as result\n" +
			"\t\t\t  ) r ON r.id=p.id\n" +
			"\t\t\t INNER JOIN base_organisation_unit facility ON facility.id=facility_id\n" +
			"\t\t\t  INNER JOIN base_organisation_unit facility_lga ON facility_lga.id=facility.parent_organisation_unit_id\n" +
			"\t\t\t  INNER JOIN base_organisation_unit facility_state ON facility_state.id=facility_lga.parent_organisation_unit_id\n" +
			"\t\t\t  LEFT JOIN base_organisation_unit res_state ON res_state.id=CAST(r.stateid AS BIGINT)\n" +
			"\t\t\t  LEFT JOIN base_organisation_unit res_lga ON res_lga.id=CAST(r.lgaid AS BIGINT)\n" +
			"\t\t\t INNER JOIN base_organisation_unit_identifier boui ON boui.organisation_unit_id=facility_id\n" +
			"\t\t\t INNER JOIN hiv_enrollment h ON h.person_uuid = p.uuid\n" +
			"\t\t\tWHERE h.archived=0 AND h.facility_id=?1),\n" +
			"\t\t\t\n" +
			"\t\t\tenrollment_details AS (\n" +
			"\t\t\tSELECT h.person_uuid,h.unique_id as uniqueId, tGroup.display as targetGroup,  sar.display as statusAtRegistration, date_confirmed_hiv as dateOfConfirmedHiv,\n" +
			"\t\t\tep.display as entryPoint, date_of_registration as dateOfEnrollment\n" +
			"\t\t\tFROM hiv_enrollment h\n" +
			"\t\t\tLEFT JOIN base_application_codeset sar ON sar.id=h.status_at_registration_id\n" +
			"\t\t\tLEFT JOIN base_application_codeset tGroup ON tGroup.id=h.target_group_id\n" +
			"\t\t\tLEFT JOIN base_application_codeset ep ON ep.id=h.entry_point_id\n" +
			"\t\t\tWHERE h.archived=0 AND h.facility_id=?1) \n" +
			"\n" +
			"            Select e.*, b.*  FROM enrollment_details e\n" +
			"\t\t\tINNER JOIN bio_data b ON e.person_uuid=b.personUuid\n" +
			"\t\t    WHERE b.facilityId = ?1 AND b.residentiallga = ?2 ", nativeQuery = true)
	List<PatientListDTO> getPatientListDTOsByFacilityAndLgaOfResidence(Long facilityId, String lgaOfResidence);
	
	@Query(value = "WITH bio_data AS (SELECT p.facility_id as facilityId, p.id, p.uuid as personUuid, CAST(p.archived as BOOLEAN) as archived, p.uuid,p.hospital_number as hospitalNumber, \n" +
			"\t\t\t  p.surname, p.first_name as firstName,\n" +
			"\t\t\t  EXTRACT(YEAR from AGE(NOW(),  date_of_birth)) as age,\n" +
			"\t\t\t  p.other_name as otherName, p.sex as gender, p.date_of_birth as dateOfBirth, \n" +
			"\t\t\t  p.date_of_registration as dateOfRegistration, p.marital_status->>'display' as maritalStatus, \n" +
			"\t\t\t  education->>'display' as education, p.employment_status->>'display' as occupation, \n" +
			"\t\t\t  facility.name as facilityName, facility_lga.name as lga, facility_state.name as state, \n" +
			"\t\t\t  boui.code as datimId, res_state.name as residentialState, res_lga.name as residentialLga,\n" +
			"\t\t\t  r.address as address, p.contact_point->'contactPoint'->0->'value'->>0 AS phone\n" +
			"\t\t\t  FROM patient_person p\n" +
			"\t\t\t  INNER JOIN (\n" +
			"\t\t\t  SELECT * FROM (SELECT p.id, REPLACE(REPLACE(REPLACE(CAST(address_object->>'line' AS text), '\\', ''), ']', ''), '[', '') AS address, \n" +
			"\t\t\tCASE WHEN address_object->>'stateId'  ~ '^\\d+(\\.\\d+)?$' THEN address_object->>'stateId' ELSE null END  AS stateId,\n" +
			"\t\t\tCASE WHEN address_object->>'district'  ~ '^\\d+(\\.\\d+)?$' THEN address_object->>'district' ELSE null END  AS lgaId\n" +
			"\t\t\t      FROM patient_person p,\n" +
			"\t\t\tjsonb_array_elements(p.address-> 'address') with ordinality l(address_object)) as result\n" +
			"\t\t\t  ) r ON r.id=p.id\n" +
			"\t\t\t INNER JOIN base_organisation_unit facility ON facility.id=facility_id\n" +
			"\t\t\t  INNER JOIN base_organisation_unit facility_lga ON facility_lga.id=facility.parent_organisation_unit_id\n" +
			"\t\t\t  INNER JOIN base_organisation_unit facility_state ON facility_state.id=facility_lga.parent_organisation_unit_id\n" +
			"\t\t\t  LEFT JOIN base_organisation_unit res_state ON res_state.id=CAST(r.stateid AS BIGINT)\n" +
			"\t\t\t  LEFT JOIN base_organisation_unit res_lga ON res_lga.id=CAST(r.lgaid AS BIGINT)\n" +
			"\t\t\t INNER JOIN base_organisation_unit_identifier boui ON boui.organisation_unit_id=facility_id\n" +
			"\t\t\t INNER JOIN hiv_enrollment h ON h.person_uuid = p.uuid\n" +
			"\t\t\tWHERE h.archived=0 AND h.facility_id=?1),\n" +
			"\t\t\t\n" +
			"\t\t\tenrollment_details AS (\n" +
			"\t\t\tSELECT h.person_uuid,h.unique_id as uniqueId, tGroup.display as targetGroup,  sar.display as statusAtRegistration, date_confirmed_hiv as dateOfConfirmedHiv,\n" +
			"\t\t\tep.display as entryPoint, date_of_registration as dateOfEnrollment\n" +
			"\t\t\tFROM hiv_enrollment h\n" +
			"\t\t\tLEFT JOIN base_application_codeset sar ON sar.id=h.status_at_registration_id\n" +
			"\t\t\tLEFT JOIN base_application_codeset tGroup ON tGroup.id=h.target_group_id\n" +
			"\t\t\tLEFT JOIN base_application_codeset ep ON ep.id=h.entry_point_id\n" +
			"\t\t\tWHERE h.archived=0 AND h.facility_id=?1) \n" +
			"\n" +
			"            Select e.*, b.*  FROM enrollment_details e\n" +
			"\t\t\tINNER JOIN bio_data b ON e.person_uuid=b.personUuid\n" +
			"\t\t    WHERE b.facilityId = ?1 AND b.gender =?2 ", nativeQuery = true)
	List<PatientListDTO> getPatientListDTOsByFacilityAndGender(Long facilityId, String gender);
	
	
	@Query(value = "WITH bio_data AS (SELECT p.facility_id as facilityId, p.id, p.uuid as personUuid, CAST(p.archived as BOOLEAN) as archived, p.uuid,p.hospital_number as hospitalNumber, \n" +
			"\t\t\t  p.surname, p.first_name as firstName,\n" +
			"\t\t\t  EXTRACT(YEAR from AGE(NOW(),  date_of_birth)) as age,\n" +
			"\t\t\t  p.other_name as otherName, p.sex as gender, p.date_of_birth as dateOfBirth, \n" +
			"\t\t\t  p.date_of_registration as dateOfRegistration, p.marital_status->>'display' as maritalStatus, \n" +
			"\t\t\t  education->>'display' as education, p.employment_status->>'display' as occupation, \n" +
			"\t\t\t  facility.name as facilityName, facility_lga.name as lga, facility_state.name as state, \n" +
			"\t\t\t  boui.code as datimId, res_state.name as residentialState, res_lga.name as residentialLga,\n" +
			"\t\t\t  r.address as address, p.contact_point->'contactPoint'->0->'value'->>0 AS phone\n" +
			"\t\t\t  FROM patient_person p\n" +
			"\t\t\t  INNER JOIN (\n" +
			"\t\t\t  SELECT * FROM (SELECT p.id, REPLACE(REPLACE(REPLACE(CAST(address_object->>'line' AS text), '\\', ''), ']', ''), '[', '') AS address, \n" +
			"\t\t\tCASE WHEN address_object->>'stateId'  ~ '^\\d+(\\.\\d+)?$' THEN address_object->>'stateId' ELSE null END  AS stateId,\n" +
			"\t\t\tCASE WHEN address_object->>'district'  ~ '^\\d+(\\.\\d+)?$' THEN address_object->>'district' ELSE null END  AS lgaId\n" +
			"\t\t\t      FROM patient_person p,\n" +
			"\t\t\tjsonb_array_elements(p.address-> 'address') with ordinality l(address_object)) as result\n" +
			"\t\t\t  ) r ON r.id=p.id\n" +
			"\t\t\t INNER JOIN base_organisation_unit facility ON facility.id=facility_id\n" +
			"\t\t\t  INNER JOIN base_organisation_unit facility_lga ON facility_lga.id=facility.parent_organisation_unit_id\n" +
			"\t\t\t  INNER JOIN base_organisation_unit facility_state ON facility_state.id=facility_lga.parent_organisation_unit_id\n" +
			"\t\t\t  LEFT JOIN base_organisation_unit res_state ON res_state.id=CAST(r.stateid AS BIGINT)\n" +
			"\t\t\t  LEFT JOIN base_organisation_unit res_lga ON res_lga.id=CAST(r.lgaid AS BIGINT)\n" +
			"\t\t\t INNER JOIN base_organisation_unit_identifier boui ON boui.organisation_unit_id=facility_id\n" +
			"\t\t\t INNER JOIN hiv_enrollment h ON h.person_uuid = p.uuid\n" +
			"\t\t\tWHERE h.archived=0 AND h.facility_id=?1),\n" +
			"\t\t\t\n" +
			"\t\t\tenrollment_details AS (\n" +
			"\t\t\tSELECT h.person_uuid,h.unique_id as uniqueId, tGroup.display as targetGroup,  sar.display as statusAtRegistration, date_confirmed_hiv as dateOfConfirmedHiv,\n" +
			"\t\t\tep.display as entryPoint, date_of_registration as dateOfEnrollment\n" +
			"\t\t\tFROM hiv_enrollment h\n" +
			"\t\t\tLEFT JOIN base_application_codeset sar ON sar.id=h.status_at_registration_id\n" +
			"\t\t\tLEFT JOIN base_application_codeset tGroup ON tGroup.id=h.target_group_id\n" +
			"\t\t\tLEFT JOIN base_application_codeset ep ON ep.id=h.entry_point_id\n" +
			"\t\t\tWHERE h.archived=0 AND h.facility_id=?1) \n" +
			"\n" +
			"            Select e.*, b.*  FROM enrollment_details e\n" +
			"\t\t\tINNER JOIN bio_data b ON e.person_uuid=b.personUuid\n" +
			"\t\t    WHERE b.facilityId = ?1 AND ( b.residentiallga = ?3 OR  b.residentiallga IS NULL)\n" +
			"\t\t\t AND (b.residentialstate = ?2 OR b.residentialstate IS NULL )", nativeQuery = true)
	List<PatientListDTO> getPatientListDTOsByFacilityAndStateAndLga(Long facilityId,
	                                        String stateOfResidence,
	                                        String lgaOfResidence);
	
	
	@Query(value = "WITH bio_data AS (SELECT p.facility_id as facilityId, p.id, p.uuid as personUuid, CAST(p.archived as BOOLEAN) as archived, p.uuid,p.hospital_number as hospitalNumber, \n" +
			"\t\t\t  p.surname, p.first_name as firstName,\n" +
			"\t\t\t  EXTRACT(YEAR from AGE(NOW(),  date_of_birth)) as age,\n" +
			"\t\t\t  p.other_name as otherName, p.sex as gender, p.date_of_birth as dateOfBirth, \n" +
			"\t\t\t  p.date_of_registration as dateOfRegistration, p.marital_status->>'display' as maritalStatus, \n" +
			"\t\t\t  education->>'display' as education, p.employment_status->>'display' as occupation, \n" +
			"\t\t\t  facility.name as facilityName, facility_lga.name as lga, facility_state.name as state, \n" +
			"\t\t\t  boui.code as datimId, res_state.name as residentialState, res_lga.name as residentialLga,\n" +
			"\t\t\t  r.address as address, p.contact_point->'contactPoint'->0->'value'->>0 AS phone\n" +
			"\t\t\t  FROM patient_person p\n" +
			"\t\t\t  INNER JOIN (\n" +
			"\t\t\t  SELECT * FROM (SELECT p.id, REPLACE(REPLACE(REPLACE(CAST(address_object->>'line' AS text), '\\', ''), ']', ''), '[', '') AS address, \n" +
			"\t\t\tCASE WHEN address_object->>'stateId'  ~ '^\\d+(\\.\\d+)?$' THEN address_object->>'stateId' ELSE null END  AS stateId,\n" +
			"\t\t\tCASE WHEN address_object->>'district'  ~ '^\\d+(\\.\\d+)?$' THEN address_object->>'district' ELSE null END  AS lgaId\n" +
			"\t\t\t      FROM patient_person p,\n" +
			"\t\t\tjsonb_array_elements(p.address-> 'address') with ordinality l(address_object)) as result\n" +
			"\t\t\t  ) r ON r.id=p.id\n" +
			"\t\t\t INNER JOIN base_organisation_unit facility ON facility.id=facility_id\n" +
			"\t\t\t  INNER JOIN base_organisation_unit facility_lga ON facility_lga.id=facility.parent_organisation_unit_id\n" +
			"\t\t\t  INNER JOIN base_organisation_unit facility_state ON facility_state.id=facility_lga.parent_organisation_unit_id\n" +
			"\t\t\t  LEFT JOIN base_organisation_unit res_state ON res_state.id=CAST(r.stateid AS BIGINT)\n" +
			"\t\t\t  LEFT JOIN base_organisation_unit res_lga ON res_lga.id=CAST(r.lgaid AS BIGINT)\n" +
			"\t\t\t INNER JOIN base_organisation_unit_identifier boui ON boui.organisation_unit_id=facility_id\n" +
			"\t\t\t INNER JOIN hiv_enrollment h ON h.person_uuid = p.uuid\n" +
			"\t\t\tWHERE h.archived=0 AND h.facility_id=?1),\n" +
			"\t\t\t\n" +
			"\t\t\tenrollment_details AS (\n" +
			"\t\t\tSELECT h.person_uuid,h.unique_id as uniqueId, tGroup.display as targetGroup,  sar.display as statusAtRegistration, date_confirmed_hiv as dateOfConfirmedHiv,\n" +
			"\t\t\tep.display as entryPoint, date_of_registration as dateOfEnrollment\n" +
			"\t\t\tFROM hiv_enrollment h\n" +
			"\t\t\tLEFT JOIN base_application_codeset sar ON sar.id=h.status_at_registration_id\n" +
			"\t\t\tLEFT JOIN base_application_codeset tGroup ON tGroup.id=h.target_group_id\n" +
			"\t\t\tLEFT JOIN base_application_codeset ep ON ep.id=h.entry_point_id\n" +
			"\t\t\tWHERE h.archived=0 AND h.facility_id=?1) \n" +
			"\n" +
			"            Select e.*, b.*  FROM enrollment_details e\n" +
			"\t\t\tINNER JOIN bio_data b ON e.person_uuid=b.personUuid\n" +
			"\t\t    WHERE b.facilityId = ?1 AND ( b.residentiallga = ?3 OR  b.residentiallga IS NULL)\n" +
			"\t\t\t AND (b.residentialstate = ?2 OR b.residentialstate IS NULL ) AND ( b.gender =?4 OR  b.gender IS NULL )", nativeQuery = true)
	List<PatientListDTO> getPatientListDTOsByFacilityAndStateAndLgaAndGender(Long facilityId,
	                                        String stateOfResidence,
	                                        String lgaOfResidence,
	                                        String gender
	                                       );
	@Query(value = "WITH bio_data AS (SELECT p.facility_id as facilityId, p.id, p.uuid as personUuid, CAST(p.archived as BOOLEAN) as archived, p.uuid,p.hospital_number as hospitalNumber, \n" +
			"\t\t\t  p.surname, p.first_name as firstName,\n" +
			"\t\t\t  EXTRACT(YEAR from AGE(NOW(),  date_of_birth)) as age,\n" +
			"\t\t\t  p.other_name as otherName, p.sex as gender, p.date_of_birth as dateOfBirth, \n" +
			"\t\t\t  p.date_of_registration as dateOfRegistration, p.marital_status->>'display' as maritalStatus, \n" +
			"\t\t\t  education->>'display' as education, p.employment_status->>'display' as occupation, \n" +
			"\t\t\t  facility.name as facilityName, facility_lga.name as lga, facility_state.name as state, \n" +
			"\t\t\t  boui.code as datimId, res_state.name as residentialState, res_lga.name as residentialLga,\n" +
			"\t\t\t  r.address as address, p.contact_point->'contactPoint'->0->'value'->>0 AS phone\n" +
			"\t\t\t  FROM patient_person p\n" +
			"\t\t\t  INNER JOIN (\n" +
			"\t\t\t  SELECT * FROM (SELECT p.id, REPLACE(REPLACE(REPLACE(CAST(address_object->>'line' AS text), '\\', ''), ']', ''), '[', '') AS address, \n" +
			"\t\t\tCASE WHEN address_object->>'stateId'  ~ '^\\d+(\\.\\d+)?$' THEN address_object->>'stateId' ELSE null END  AS stateId,\n" +
			"\t\t\tCASE WHEN address_object->>'district'  ~ '^\\d+(\\.\\d+)?$' THEN address_object->>'district' ELSE null END  AS lgaId\n" +
			"\t\t\t      FROM patient_person p,\n" +
			"\t\t\tjsonb_array_elements(p.address-> 'address') with ordinality l(address_object)) as result\n" +
			"\t\t\t  ) r ON r.id=p.id\n" +
			"\t\t\t INNER JOIN base_organisation_unit facility ON facility.id=facility_id\n" +
			"\t\t\t  INNER JOIN base_organisation_unit facility_lga ON facility_lga.id=facility.parent_organisation_unit_id\n" +
			"\t\t\t  INNER JOIN base_organisation_unit facility_state ON facility_state.id=facility_lga.parent_organisation_unit_id\n" +
			"\t\t\t  LEFT JOIN base_organisation_unit res_state ON res_state.id=CAST(r.stateid AS BIGINT)\n" +
			"\t\t\t  LEFT JOIN base_organisation_unit res_lga ON res_lga.id=CAST(r.lgaid AS BIGINT)\n" +
			"\t\t\t INNER JOIN base_organisation_unit_identifier boui ON boui.organisation_unit_id=facility_id\n" +
			"\t\t\t INNER JOIN hiv_enrollment h ON h.person_uuid = p.uuid\n" +
			"\t\t\tWHERE h.archived=0 AND h.facility_id=?1),\n" +
			"\t\t\t\n" +
			"\t\t\tenrollment_details AS (\n" +
			"\t\t\tSELECT h.person_uuid,h.unique_id as uniqueId, tGroup.display as targetGroup,  sar.display as statusAtRegistration, date_confirmed_hiv as dateOfConfirmedHiv,\n" +
			"\t\t\tep.display as entryPoint, date_of_registration as dateOfEnrollment\n" +
			"\t\t\tFROM hiv_enrollment h\n" +
			"\t\t\tLEFT JOIN base_application_codeset sar ON sar.id=h.status_at_registration_id\n" +
			"\t\t\tLEFT JOIN base_application_codeset tGroup ON tGroup.id=h.target_group_id\n" +
			"\t\t\tLEFT JOIN base_application_codeset ep ON ep.id=h.entry_point_id\n" +
			"\t\t\tWHERE h.archived=0 AND h.facility_id=?1) \n" +
			"\n" +
			"            Select e.*, b.*  FROM enrollment_details e\n" +
			"\t\t\tINNER JOIN bio_data b ON e.person_uuid=b.personUuid\n" +
			"\t\t    WHERE b.facilityId = ?1 AND (e.targetgroup = ?4 OR e.targetgroup IS NULL ) AND ( b.residentiallga = ?3 OR  b.residentiallga IS NULL)\n" +
			"\t\t\t AND (b.residentialstate = ?2 OR b.residentialstate IS NULL )", nativeQuery = true)
	List<PatientListDTO> getPatientListDTOsByFacilityAndStateAndLgaAndTargetGroup(Long facilityId,
	                                        String stateOfResidence,
	                                        String lgaOfResidence,
	                                        String targetGroup);
	
	@Query(value = "WITH bio_data AS (SELECT p.facility_id as facilityId, p.id, p.uuid as personUuid, CAST(p.archived as BOOLEAN) as archived, p.uuid,p.hospital_number as hospitalNumber, \n" +
			"\t\t\t  p.surname, p.first_name as firstName,\n" +
			"\t\t\t  EXTRACT(YEAR from AGE(NOW(),  date_of_birth)) as age,\n" +
			"\t\t\t  p.other_name as otherName, p.sex as gender, p.date_of_birth as dateOfBirth, \n" +
			"\t\t\t  p.date_of_registration as dateOfRegistration, p.marital_status->>'display' as maritalStatus, \n" +
			"\t\t\t  education->>'display' as education, p.employment_status->>'display' as occupation, \n" +
			"\t\t\t  facility.name as facilityName, facility_lga.name as lga, facility_state.name as state, \n" +
			"\t\t\t  boui.code as datimId, res_state.name as residentialState, res_lga.name as residentialLga,\n" +
			"\t\t\t  r.address as address, p.contact_point->'contactPoint'->0->'value'->>0 AS phone\n" +
			"\t\t\t  FROM patient_person p\n" +
			"\t\t\t  INNER JOIN (\n" +
			"\t\t\t  SELECT * FROM (SELECT p.id, REPLACE(REPLACE(REPLACE(CAST(address_object->>'line' AS text), '\\', ''), ']', ''), '[', '') AS address, \n" +
			"\t\t\tCASE WHEN address_object->>'stateId'  ~ '^\\d+(\\.\\d+)?$' THEN address_object->>'stateId' ELSE null END  AS stateId,\n" +
			"\t\t\tCASE WHEN address_object->>'district'  ~ '^\\d+(\\.\\d+)?$' THEN address_object->>'district' ELSE null END  AS lgaId\n" +
			"\t\t\t      FROM patient_person p,\n" +
			"\t\t\tjsonb_array_elements(p.address-> 'address') with ordinality l(address_object)) as result\n" +
			"\t\t\t  ) r ON r.id=p.id\n" +
			"\t\t\t INNER JOIN base_organisation_unit facility ON facility.id=facility_id\n" +
			"\t\t\t  INNER JOIN base_organisation_unit facility_lga ON facility_lga.id=facility.parent_organisation_unit_id\n" +
			"\t\t\t  INNER JOIN base_organisation_unit facility_state ON facility_state.id=facility_lga.parent_organisation_unit_id\n" +
			"\t\t\t  LEFT JOIN base_organisation_unit res_state ON res_state.id=CAST(r.stateid AS BIGINT)\n" +
			"\t\t\t  LEFT JOIN base_organisation_unit res_lga ON res_lga.id=CAST(r.lgaid AS BIGINT)\n" +
			"\t\t\t INNER JOIN base_organisation_unit_identifier boui ON boui.organisation_unit_id=facility_id\n" +
			"\t\t\t INNER JOIN hiv_enrollment h ON h.person_uuid = p.uuid\n" +
			"\t\t\tWHERE h.archived=0 AND h.facility_id=?1),\n" +
			"\t\t\t\n" +
			"\t\t\tenrollment_details AS (\n" +
			"\t\t\tSELECT h.person_uuid,h.unique_id as uniqueId, tGroup.display as targetGroup,  sar.display as statusAtRegistration, date_confirmed_hiv as dateOfConfirmedHiv,\n" +
			"\t\t\tep.display as entryPoint, date_of_registration as dateOfEnrollment\n" +
			"\t\t\tFROM hiv_enrollment h\n" +
			"\t\t\tLEFT JOIN base_application_codeset sar ON sar.id=h.status_at_registration_id\n" +
			"\t\t\tLEFT JOIN base_application_codeset tGroup ON tGroup.id=h.target_group_id\n" +
			"\t\t\tLEFT JOIN base_application_codeset ep ON ep.id=h.entry_point_id\n" +
			"\t\t\tWHERE h.archived=0 AND h.facility_id=?1) \n" +
			"\n" +
			"            Select e.*, b.*  FROM enrollment_details e\n" +
			"\t\t\tINNER JOIN bio_data b ON e.person_uuid=b.personUuid\n" +
			"\t\t    WHERE b.facilityId = ?1 \n" +
			"\t\t\t AND (b.residentialstate = ?2 OR b.residentialstate IS NULL ) AND ( b.gender =?3 OR  b.gender IS NULL)", nativeQuery = true)
	List<PatientListDTO> getPatientListDTOsByFacilityAndStateAndGender(Long facilityId, String stateOfResidence, String gender);
	
	
	@Query(value = "WITH bio_data AS (SELECT p.facility_id as facilityId, p.id, p.uuid as personUuid, CAST(p.archived as BOOLEAN) as archived, p.uuid,p.hospital_number as hospitalNumber, \n" +
			"\t\t\t  p.surname, p.first_name as firstName,\n" +
			"\t\t\t  EXTRACT(YEAR from AGE(NOW(),  date_of_birth)) as age,\n" +
			"\t\t\t  p.other_name as otherName, p.sex as gender, p.date_of_birth as dateOfBirth, \n" +
			"\t\t\t  p.date_of_registration as dateOfRegistration, p.marital_status->>'display' as maritalStatus, \n" +
			"\t\t\t  education->>'display' as education, p.employment_status->>'display' as occupation, \n" +
			"\t\t\t  facility.name as facilityName, facility_lga.name as lga, facility_state.name as state, \n" +
			"\t\t\t  boui.code as datimId, res_state.name as residentialState, res_lga.name as residentialLga,\n" +
			"\t\t\t  r.address as address, p.contact_point->'contactPoint'->0->'value'->>0 AS phone\n" +
			"\t\t\t  FROM patient_person p\n" +
			"\t\t\t  INNER JOIN (\n" +
			"\t\t\t  SELECT * FROM (SELECT p.id, REPLACE(REPLACE(REPLACE(CAST(address_object->>'line' AS text), '\\', ''), ']', ''), '[', '') AS address, \n" +
			"\t\t\tCASE WHEN address_object->>'stateId'  ~ '^\\d+(\\.\\d+)?$' THEN address_object->>'stateId' ELSE null END  AS stateId,\n" +
			"\t\t\tCASE WHEN address_object->>'district'  ~ '^\\d+(\\.\\d+)?$' THEN address_object->>'district' ELSE null END  AS lgaId\n" +
			"\t\t\t      FROM patient_person p,\n" +
			"\t\t\tjsonb_array_elements(p.address-> 'address') with ordinality l(address_object)) as result\n" +
			"\t\t\t  ) r ON r.id=p.id\n" +
			"\t\t\t INNER JOIN base_organisation_unit facility ON facility.id=facility_id\n" +
			"\t\t\t  INNER JOIN base_organisation_unit facility_lga ON facility_lga.id=facility.parent_organisation_unit_id\n" +
			"\t\t\t  INNER JOIN base_organisation_unit facility_state ON facility_state.id=facility_lga.parent_organisation_unit_id\n" +
			"\t\t\t  LEFT JOIN base_organisation_unit res_state ON res_state.id=CAST(r.stateid AS BIGINT)\n" +
			"\t\t\t  LEFT JOIN base_organisation_unit res_lga ON res_lga.id=CAST(r.lgaid AS BIGINT)\n" +
			"\t\t\t INNER JOIN base_organisation_unit_identifier boui ON boui.organisation_unit_id=facility_id\n" +
			"\t\t\t INNER JOIN hiv_enrollment h ON h.person_uuid = p.uuid\n" +
			"\t\t\tWHERE h.archived=0 AND h.facility_id=?1),\n" +
			"\t\t\t\n" +
			"\t\t\tenrollment_details AS (\n" +
			"\t\t\tSELECT h.person_uuid,h.unique_id as uniqueId, tGroup.display as targetGroup,  sar.display as statusAtRegistration, date_confirmed_hiv as dateOfConfirmedHiv,\n" +
			"\t\t\tep.display as entryPoint, date_of_registration as dateOfEnrollment\n" +
			"\t\t\tFROM hiv_enrollment h\n" +
			"\t\t\tLEFT JOIN base_application_codeset sar ON sar.id=h.status_at_registration_id\n" +
			"\t\t\tLEFT JOIN base_application_codeset tGroup ON tGroup.id=h.target_group_id\n" +
			"\t\t\tLEFT JOIN base_application_codeset ep ON ep.id=h.entry_point_id\n" +
			"\t\t\tWHERE h.archived=0 AND h.facility_id=?1) \n" +
			"\n" +
			"            Select e.*, b.*  FROM enrollment_details e\n" +
			"\t\t\tINNER JOIN bio_data b ON e.person_uuid=b.personUuid\n" +
			"\t\t    WHERE b.facilityId = ?1 \n" +
			"\t\t\t AND (b.residentialstate = ?2 OR b.residentialstate IS NULL )  AND (e.targetgroup = ?3 OR  e.targetgroup IS NULL )", nativeQuery = true)
	List<PatientListDTO> getPatientListDTOsByFacilityAndStateAndTargetGroup(Long facilityId, String stateOfResidence, String targetGroup);
	
	
	@Query(value = "WITH bio_data AS (SELECT p.facility_id as facilityId, p.id, p.uuid as personUuid, CAST(p.archived as BOOLEAN) as archived, p.uuid,p.hospital_number as hospitalNumber, \n" +
			"\t\t\t  p.surname, p.first_name as firstName,\n" +
			"\t\t\t  EXTRACT(YEAR from AGE(NOW(),  date_of_birth)) as age,\n" +
			"\t\t\t  p.other_name as otherName, p.sex as gender, p.date_of_birth as dateOfBirth, \n" +
			"\t\t\t  p.date_of_registration as dateOfRegistration, p.marital_status->>'display' as maritalStatus, \n" +
			"\t\t\t  education->>'display' as education, p.employment_status->>'display' as occupation, \n" +
			"\t\t\t  facility.name as facilityName, facility_lga.name as lga, facility_state.name as state, \n" +
			"\t\t\t  boui.code as datimId, res_state.name as residentialState, res_lga.name as residentialLga,\n" +
			"\t\t\t  r.address as address, p.contact_point->'contactPoint'->0->'value'->>0 AS phone\n" +
			"\t\t\t  FROM patient_person p\n" +
			"\t\t\t  INNER JOIN (\n" +
			"\t\t\t  SELECT * FROM (SELECT p.id, REPLACE(REPLACE(REPLACE(CAST(address_object->>'line' AS text), '\\', ''), ']', ''), '[', '') AS address, \n" +
			"\t\t\tCASE WHEN address_object->>'stateId'  ~ '^\\d+(\\.\\d+)?$' THEN address_object->>'stateId' ELSE null END  AS stateId,\n" +
			"\t\t\tCASE WHEN address_object->>'district'  ~ '^\\d+(\\.\\d+)?$' THEN address_object->>'district' ELSE null END  AS lgaId\n" +
			"\t\t\t      FROM patient_person p,\n" +
			"\t\t\tjsonb_array_elements(p.address-> 'address') with ordinality l(address_object)) as result\n" +
			"\t\t\t  ) r ON r.id=p.id\n" +
			"\t\t\t INNER JOIN base_organisation_unit facility ON facility.id=facility_id\n" +
			"\t\t\t  INNER JOIN base_organisation_unit facility_lga ON facility_lga.id=facility.parent_organisation_unit_id\n" +
			"\t\t\t  INNER JOIN base_organisation_unit facility_state ON facility_state.id=facility_lga.parent_organisation_unit_id\n" +
			"\t\t\t  LEFT JOIN base_organisation_unit res_state ON res_state.id=CAST(r.stateid AS BIGINT)\n" +
			"\t\t\t  LEFT JOIN base_organisation_unit res_lga ON res_lga.id=CAST(r.lgaid AS BIGINT)\n" +
			"\t\t\t INNER JOIN base_organisation_unit_identifier boui ON boui.organisation_unit_id=facility_id\n" +
			"\t\t\t INNER JOIN hiv_enrollment h ON h.person_uuid = p.uuid\n" +
			"\t\t\tWHERE h.archived=0 AND h.facility_id=?1),\n" +
			"\t\t\t\n" +
			"\t\t\tenrollment_details AS (\n" +
			"\t\t\tSELECT h.person_uuid,h.unique_id as uniqueId, tGroup.display as targetGroup,  sar.display as statusAtRegistration, date_confirmed_hiv as dateOfConfirmedHiv,\n" +
			"\t\t\tep.display as entryPoint, date_of_registration as dateOfEnrollment\n" +
			"\t\t\tFROM hiv_enrollment h\n" +
			"\t\t\tLEFT JOIN base_application_codeset sar ON sar.id=h.status_at_registration_id\n" +
			"\t\t\tLEFT JOIN base_application_codeset tGroup ON tGroup.id=h.target_group_id\n" +
			"\t\t\tLEFT JOIN base_application_codeset ep ON ep.id=h.entry_point_id\n" +
			"\t\t\tWHERE h.archived=0 AND h.facility_id=?1) \n" +
			"\n" +
			"            Select e.*, b.*  FROM enrollment_details e\n" +
			"\t\t\tINNER JOIN bio_data b ON e.person_uuid=b.personUuid\n" +
			"\t\t    WHERE b.facilityId = ?1 \n" +
			"\t\t\t AND (b.residentiallga = ?2 OR b.residentiallga IS NULL )  AND (b.gender = ?3 OR  b.gender IS NULL )", nativeQuery = true)
	List<PatientListDTO> getPatientListDTOsByFacilityAndLgaAndGender(Long facilityId, String lgaOfResidence, String gender);
	
	@Query(value = "WITH bio_data AS (SELECT p.facility_id as facilityId, p.id, p.uuid as personUuid, CAST(p.archived as BOOLEAN) as archived, p.uuid,p.hospital_number as hospitalNumber, \n" +
			"\t\t\t  p.surname, p.first_name as firstName,\n" +
			"\t\t\t  EXTRACT(YEAR from AGE(NOW(),  date_of_birth)) as age,\n" +
			"\t\t\t  p.other_name as otherName, p.sex as gender, p.date_of_birth as dateOfBirth, \n" +
			"\t\t\t  p.date_of_registration as dateOfRegistration, p.marital_status->>'display' as maritalStatus, \n" +
			"\t\t\t  education->>'display' as education, p.employment_status->>'display' as occupation, \n" +
			"\t\t\t  facility.name as facilityName, facility_lga.name as lga, facility_state.name as state, \n" +
			"\t\t\t  boui.code as datimId, res_state.name as residentialState, res_lga.name as residentialLga,\n" +
			"\t\t\t  r.address as address, p.contact_point->'contactPoint'->0->'value'->>0 AS phone\n" +
			"\t\t\t  FROM patient_person p\n" +
			"\t\t\t  INNER JOIN (\n" +
			"\t\t\t  SELECT * FROM (SELECT p.id, REPLACE(REPLACE(REPLACE(CAST(address_object->>'line' AS text), '\\', ''), ']', ''), '[', '') AS address, \n" +
			"\t\t\tCASE WHEN address_object->>'stateId'  ~ '^\\d+(\\.\\d+)?$' THEN address_object->>'stateId' ELSE null END  AS stateId,\n" +
			"\t\t\tCASE WHEN address_object->>'district'  ~ '^\\d+(\\.\\d+)?$' THEN address_object->>'district' ELSE null END  AS lgaId\n" +
			"\t\t\t      FROM patient_person p,\n" +
			"\t\t\tjsonb_array_elements(p.address-> 'address') with ordinality l(address_object)) as result\n" +
			"\t\t\t  ) r ON r.id=p.id\n" +
			"\t\t\t INNER JOIN base_organisation_unit facility ON facility.id=facility_id\n" +
			"\t\t\t  INNER JOIN base_organisation_unit facility_lga ON facility_lga.id=facility.parent_organisation_unit_id\n" +
			"\t\t\t  INNER JOIN base_organisation_unit facility_state ON facility_state.id=facility_lga.parent_organisation_unit_id\n" +
			"\t\t\t  LEFT JOIN base_organisation_unit res_state ON res_state.id=CAST(r.stateid AS BIGINT)\n" +
			"\t\t\t  LEFT JOIN base_organisation_unit res_lga ON res_lga.id=CAST(r.lgaid AS BIGINT)\n" +
			"\t\t\t INNER JOIN base_organisation_unit_identifier boui ON boui.organisation_unit_id=facility_id\n" +
			"\t\t\t INNER JOIN hiv_enrollment h ON h.person_uuid = p.uuid\n" +
			"\t\t\tWHERE h.archived=0 AND h.facility_id=?1),\n" +
			"\t\t\t\n" +
			"\t\t\tenrollment_details AS (\n" +
			"\t\t\tSELECT h.person_uuid,h.unique_id as uniqueId, tGroup.display as targetGroup,  sar.display as statusAtRegistration, date_confirmed_hiv as dateOfConfirmedHiv,\n" +
			"\t\t\tep.display as entryPoint, date_of_registration as dateOfEnrollment\n" +
			"\t\t\tFROM hiv_enrollment h\n" +
			"\t\t\tLEFT JOIN base_application_codeset sar ON sar.id=h.status_at_registration_id\n" +
			"\t\t\tLEFT JOIN base_application_codeset tGroup ON tGroup.id=h.target_group_id\n" +
			"\t\t\tLEFT JOIN base_application_codeset ep ON ep.id=h.entry_point_id\n" +
			"\t\t\tWHERE h.archived=0 AND h.facility_id=?1) \n" +
			"\n" +
			"            Select e.*, b.*  FROM enrollment_details e\n" +
			"\t\t\tINNER JOIN bio_data b ON e.person_uuid=b.personUuid\n" +
			"\t\t    WHERE b.facilityId = ?1 \n" +
			"\t\t\t AND (b.residentiallga = ?2 OR b.residentiallga IS NULL ) AND (e.targetgroup = ?3 OR  e.targetgroup IS NULL )", nativeQuery = true)
	List<PatientListDTO> getPatientListDTOsByFacilityAndLgaAndTargetGroup(Long facilityId, String stateOfResidence, String targetGroup);
}
