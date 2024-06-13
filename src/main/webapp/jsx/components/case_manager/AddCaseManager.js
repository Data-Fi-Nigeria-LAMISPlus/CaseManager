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
import MatButton from "@material-ui/core/Button";
import { makeStyles } from "@material-ui/core/styles";
import SaveIcon from "@material-ui/icons/Save";
import CancelIcon from "@material-ui/icons/Cancel";
import { Alert } from "reactstrap";
import { Spinner } from "reactstrap";
import PhoneInput from "react-phone-input-2";
import "react-phone-input-2/lib/style.css";
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
  error: {
    color: "#f85032",
    fontSize: "11px",
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

const AddCaseManager = (props) => {
  const classes = useStyles();
  const [data, setData] = useState({
    designation: "",
    firstName: "",
    lastName: "",
    sex: "",
    phoneNumber: "",
    facilityId: "",
    religion: "",
    address: "",
    created_by: "",
    modified_by: "",
    active: "",
    username: "",
    password: "",
    userId: "",
  });
  const [facilities, setFacilities] = useState([]);
  const [contactPhone, setContactPhone] = useState("");
  const [errors, setErrors] = useState({});
  const [user, setUser] = useState("");

  const Facilities = () => {
    axios
      .get(`${baseUrl}account`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((response) => {
        // console.log(
        //   response.data.applicationUserOrganisationUnits[0].organisationUnitId
        // );
        setFacilities(response.data.applicationUserOrganisationUnits);
        setUser(`${response.data.firstName} ${response.data.lastName}`);
      })
      .catch((error) => {
        //console.log(error);
      });
  };

  useEffect(() => {
    Facilities();
  }, []);

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

  const validateInputs = () => {
    let temp = { ...errors };
    temp.designation = data.designation ? "" : "Designation is required.";
    temp.firstName = data.firstName ? "" : "First name is required.";
    temp.lastName = data.lastName ? "" : "Last name is required.";
    temp.sex = data.sex ? "" : "Sex is required.";
    temp.phoneNumber = data.phoneNumber ? "" : "Phone number is required.";
    temp.facility = data.facilityId ? "" : "Facility is required.";
    temp.religion = data.religion ? "" : "Religion is required.";
    temp.address = data.address ? "" : "Address is required.";
    temp.username = data.username ? "" : "Username is required.";
    temp.password = data.password ? "" : "Password is required.";

    setErrors({
      ...temp,
    });
    return Object.values(temp).every((x) => x === "");
  };

  const addCaseManager = async (e) => {
    e.preventDefault();

    data.phoneNumber = contactPhone;
    data.created_by = user;
    data.active = true;

    if (validateInputs()) {
      const user = {
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
        facilityIds: [facilities[0].organisationUnitId],
      };

      await axios
        .post(`${baseUrl}users`, user, {
          headers: { Authorization: `Bearer ${token}` },
        })
        .then((response) => {
          console.log("user " + response.data);

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
            user_id: response.data,
          };

          axios
            .post(`${baseUrl}casemanager/create`, caseManagerDetails, {
              headers: { Authorization: `Bearer ${token}` },
            })
            .then((resp) => {
              console.log(resp);
              toast.success("Case manager added successfully");
              console.log(resp.data);

              setData({
                designation: "",
                firstName: "",
                lastName: "",
                sex: "",
                phoneNumber: "",
                facilityId: "",
                religion: "",
                address: "",
                username: "",
                password: "",
              });
              props.getAllCaseManagers();
              props.togglestatus();
            })
            .catch((err) => {
              toast.error(
                "Something went wrong. Please try again... " + err.message
              );
            });
        })
        .catch((error) => {
          toast.error(`An error occurred, while adding user`);
        });
    }
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
            Add Case Manager
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
                      {errors.designation !== "" ? (
                        <span className={classes.error}>
                          {errors.designation}
                        </span>
                      ) : (
                        ""
                      )}
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
                      {errors.firstName !== "" ? (
                        <span className={classes.error}>
                          {errors.firstName}
                        </span>
                      ) : (
                        ""
                      )}
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
                      {errors.lastName !== "" ? (
                        <span className={classes.error}>{errors.lastName}</span>
                      ) : (
                        ""
                      )}
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
                      {errors.sex !== "" ? (
                        <span className={classes.error}>{errors.sex}</span>
                      ) : (
                        ""
                      )}
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
                      {errors.phoneNumber !== "" ? (
                        <span className={classes.error}>
                          {errors.phoneNumber}
                        </span>
                      ) : (
                        ""
                      )}
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
                        s
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
                      {errors.facility !== "" ? (
                        <span className={classes.error}>{errors.facility}</span>
                      ) : (
                        ""
                      )}
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
                      {errors.username !== "" ? (
                        <span className={classes.error}>{errors.username}</span>
                      ) : (
                        ""
                      )}
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
                      {errors.password !== "" ? (
                        <span className={classes.error}>{errors.password}</span>
                      ) : (
                        ""
                      )}
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
                      {errors.sex !== "" ? (
                        <span className={classes.error}>{errors.sex}</span>
                      ) : (
                        ""
                      )}
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
                      {errors.address !== "" ? (
                        <span className={classes.error}>{errors.address}</span>
                      ) : (
                        ""
                      )}
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
                  startIcon={<SaveIcon />}
                  onClick={addCaseManager}
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

export default AddCaseManager;
