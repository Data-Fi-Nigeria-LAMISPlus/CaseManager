import React, { useEffect, useCallback, useState, forwardRef } from "react";
import { Link } from "react-router-dom";
import { toast } from "react-toastify";
import Button from "@material-ui/core/Button";
import uniq from "lodash/uniq";
import PersonSearchIcon from "@mui/icons-material/PersonSearch";
import PersonAddIcon from "@mui/icons-material/PersonAdd";
import { makeStyles } from "@material-ui/core/styles";
import { Row, Col, Form, FormGroup, Label } from "reactstrap";

import axios from "axios";
import { token, url } from "../../../api";
import MaterialTable from "material-table";
import AddBox from "@material-ui/icons/AddBox";
import ArrowUpward from "@material-ui/icons/ArrowUpward";
import Check from "@material-ui/icons/Check";
import ChevronLeft from "@material-ui/icons/ChevronLeft";
import ChevronRight from "@material-ui/icons/ChevronRight";
import Clear from "@material-ui/icons/Clear";
import DeleteOutline from "@material-ui/icons/DeleteOutline";
import Edit from "@material-ui/icons/Edit";
import FilterList from "@material-ui/icons/FilterList";
import FirstPage from "@material-ui/icons/FirstPage";
import LastPage from "@material-ui/icons/LastPage";
import Remove from "@material-ui/icons/Remove";
import SaveAlt from "@material-ui/icons/SaveAlt";
import Search from "@material-ui/icons/Search";
import ViewColumn from "@material-ui/icons/ViewColumn";

const tableIcons = {
  Add: forwardRef((props, ref) => <AddBox {...props} ref={ref} />),
  Check: forwardRef((props, ref) => <Check {...props} ref={ref} />),
  Clear: forwardRef((props, ref) => <Clear {...props} ref={ref} />),
  Delete: forwardRef((props, ref) => <DeleteOutline {...props} ref={ref} />),
  DetailPanel: forwardRef((props, ref) => (
    <ChevronRight {...props} ref={ref} />
  )),
  Edit: forwardRef((props, ref) => <Edit {...props} ref={ref} />),
  Export: forwardRef((props, ref) => <SaveAlt {...props} ref={ref} />),
  Filter: forwardRef((props, ref) => <FilterList {...props} ref={ref} />),
  FirstPage: forwardRef((props, ref) => <FirstPage {...props} ref={ref} />),
  LastPage: forwardRef((props, ref) => <LastPage {...props} ref={ref} />),
  NextPage: forwardRef((props, ref) => <ChevronRight {...props} ref={ref} />),
  PreviousPage: forwardRef((props, ref) => (
    <ChevronLeft {...props} ref={ref} />
  )),
  ResetSearch: forwardRef((props, ref) => <Clear {...props} ref={ref} />),
  Search: forwardRef((props, ref) => <Search {...props} ref={ref} />),
  SortArrow: forwardRef((props, ref) => <ArrowUpward {...props} ref={ref} />),
  ThirdStateCheck: forwardRef((props, ref) => <Remove {...props} ref={ref} />),
  ViewColumn: forwardRef((props, ref) => <ViewColumn {...props} ref={ref} />),
};

const useStyles = makeStyles((theme) => ({
  card: {
    margin: theme.spacing(20),
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
  },
  form: {
    width: "100%", // Fix IE 11 issue.
    marginTop: theme.spacing(3),
  },
  submit: {
    margin: theme.spacing(3, 0, 2),
  },
  cardBottom: {
    marginBottom: 20,
  },
  Select: {
    height: 45,
    width: 350,
  },
  button: {
    margin: theme.spacing(1),
  },

  root: {
    "& > *": {
      margin: theme.spacing(1),
    },
  },
  input: {
    border: "1px solid #014d88",
    borderRadius: "0px",
    fontSize: "14px",
    color: "#000",
  },
  error: {
    color: "#f85032",
    fontSize: "11px",
  },
  success: {
    color: "#4BB543 ",
    fontSize: "11px",
  },
  inputGroupText: {
    backgroundColor: "#014d88",
    fontWeight: "bolder",
    color: "#fff",
    borderRadius: "0px",
  },
  label: {
    fontSize: "14px",
    color: "#014d88",
    fontWeight: "600",
  },
}));

