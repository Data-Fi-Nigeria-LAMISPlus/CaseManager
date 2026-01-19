import React, { useState, useEffect } from "react";
import {
  Modal,
  ModalHeader,
  ModalBody,
  Form,
  FormFeedback,
  Row,
  Col,
  Card,
  CardBody,
  FormGroup,
  Label,
  Input,
} from "reactstrap";
import PhoneInput from "react-phone-input-2";
import "react-phone-input-2/lib/style.css";
import MatButton from "@material-ui/core/Button";
import { makeStyles } from "@material-ui/core/styles";
import UpdateIcon from "@mui/icons-material/Update";
import CancelIcon from "@material-ui/icons/Cancel";
import { Alert } from "reactstrap";
import { Spinner } from "reactstrap";
import axios from "axios";
import { token, url as baseUrl } from "../../../api";
import { toast } from "react-toastify";

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
    display: "none",
  },
}));

const EditCaseManager = (props) => {
  const [facilities, setFacilities] = useState([]);
  const classes = useStyles();
  const [user, setUser] = useState("");

  //console.log(props.casemanager);

  const [data, setData] = useState({
    designation: props.casemanager?.designation,
    firstName: props.casemanager?.firstName,
    lastName: props.casemanager?.lastName,
    sex: props.casemanager?.sex,
    phoneNumber: props.casemanager?.phoneNumber,
    facilityId: props.casemanager?.facilityId,
    religion: props.casemanager?.religion,
    address: props.casemanager?.address,
    created_by: props.casemanager?.createdBy,
    modified_by: "",
    active: props.casemanager?.active,
    username: props.casemanager?.username,
    password: "**********",
  });

  const Facilities = () => {
    axios
      .get(`${baseUrl}account`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((response) => {
        //console.log(response.data);
        setUser(`${response.data.firstName} ${response.data.lastName}`);
        setFacilities(response.data.applicationUserOrganisationUnits);
      })
      .catch((error) => {
        //console.log(error);
      });
  };

  useEffect(() => {
    Facilities();
  }, []);

  const [contactPhone, setContactPhone] = useState(data.phoneNumber);

  useEffect(() => {
    setData({ ...props.casemanager, password: "********" });
  }, [props.casemanager]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setData({
      ...data,
      [name]: value,
    });
  };

  const checkPhoneNumber = (e) => {
    setContactPhone(e);
  };

  const updateContactManager = (id, data) => {
    axios
      .put(`${baseUrl}casemanager/update/${id}`, data, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((resp) => {
        console.log(resp);
        toast.success("Case manager updated successfully");
      })
      .catch((err) => {
        console.log(err);
        toast.error("Something went wrong. Please try again... " + err.message);
      });
  };

  const editCaseManager = async (e) => {
    e.preventDefault();

    //console.log("Edit data", contactPhone);
    data.phoneNumber = contactPhone ?? data.phoneNumber;
    data.modified_by = user;

    const userPayload = {
      firstName: data.firstName,
      lastName: data.lastName,
      username: data.username,
      email: data.email,
      phone: data.phoneNumber,
      role: "",
      designation: data.designation,
      gender: data.sex,
      dateOfBirth: data.dateOfBirth,
      password: data.password,
      adminRegistration: true,
      details: {},
      userName: data.username,
      phoneNumber: data.phoneNumber,
      roles: ["User"],
      facilityIds: [data.facilityId],
    };

    const caseManagerDetails = {
      designation: data.designation,
      firstName: data.firstName,
      lastName: data.lastName,
      sex: data.sex,
      phoneNumber: data.phoneNumber,
      facilityId: data.facilityId,
      religion: data.religion,
      address: data.address,
      created_by: data.created_by,
      modified_by: data.modified_by,
      active: data.active,
      username: data.username,
      password: "********",
      user_id: "",
    };

    // console.log(userPayload);
    // console.log(caseManagerDetails);

    const userID = parseInt(props.casemanager.user_id);
    console.log(props.casemanager.user_id);
    if (props.casemanager.user_id == null || props.casemanager.user_id == "") {
      await axios
        .post(`${baseUrl}users`, userPayload, {
          headers: { Authorization: `Bearer ${token}` },
        })
        .then((resp) => {
          console.log("create", resp.data);
          caseManagerDetails.user_id = resp.data;

          updateContactManager(props.casemanager.id, caseManagerDetails);
        })
        .catch((err) => {
          console.log(err);
          toast.error(
            "Something went wrong. Please try again... " + err.message
          );
        });
    } else {
      await axios
        .put(`${baseUrl}users/${userID}`, userPayload, {
          headers: { Authorization: `Bearer ${token}` },
        })
        .then((resp) => {
          console.log("update", resp);
          caseManagerDetails.user_id = resp.data;
          updateContactManager(props.casemanager.id, caseManagerDetails);
        })
        .catch((err) => {
          console.log(err);
          toast.error(
            "Something went wrong. Please try again... " + err.message
          );
        });
    }

    props.getAllCaseManagers();
    props.togglestatus();
  };

  return (
    <div>
      <Modal
        isOpen={props.modalstatus}
        toggle={props.togglestatus}
        className={props.className}
        size="lg"
      >
        <Form>
          <ModalHeader toggle={props.togglestatus}>
            Edit Case Manager
          </ModalHeader>
          <ModalBody>
            <Card>
              <CardBody>
                <Row>
                  <Col md={6}>
                    <FormGroup>
                      <Label for="courierRiderName" className={classes.label}>
                        Designation <span style={{ color: "red" }}> *</span>
                      </Label>
                      <Input
                        type="text"
                        name="designation"
                        id="designation"
                        value={data.designation}
                        onChange={handleInputChange}
                        className="form-control"
                        style={{
                          border: "1px solid #014D88",
                          borderRadius: "0.2rem",
                        }}
                      />
                    </FormGroup>
                  </Col>
                  <Col md={6}>
                    <FormGroup>
                      <Label for="courierRiderName" className={classes.label}>
                        First Name <span style={{ color: "red" }}> *</span>
                      </Label>
                      <Input
                        type="text"
                        name="firstName"
                        id="firstName"
                        value={data.firstName}
                        onChange={handleInputChange}
                        className="form-control"
                        style={{
                          border: "1px solid #014D88",
                          borderRadius: "0.2rem",
                        }}
                      />
                    </FormGroup>
                  </Col>
                </Row>
                <Row>
                  <Col md={6}>
                    <FormGroup>
                      <Label for="courierRiderName" className={classes.label}>
                        Last Name <span style={{ color: "red" }}> *</span>
                      </Label>
                      <Input
                        type="text"
                        name="lastName"
                        id="lastName"
                        value={data.lastName}
                        onChange={handleInputChange}
                        className="form-control"
                        style={{
                          border: "1px solid #014D88",
                          borderRadius: "0.2rem",
                        }}
                      />
                    </FormGroup>
                  </Col>
                  <Col md={6}>
                    <FormGroup>
                      <Label for="sex" className={classes.label}>
                        Gender <span style={{ color: "red" }}> *</span>
                      </Label>
                      <select
                        className="form-control"
                        style={{
                          border: "1px solid #014d88",
                          borderRadius: "0px",
                          fontSize: "14px",
                          color: "#000",
                        }}
                        name="sex"
                        value={data.sex}
                        id="sex"
                        onChange={handleInputChange}
                      >
                        <option>Select Gender</option>
                        <option>Male</option>
                        <option>Female</option>
                      </select>
                    </FormGroup>
                  </Col>
                </Row>
                <Row>
                  <Col md={6}>
                    <FormGroup>
                      <Label for="phoneNumber" className={classes.label}>
                        Phone Number <span style={{ color: "red" }}> *</span>
                      </Label>
                      <PhoneInput
                        containerStyle={{
                          width: "100%",
                          border: "1px solid #014d88",
                        }}
                        inputStyle={{
                          width: "100%",
                          borderRadius: "0px",
                          height: 44,
                        }}
                        country={"ng"}
                        masks={{ ng: "...-...-....", at: "(....) ...-...." }}
                        placeholder="(234)7099999999"
                        value={data.phoneNumber}
                        onChange={(e) => checkPhoneNumber(e)}
                      />
                    </FormGroup>
                  </Col>
                  <Col md={6}>
                    <FormGroup>
                      <Label>
                        Facility <span style={{ color: "red" }}> *</span>
                      </Label>
                      <select
                        className="form-control"
                        name="facilityId"
                        id="facilityId"
                        value={data.facilityId}
                        onChange={handleInputChange}
                        style={{
                          border: "1px solid #014D88",
                          borderRadius: "0.2rem",
                        }}
                      >
                        <option value={""}>Select Facility</option>
                        {facilities.map((value) => (
                          <option
                            key={value.id}
                            value={value.organisationUnitId}
                          >
                            {value.organisationUnitName}
                          </option>
                        ))}
                      </select>
                    </FormGroup>
                  </Col>
                </Row>
                <Row>
                  <Col md={6}>
                    <FormGroup>
                      <Label for="username" className={classes.label}>
                        Username <span style={{ color: "red" }}> *</span>
                      </Label>
                      <Input
                        type="text"
                        name="username"
                        id="username"
                        value={data.username}
                        onChange={handleInputChange}
                        className="form-control"
                        style={{
                          border: "1px solid #014D88",
                          borderRadius: "0.2rem",
                        }}
                      />
                    </FormGroup>
                  </Col>
                  <Col md={6}>
                    <FormGroup>
                      <Label for="password" className={classes.label}>
                        Password <span style={{ color: "red" }}> *</span>
                      </Label>
                      <Input
                        type="text"
                        name="password"
                        id="password"
                        value={data.password}
                        onChange={handleInputChange}
                        className="form-control"
                        style={{
                          border: "1px solid #014D88",
                          borderRadius: "0.2rem",
                        }}
                      />
                    </FormGroup>
                  </Col>
                </Row>
                <Row>
                  <Col md={4}>
                    <FormGroup>
                      <Label for="religion" className={classes.label}>
                        Religion <span style={{ color: "red" }}> *</span>
                      </Label>
                      <select
                        className="form-control"
                        style={{
                          border: "1px solid #014d88",
                          borderRadius: "0px",
                          fontSize: "14px",
                          color: "#000",
                        }}
                        name="religion"
                        value={data.religion}
                        id="sex"
                        onChange={handleInputChange}
                      >
                        <option>Select Religion</option>
                        <option>Christianity</option>
                        <option>Islam</option>
                      </select>
                    </FormGroup>
                  </Col>
                  <Col md={8}>
                    <FormGroup>
                      <Label for="sex" className={classes.label}>
                        Address <span style={{ color: "red" }}> *</span>
                      </Label>
                      <Input
                        type="text"
                        name="address"
                        id="address"
                        value={data.address}
                        onChange={handleInputChange}
                        className="form-control"
                        style={{
                          border: "1px solid #014D88",
                          borderRadius: "0.2rem",
                        }}
                      />
                    </FormGroup>
                  </Col>
                </Row>
                <br />
                <br />

                <MatButton
                  type="submit"
                  variant="contained"
                  color="primary"
                  className={classes.button}
                  startIcon={<UpdateIcon />}
                  onClick={editCaseManager}
                >
                  Submit
                </MatButton>

                <MatButton
                  variant="contained"
                  color="default"
                  onClick={props.togglestatus}
                  className={classes.button}
                  startIcon={<CancelIcon />}
                >
                  Cancel
                </MatButton>
              </CardBody>
            </Card>
          </ModalBody>
        </Form>
      </Modal>
    </div>
  );
};

export default EditCaseManager;
