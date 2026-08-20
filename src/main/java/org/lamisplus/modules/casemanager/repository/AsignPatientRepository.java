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
			"        REPLACE(\n" +
			"            REPLACE(\n" +
			"                REPLACE(\n" +
			"                    address_object->>'line',\n" +
			"                    '\\\\',\n" +
			"                    ''\n" +
			"                ),\n" +
			"                ']',\n" +
			"                ''\n" +
			"            ),\n" +
			"            '[',\n" +
			"            ''\n" +
			"        ) AS address,\n" +
			"        CASE\n" +
			"            WHEN address_object->>'stateId' ~ '^\\d+$'\n" +
			"            THEN CAST((address_object->>'stateId') AS BIGINT)\n" +
			"            ELSE NULL\n" +
			"        END AS state_id,\n" +
			"        CASE\n" +
			"            WHEN address_object->>'district' ~ '^\\d+$'\n" +
			"            THEN CAST((address_object->>'district') AS BIGINT)\n" +
			"            ELSE NULL\n" +
			"        END AS lga_id\n" +
			"    FROM patient_person p\n" +
			"    CROSS JOIN LATERAL jsonb_array_elements(\n" +
			"        COALESCE(\n" +
			"            p.address->'address',\n" +
			"            CAST('[]' AS jsonb)\n" +
			"        )\n" +
			"    ) AS address_object\n" +
			"    ORDER BY p.id\n" +
			"),\n" +
			"facility_identifier AS (\n" +
			"    SELECT DISTINCT ON (organisation_unit_id)\n" +
			"        organisation_unit_id,\n" +
			"        code\n" +
			"    FROM base_organisation_unit_identifier\n" +
			"    ORDER BY organisation_unit_id\n" +
			"),\n" +
			"facility_data AS (\n" +
			"    SELECT\n" +
			"        f.id AS facility_id,\n" +
			"        f.name AS facility_name,\n" +
			"        lga.id AS facility_lga_id,\n" +
			"        lga.name AS facility_lga_name,\n" +
			"        state.id AS facility_state_id,\n" +
			"        state.name AS facility_state_name\n" +
			"    FROM base_organisation_unit f\n" +
			"    INNER JOIN base_organisation_unit lga\n" +
			"        ON lga.id = f.parent_organisation_unit_id\n" +
			"    INNER JOIN base_organisation_unit state\n" +
			"        ON state.id = lga.parent_organisation_unit_id\n" +
			"),\n" +
			"bio_data AS (\n" +
			"    SELECT DISTINCT ON (p.uuid)\n" +
			"        p.facility_id AS facilityId,\n" +
			"        p.id AS patientId,\n" +
			"        p.uuid AS personUuid,\n" +
			"        p.hospital_number AS hospitalNumber,\n" +
			"        p.surname,\n" +
			"        p.first_name AS firstName,\n" +
			"        p.date_of_birth AS dateOfBirth,\n" +
			"        EXTRACT(\n" +
			"            YEAR FROM AGE(NOW(), p.date_of_birth)\n" +
			"        ) AS age,\n" +
			"        p.sex AS gender,\n" +
			"        fd.facility_name AS facilityName,\n" +
			"        fi.code AS datimId,\n" +
			"        ad.address,\n" +
			"        p.contact_point\n" +
			"            -> 'contactPoint'\n" +
			"            -> 0\n" +
			"            -> 'value'\n" +
			"            ->> 0 AS phone,\n" +
			"        residence_lga.name AS lga,\n" +
			"        residence_state.name AS state\n" +
			"    FROM patient_person p\n" +
			"    INNER JOIN facility_data fd\n" +
			"        ON fd.facility_id = p.facility_id\n" +
			"    LEFT JOIN address_data ad\n" +
			"        ON ad.id = p.id\n" +
			"    LEFT JOIN base_organisation_unit residence_lga\n" +
			"        ON residence_lga.id = ad.lga_id\n" +
			"    LEFT JOIN base_organisation_unit residence_state\n" +
			"        ON residence_state.id = ad.state_id\n" +
			"    LEFT JOIN facility_identifier fi\n" +
			"        ON fi.organisation_unit_id = p.facility_id\n" +
			"    INNER JOIN hiv_enrollment_commencement h\n" +
			"        ON h.person_uuid = p.uuid\n" +
			"    WHERE h.archived = 0\n" +
			"      AND ( NULLIF(?1, '') IS NULL OR p.facility_id = CAST(NULLIF(?1, '') AS BIGINT) ) \n" +
			"\t  AND ( NULLIF(?2, '') IS NULL OR ad.state_id = CAST(NULLIF(?2, '') AS BIGINT) ) \n" +
			"\t  AND ( NULLIF(?3, '') IS NULL OR ad.lga_id = CAST(NULLIF(?3, '') AS BIGINT) )\n" +
			"      AND NOT EXISTS (\n" +
			"          SELECT 1\n" +
			"          FROM case_manager_patients cmp\n" +
			"          WHERE cmp.person_uuid = p.uuid\n" +
			"             OR cmp.hospital_no = p.hospital_number\n" +
			"      )\n" +
			"    ORDER BY p.uuid\n" +
			"),\n" +
			"enrollment_details AS (\n" +
			"    SELECT DISTINCT ON (h.person_uuid)\n" +
			"        h.person_uuid,\n" +
			"        h.unique_id AS uniqueId,\n" +
			"        h.date_enrolled_in_hiv_care AS dateOfEnrollment\n" +
			"    FROM hiv_enrollment_commencement h\n" +
			"    INNER JOIN bio_data b\n" +
			"        ON b.personUuid = h.person_uuid\n" +
			"    WHERE h.archived = 0\n" +
			"    ORDER BY\n" +
			"        h.person_uuid,\n" +
			"        h.date_enrolled_in_hiv_care DESC\n" +
			")\n" +
			"SELECT\n" +
			"    e.person_uuid AS enrollmentPersonUuid,\n" +
			"    e.uniqueId,\n" +
			"    e.dateOfEnrollment,\n" +
			"    b.facilityId,\n" +
			"    b.patientId,\n" +
			"    b.personUuid,\n" +
			"    b.hospitalNumber,\n" +
			"    b.surname,\n" +
			"    b.firstName,\n" +
			"    b.dateOfBirth,\n" +
			"    b.age,\n" +
			"    b.gender,\n" +
			"    b.facilityName,\n" +
			"    b.datimId,\n" +
			"    b.address,\n" +
			"    b.phone,\n" +
			"    b.lga,\n" +
			"    b.state\n" +
			"FROM bio_data b\n" +
			"INNER JOIN enrollment_details e\n" +
			"    ON e.person_uuid = b.personUuid\n" +
			"ORDER BY b.personUuid", nativeQuery = true)
	List<PatientListDTO> getPatientListDTOsByFacility(String facilityId, String stateOfResidence, String lgaOfResidence);
	
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
			"\t\t  p.surname, p.first_name as firstName,\n" +
			"\t\t  EXTRACT(YEAR from AGE(NOW(),  date_of_birth)) as age,\n" +
			"\t\t  p.other_name as otherName, p.sex as gender, p.date_of_birth as dateOfBirth, \n" +
			"\t\t  p.date_of_registration as dateOfRegistration, p.marital_status->>'display' as maritalStatus, \n" +
			"\t\t  education->>'display' as education, p.employment_status->>'display' as occupation, \n" +
			"\t\t  facility.name as facilityName, facility_lga.name as lga, facility_state.name as state, \n" +
			"\t\t  boui.code as datimId, \n" +
			"\t\t  residential_state.name as residentialState, \n" +
			"\t\t  residential_lga.name as residentialLga,\n" +
			"\t\t  addr.residentialAddress as address, p.contact_point->'contactPoint'->0->'value'->>0 AS phone\n" +
			"\t\t  FROM patient_person p\n" +
			"\t\t  CROSS JOIN LATERAL (\n" +
			"\t\t\tSELECT\n" +
			"\t\t\t\tarray_to_string(\n" +
			"\t\t\t\t\tARRAY(\n" +
			"\t\t\t\t\t\tSELECT jsonb_array_elements_text(address_object->'line')\n" +
			"\t\t\t\t\t),\n" +
			"\t\t\t\t\t', '\n" +
			"\t\t\t\t) AS residentialAddress,\n" +
			"\n" +
			"\t\t\t\tCASE\n" +
			"\t\t\t\t\tWHEN address_object->>'stateId' ~ '^\\d+$'\n" +
			"\t\t\t\t\tTHEN CAST((address_object->>'stateId') AS BIGINT)\n" +
			"\t\t\t\tEND AS residentialStateId,\n" +
			"\n" +
			"\t\t\t\tCASE\n" +
			"\t\t\t\t\tWHEN address_object->>'district' ~ '^\\d+$'\n" +
			"\t\t\t\t\tTHEN CAST((address_object->>'district') AS BIGINT)\n" +
			"\t\t\t\tEND AS residentialLgaId\n" +
			"\n" +
			"\t\t\tFROM jsonb_array_elements(p.address->'address') AS a(address_object)\n" +
			"\n" +
			"\t\t\tLIMIT 1\n" +
			"\n" +
			"\t\t) addr\n" +
			"\t\tINNER JOIN base_organisation_unit facility\n" +
			"        ON facility.id = p.facility_id\n" +
			"\n" +
			"\t\tINNER JOIN base_organisation_unit facility_lga\n" +
			"\t\t\tON facility_lga.id = facility.parent_organisation_unit_id\n" +
			"\n" +
			"\t\tINNER JOIN base_organisation_unit facility_state\n" +
			"\t\t\tON facility_state.id = facility_lga.parent_organisation_unit_id\n" +
			"\n" +
			"\t\tLEFT JOIN base_organisation_unit residential_state\n" +
			"\t\t\tON residential_state.id = addr.residentialStateId\n" +
			"\n" +
			"\t\tLEFT JOIN base_organisation_unit residential_lga\n" +
			"\t\t\tON residential_lga.id = addr.residentialLgaId\n" +
			"\t\t INNER JOIN base_organisation_unit_identifier boui ON boui.organisation_unit_id=facility_id\n" +
			"\t\t INNER JOIN hiv_enrollment h ON h.person_uuid = p.uuid\n" +
			"\t\tWHERE h.archived=0 AND h.facility_id=?1),\n" +
			"\t\t\n" +
			"\t\tenrollment_details AS (\n" +
			"\t\tSELECT h.person_uuid,h.unique_id as uniqueId, sar.display as statusAtRegistration, date_confirmed_hiv_test as dateOfConfirmedHiv,\n" +
			"\t\tep.display as entryPoint, date_enrolled_in_hiv_care as dateOfEnrollment\n" +
			"\t\tFROM hiv_enrollment_commencement h\n" +
			"\t\tLEFT JOIN base_application_codeset sar ON sar.id=h.status_at_registration_id\n" +
			"\t\tLEFT JOIN base_application_codeset ep ON ep.code=h.care_entry_point_id\n" +
			"\t\tWHERE h.archived=0 AND h.facility_id=?1) \n" +
			"\t\t\t\n" +
			"\t\tSelect e.*, b.*  FROM enrollment_details e\n" +
			"\t\tINNER JOIN bio_data b ON e.person_uuid=b.personUuid\n" +
			"\t\t\t    WHERE b.facilityId = ?1 \n" +
			"\t\t\t\tAND b.residentialstate = ?2 ", nativeQuery = true)
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
			"\t\t  p.surname, p.first_name as firstName,\n" +
			"\t\t  EXTRACT(YEAR from AGE(NOW(),  date_of_birth)) as age,\n" +
			"\t\t  p.other_name as otherName, p.sex as gender, p.date_of_birth as dateOfBirth, \n" +
			"\t\t  p.date_of_registration as dateOfRegistration, p.marital_status->>'display' as maritalStatus, \n" +
			"\t\t  education->>'display' as education, p.employment_status->>'display' as occupation, \n" +
			"\t\t  facility.name as facilityName, facility_lga.name as lga, facility_state.name as state, \n" +
			"\t\t  boui.code as datimId, \n" +
			"\t\t  residential_state.name as residentialState, \n" +
			"\t\t  residential_lga.name as residentialLga,\n" +
			"\t\t  addr.residentialAddress as address, p.contact_point->'contactPoint'->0->'value'->>0 AS phone\n" +
			"\t\t  FROM patient_person p\n" +
			"\t\t  CROSS JOIN LATERAL (\n" +
			"\t\t\tSELECT\n" +
			"\t\t\t\tarray_to_string(\n" +
			"\t\t\t\t\tARRAY(\n" +
			"\t\t\t\t\t\tSELECT jsonb_array_elements_text(address_object->'line')\n" +
			"\t\t\t\t\t),\n" +
			"\t\t\t\t\t', '\n" +
			"\t\t\t\t) AS residentialAddress,\n" +
			"\n" +
			"\t\t\t\tCASE\n" +
			"\t\t\t\t\tWHEN address_object->>'stateId' ~ '^\\d+$'\n" +
			"\t\t\t\t\tTHEN CAST((address_object->>'stateId') AS BIGINT)\n" +
			"\t\t\t\tEND AS residentialStateId,\n" +
			"\n" +
			"\t\t\t\tCASE\n" +
			"\t\t\t\t\tWHEN address_object->>'district' ~ '^\\d+$'\n" +
			"\t\t\t\t\tTHEN CAST((address_object->>'district') AS BIGINT)\n" +
			"\t\t\t\tEND AS residentialLgaId\n" +
			"\n" +
			"\t\t\tFROM jsonb_array_elements(p.address->'address') AS a(address_object)\n" +
			"\n" +
			"\t\t\tLIMIT 1\n" +
			"\n" +
			"\t\t) addr\n" +
			"\t\tINNER JOIN base_organisation_unit facility\n" +
			"        ON facility.id = p.facility_id\n" +
			"\n" +
			"\t\tINNER JOIN base_organisation_unit facility_lga\n" +
			"\t\t\tON facility_lga.id = facility.parent_organisation_unit_id\n" +
			"\n" +
			"\t\tINNER JOIN base_organisation_unit facility_state\n" +
			"\t\t\tON facility_state.id = facility_lga.parent_organisation_unit_id\n" +
			"\n" +
			"\t\tLEFT JOIN base_organisation_unit residential_state\n" +
			"\t\t\tON residential_state.id = addr.residentialStateId\n" +
			"\n" +
			"\t\tLEFT JOIN base_organisation_unit residential_lga\n" +
			"\t\t\tON residential_lga.id = addr.residentialLgaId\n" +
			"\t\t INNER JOIN base_organisation_unit_identifier boui ON boui.organisation_unit_id=facility_id\n" +
			"\t\t INNER JOIN hiv_enrollment h ON h.person_uuid = p.uuid\n" +
			"\t\tWHERE h.archived=0 AND h.facility_id=1488),\n" +
			"\t\t\n" +
			"\t\tenrollment_details AS (\n" +
			"\t\tSELECT h.person_uuid,h.unique_id as uniqueId, sar.display as statusAtRegistration, date_confirmed_hiv_test as dateOfConfirmedHiv,\n" +
			"\t\tep.display as entryPoint, date_enrolled_in_hiv_care as dateOfEnrollment\n" +
			"\t\tFROM hiv_enrollment_commencement h\n" +
			"\t\tLEFT JOIN base_application_codeset sar ON sar.id=h.status_at_registration_id\n" +
			"\t\tLEFT JOIN base_application_codeset ep ON ep.code=h.care_entry_point_id\n" +
			"\t\tWHERE h.archived=0 AND h.facility_id=?1) \n" +
			"\t\t\t\n" +
			"\t\tSelect e.*, b.*  FROM enrollment_details e\n" +
			"\t\tINNER JOIN bio_data b ON e.person_uuid=b.personUuid\n" +
			"\t\t\t    WHERE b.facilityId = ?1 \n" +
			"\t\t\t\tAND (b.residentialstate = ?2 OR b.residentialstate IS NULL)\n" +
			"\t\t\t\tAND (b.residentialLga = ?3 OR b.residentialLga IS NULL)\n" +
			"\t\t\t\t", nativeQuery = true)
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
