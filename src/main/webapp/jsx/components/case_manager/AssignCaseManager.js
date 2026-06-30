import React, { useEffect, useCallback, useState } from "react";
import { useLocation, useHistory } from "react-router-dom";
import { makeStyles } from "@material-ui/core/styles";
import Container from "@mui/material/Container";
import SendIcon from "@mui/icons-material/Send";
import Button from "@mui/material/Button";
import axios from "axios";
import { toast } from "react-toastify";
import { token, url } from "../../../api";
import PageTitle from "../layouts/PageTitle";
import Alert from "@mui/material/Alert";

import { Link } from "react-router-dom";
import MatButton from "@material-ui/core/Button";
import ReplyIcon from "@mui/icons-material/Reply";

import {
  Row,
  Col,
  Card,
  CardBody,
  Form,
  FormGroup,
  Input,
  Label,
  Table,
} from "reactstrap";

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

const AssignCaseManager = (props) => {
  const location = useLocation();
  // const { patients } = location.state;
  const history = useHistory();
  const result = JSON.parse(localStorage.getItem("patients"));
  // console.log(result);
  const classes = useStyles();
  const [states, setStates] = useState([]);
  const [provinces, setProvinces] = useState([]);
  const [caseManager, setCaseManager] = useState([]);
  const [patientAssigned, setPatientAssigned] = useState([]);
  const [errors, setErrors] = useState({});
  const [assignedData, setAssignedData] = useState({
    assignDate: "",
    caseManager: "",
    state: "",
    lga: "",
    patients: result,
  });

  const handleInputChange = (e) => {
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
      setAssignedData({ ...assignedData, state: e.target.value });
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

  const getCaseManager = async () => {
    await axios
      .get(`${url}casemanager/list`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((resp) => setCaseManager(resp.data))
      .catch((err) => console.log(err));
  };

  useEffect(() => {
    const result = JSON.parse(localStorage.getItem("patients"));

    setPatientAssigned(result);

    getStates();
    getCaseManager();
  }, []);

  const validateInputs = () => {
    let temp = { ...errors };
    temp.assignDate = assignedData.assignDate ? "" : "Assign date is required.";
    temp.caseManager = assignedData.caseManager
      ? ""
      : "Case manager is required.";
    // temp.state = assignedData.state ? "" : "State is required.";
    // temp.lga = assignedData.lga ? "" : "LGA is required.";
    setErrors({
      ...temp,
    });
    return Object.values(temp).every((x) => x === "");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (validateInputs()) {
      const filterData = JSON.parse(localStorage.getItem("filterData"));

      assignedData.state = filterData.state;
      assignedData.lga = filterData.lga;
      console.log(assignedData);

      await axios
        .post(`${url}assign/create`, assignedData, {
          headers: { Authorization: `Bearer ${token}` },
        })
        .then((resp) => {
          console.log(resp);
          toast.success("Case manager assigned to patient successfully");
        })
        .catch((err) => {
          console.log(err);
          toast.error(
            "Something went wrong. Please try again... " + err.message
          );
        });

      history.push("/");
    }
  };

  return (
    <>
      <Container maxWidth>
        <Card>
          <CardBody>
            <PageTitle activeMenu="Assign " motherMenu="Case Manager" />
            {result === null ? (
              <>
                <Alert
                  severity="error"
                  style={{
                    width: "100%",
                    fontSize: "18px",
                    textAlign: "center",
                  }}
                >
                  <b>No patient</b> selected to be assigned to a case manager...
                </Alert>
                <br />
              </>
            ) : (
              ""
            )}
            <p style={{ textAlign: "right" }}>
              <Link color="inherit" to={{ pathname: "/" }}>
                <MatButton
                  variant="contained"
                  color="primary"
                  startIcon={<ReplyIcon />}
                  style={{ backgroundColor: "rgb(153, 46, 98)", color: "#fff" }}
                >
                  back
                </MatButton>
              </Link>
            </p>
            <hr />
            <br />
            <Form>
              <Row>
                <Col>
                  {" "}
                  <FormGroup>
                    <Label for="assignDate" className={classes.label}>
                      Date & Time <span style={{ color: "red" }}> *</span>
                    </Label>
                    <Input
                      type="datetime-local"
                      max={new Date().toISOString().substr(0, 16)}
                      name="assignDate"
                      id="assignDate"
                      placeholder="Date & Time Created"
                      className={classes.input}
                      value={assignedData.assignDate}
                      onChange={handleInputChange}
                    />
                    {errors.assignDate !== "" ? (
                      <span className={classes.error}>{errors.assignDate}</span>
                    ) : (
                      ""
                    )}
                  </FormGroup>
                </Col>
                <Col>
                  <FormGroup>
                    <Label for="caseManager" className={classes.label}>
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
                      name="caseManager"
                      value={assignedData.caseManager}
                      id="caseManager"
                      onChange={handleInputChange}
                    >
                      <option>Select Case Manager</option>
                      {caseManager &&
                        caseManager.map((value, i) => (
                          <option
                            key={i}
                            value={`${value.firstName} ${value.lastName}`}
                          >
                            {`${value.firstName} ${value.lastName}`}
                          </option>
                        ))}
                    </select>
                    {errors.caseManager !== "" ? (
                      <span className={classes.error}>
                        {errors.caseManager}
                      </span>
                    ) : (
                      ""
                    )}
                  </FormGroup>
                </Col>
              </Row>
     
              {result !== null ? (
                <Button
                  variant="contained"
                  color="primary"
                  type="submit"
                  startIcon={<SendIcon />}
                  onClick={handleSubmit}
                  style={{
                    backgroundColor: "#014d88",
                    fontWeight: "bolder",
                    color: "fff",
                  }}
                >
                  Submit
                </Button>
              ) : (
                ""
              )}
            </Form>
            <br />
            <br />
            <Row>
              <Table striped bordered size="sm">
                <tbody>
                  <tr style={{ backgroundColor: "#014d88", color: "#fff" }}>
                    <th>Hospital No</th>
                    <th>Full Name</th>
                    <th>Sex</th>
                    <th>Age</th>
                    {/* <th>Current Status</th> */}
                  </tr>
                  {patientAssigned &&
                    patientAssigned.map((item, value) => (
                      <tr key={value + 1}>
                        <td>{item.hospitalNo}</td>
                        <td>{item.fullName}</td>
                        <td>{item.sex}</td>
                        <td>{item.age}</td>
                        {/* <td>{item.currentStatus}</td> */}
                      </tr>
                    ))}
                </tbody>
              </Table>
            </Row>
          </CardBody>
        </Card>
      </Container>
    </>
  );
};

export default AssignCaseManager;