const PatientList = (props) => {
  const classes = useStyles();
  const [loading, setLoading] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [states, setStates] = useState([]);
  const [provinces, setProvinces] = useState([]);
  const [facilities, setFacilities] = useState([]);
  const [kP, setKP] = useState([]);
  const [pregnancyStatus, setPregnancyStatus] = useState([]);
  const [patients, setPatients] = useState([]);
  const [filtered, setFiltered] = useState(false);
  const [caseManager, setCaseManager] = useState([]);
  const [errors, setErrors] = useState({});
  const [user, setUser] = useState("");
  const [assignedData, setAssignedData] = useState({
    caseManagerId: "",
    patients: [],
  });
  const [filterData, setFilterData] = useState({
    facilityId: "",
    sex: "",
    state: "",
    lga: "",
    targetgroup: "",
  });

  const KP = () => {
    axios
      .get(`${url}application-codesets/v2/TARGET_GROUP`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((response) => {
        setKP(response.data);
      })
      .catch((error) => {});
  };

  const PregnancyStatus = () => {
    axios
      .get(`${url}application-codesets/v2/PREGNANCY_STATUS`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((response) => {
        setPregnancyStatus(response.data);
      })
      .catch((error) => {
        //console.log(error);
      });
  };

  const getCaseManager = async () => {
    await axios
      .get(`${url}casemanager/list`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((resp) => setCaseManager(resp.data))
      .catch((err) => console.log(err));
  };
  const Facilities = () => {
    axios
      .get(`${url}account`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((response) => {
        setUser(`${response.data.firstName} ${response.data.lastName}`);
        setFacilities(response.data.applicationUserOrganisationUnits);
      })
      .catch((error) => {
        //console.log(error);
      });
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFilterData({
      ...filterData,
      [name]: value,
    });
  };

  const handleInputSaveChange = (e) => {
    const { name, value } = e.target;
    setAssignedData({
      ...assignedData,
      [name]: value,
    });
  };

  function getStateByCountryId(getCountryId) {
    axios
      .get(
        `${url}organisation-units/parent-organisation-units/${getCountryId}`,
        { headers: { Authorization: `Bearer ${token}` } }
      )
      .then((response) => {
        setStates(response.data);
      })
      .catch((error) => {});
  }

  const getProvinces = (e) => {
    let stateValue = e.target.value.split(" ");
    let stateId = stateValue[0];
    let stateName = stateValue[1];

    if (stateName.length > 0) {
      setFilterData({ ...filterData, state: e.target.value });
    }

    axios
      .get(`${url}organisation-units/parent-organisation-units/${stateId}`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((response) => {
        setProvinces(
          response.data.sort((x, y) => {
            return x.id - y.id;
          })
        );
      })
      .catch((error) => {});
  };

  const getStates = () => {
    getStateByCountryId("1");
  };

  const validateInputs = () => {
    let temp = { ...errors };
    // temp.assignDate = assignedData.assignDate ? "" : "Assign date is required.";
    temp.caseManagerId = assignedData.caseManagerId
      ? ""
      : "Case manager is required.";
    // temp.state = assignedData.state ? "" : "State is required.";
    // temp.lga = assignedData.lga ? "" : "LGA is required.";
    setErrors({
      ...temp,
    });
    //console.log(temp);
    if (temp.caseManagerId !== "") {
      setSubmitted(false);
    }
    return Object.values(temp).every((x) => x === "");
  };

  useEffect(() => {
    getStates();
    Facilities();
    KP();
    PregnancyStatus();
    getCaseManager();
    localStorage.removeItem("patient");
    localStorage.removeItem("patients");
    localStorage.removeItem("filterData");
  }, []);

  const getPatient = () => {
    setFiltered(true);
    setLoading(true);
    setSubmitted(false);
    let state = filterData.state?.split(" ")[1];
    //console.log(state);
    localStorage.setItem("filterData", JSON.stringify(filterData));
    axios
      .get(
        `${url}casemanager/patients/${filterData.facilityId}?stateOfResidence=${
          state ?? ""
        }&lgaOfResidence=${filterData.lga}&gender=${
          filterData.sex
        }&targetGroup=${filterData.targetgroup}`,
        { headers: { Authorization: `Bearer ${token}` } }
      )
      .then((response) => {
        //console.log(response.data);
        setLoading(false);
        setPatients(response.data);
      })
      .catch((error) => console.log(error));
  };

  const handlePatientChanges = (patient) => {
    let patientArray = [];

    uniq(patient).map((item) => {
      patientArray.push(item);
    });
    localStorage.setItem("patients", JSON.stringify(patientArray));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitted(true);
    const result = JSON.parse(localStorage.getItem("patients"));

    let updatedRecord = result.map((item) => {
      const updated = {
        ...item,
        createdBy: user,
        modifiedBy: "",
        action: "ASSIGNMENTS",
      };
      return updated;
    });

    assignedData.patients = updatedRecord;

    if (validateInputs()) {
      //console.log(assignedData);

      if (assignedData.patients?.length > 0) {
        await axios
          .post(`${url}assign/create`, assignedData, {
            headers: { Authorization: `Bearer ${token}` },
          })
          .then((resp) => {
            localStorage.removeItem("patients");
            setSubmitted(true);
            //console.log(resp);
            toast.success("Case manager assigned to patient successfully");
            setPatients([]);
          })
          .catch((err) => {
            console.log(err);
            toast.error(
              "Something went wrong. Please try again... " + err.message
            );
            setSubmitted(false);
          });
      } else {
        toast.error(
          "Unassigned Patients are not selected. Please try again..."
        );
      }
    }
  };

  return (
    <div>
      <br />
      <Row>
        <Col>
          <FormGroup>
            <Label className={classes.label}>Facility</Label>
            <select
              className="form-control"
              name="facilityId"
              id="facilityId"
              value={filterData.facilityId}
              onChange={handleInputChange}
              style={{
                border: "1px solid #014D88",
                borderRadius: "0.2rem",
              }}
            >
              <option value={""}>Select Facility</option>
              {facilities.map((value) => (
                <option key={value.id} value={value.organisationUnitId}>
                  {value.organisationUnitName}
                </option>
              ))}
            </select>
          </FormGroup>
        </Col>
        <Col>
          <FormGroup>
            <Label className={classes.label}>State of Residence</Label>
            <select
              className="form-control"
              name="state"
              id="state"
              onChange={getProvinces}
              value={filterData.state}
              style={{
                border: "1px solid #014D88",
                borderRadius: "0.2rem",
              }}
            >
              <option value={""}>Select State</option>
              {states.map((value) => (
                <option key={value.id} value={`${value.id} ${value.name}`}>
                  {value.name}
                </option>
              ))}
            </select>
          </FormGroup>
        </Col>
        <Col>
          <FormGroup>
            <Label className={classes.label}>LGA of Residence</Label>
            <select
              className="form-control"
              name="lga"
              id="lga"
              value={filterData.lga}
              onChange={handleInputChange}
              style={{
                border: "1px solid #014D88",
                borderRadius: "0.2rem",
              }}
            >
              <option value={""}>Select LGA</option>
              {provinces.map((value, index) => (
                <option key={index} value={value.name}>
                  {value.name}
                </option>
              ))}
            </select>
          </FormGroup>
        </Col>
      </Row>
      {patients.length === 0 ? (
        <Button
          variant="contained"
          color="primary"
          className="float-right mr-1"
          startIcon={<PersonSearchIcon />}
          style={{
            float: "right",
            backgroundColor: "rgb(153, 46, 98)",
            fontWeight: "bolder",
            color: "fff",
          }}
          onClick={getPatient}
        >
          <span style={{ textTransform: "capitalize" }}>Search Patients</span>
        </Button>
      ) : (
        <Row>
          <Col></Col>
          <Col></Col>
          <Col></Col>
          <Col>
            <FormGroup>
              <Label for="caseManagerId" className={classes.label}>
                Case Manager <span style={{ color: "red" }}> *</span>
              </Label>
              <select
                className="form-control"
                style={{
                  border: "1px solid #014d88",
                  borderRadius: "0px",
                  fontSize: "14px",
                  color: "#000",
                }}
                name="caseManagerId"
                value={assignedData.caseManagerId}
                id="caseManagerId"
                onChange={handleInputSaveChange}
              >
                <option>Select Case Manager</option>
                {caseManager &&
                  caseManager.map((value, i) => (
                    <option key={i} value={`${value.id}`}>
                      {`${value.firstName} ${value.lastName}`}
                    </option>
                  ))}
              </select>
              {errors.caseManagerId !== "" ? (
                <span className={classes.error}>{errors.caseManagerId}</span>
              ) : (
                ""
              )}
            </FormGroup>
            {!submitted ? (
              <Button
                variant="contained"
                color="primary"
                className="float-right mr-1"
                startIcon={<PersonAddIcon />}
                onClick={handleSubmit}
                disabled={submitted ? true : false}
                style={{
                  float: "right",
                  backgroundColor: "#014d88",
                  fontWeight: "bolder",
                  color: "fff",
                }}
              >
                <span style={{ textTransform: "capitalize" }}>
                  Assign Case Manager{" "}
                </span>
              </Button>
            ) : (
              ""
            )}
          </Col>
        </Row>
      )}

      <br />
      <br />
      <MaterialTable
        icons={tableIcons}
        title="List of unassigned patients"
        columns={[
          { title: "Hospital ID", field: "hospitalNo" },
          { title: "Full Name", field: "fullName" },
          { title: "Sex", field: "sex" },
          { title: "DOB", field: "dob" },
          { title: "Age", field: "age" },
          { title: "State", field: "state" },
          { title: "LGA", field: "lga" },
          { title: "Phone", field: "phone" },
          // { title: "Residential State", field: "residentialState" },
          // { title: "Residential Lga", field: "residentialLga" },
          // { title: "Target Group", field: "targetgroup" },
          { title: "Facility", field: "facilityId", hidden: true },
          { title: "PersonUuid", field: "personUuid", hidden: true },
          { title: "DatimId", field: "datimId", hidden: true },
          // { title: "Actions", field: "actions", filtering: false },
        ]}
        isLoading={loading}
        data={
          patients &&
          patients.map((row) => ({
            hospitalNo: row.hospitalNumber,
            fullName: `${row.firstName} ${
              row.otherName === null ? " " : row.otherName
            } ${row.surname}`,
            sex: row.gender,
            dob: row.dateOfBirth,
            age: row.age,
            state: row.state,
            lga: row.lga,
            phone: row.phone,
            // residentialState: row.residentialState,
            // residentialLga: row.residentialLga,
            // biometricStatus: row.biometricStatus,
            // targetGroup: row.targetGroup,
            facilityId: row.facilityId,
            personUuid: row.personUuid,
            datimId: row.datimId,
          }))
        }
        options={{
          headerStyle: {
            backgroundColor: "#014d88",
            color: "#fff",
            fontSize: "16px",
            padding: "10px",
          },
          searchFieldStyle: {
            width: "200%",
            margingLeft: "250px",
          },
          selection: true,
          filtering: false,
          sorting: false,
          exportButton: false,
          searchFieldAlignment: "left",
          searchAutoFocus: true,
          searchFieldVariant: "filled",
          pageSizeOptions: [10, 20, 50, 100],
          pageSize: 10,
          showFirstLastPageButtons: false,
          debounceInterval: 400,
        }}
        onSelectionChange={(rows) => handlePatientChanges(rows)}
      />
    </div>
  );
};

export default PatientList;
