import React, { useEffect, useState, useCallback, useMemo } from "react";
import { toast } from "react-toastify";
import Button from "@material-ui/core/Button";
import { makeStyles } from "@material-ui/core/styles";
import { Row, Col, FormGroup, Label, Card, CardBody } from "reactstrap";
import axios from "axios";
import MaterialTable from "material-table";
import {
  AddBox,
  ArrowUpward,
  Check,
  ChevronLeft,
  ChevronRight,
  Clear,
  DeleteOutline,
  Edit,
  FilterList,
  FirstPage,
  LastPage,
  Remove,
  SaveAlt,
  Search,
  ViewColumn,
} from "@material-ui/icons";
import PersonSearchIcon from "@mui/icons-material/PersonSearch";
import PersonAddIcon from "@mui/icons-material/PersonAdd";
import RestartAltIcon from "@mui/icons-material/RestartAlt";
import { token, url } from "../../../api";

const tableIcons = {
  Add: React.forwardRef((props, ref) => <AddBox {...props} ref={ref} />),
  Check: React.forwardRef((props, ref) => <Check {...props} ref={ref} />),
  Clear: React.forwardRef((props, ref) => <Clear {...props} ref={ref} />),
  Delete: React.forwardRef((props, ref) => (
    <DeleteOutline {...props} ref={ref} />
  )),
  DetailPanel: React.forwardRef((props, ref) => (
    <ChevronRight {...props} ref={ref} />
  )),
  Edit: React.forwardRef((props, ref) => <Edit {...props} ref={ref} />),
  Export: React.forwardRef((props, ref) => <SaveAlt {...props} ref={ref} />),
  Filter: React.forwardRef((props, ref) => <FilterList {...props} ref={ref} />),
  FirstPage: React.forwardRef((props, ref) => (
    <FirstPage {...props} ref={ref} />
  )),
  LastPage: React.forwardRef((props, ref) => <LastPage {...props} ref={ref} />),
  NextPage: React.forwardRef((props, ref) => (
    <ChevronRight {...props} ref={ref} />
  )),
  PreviousPage: React.forwardRef((props, ref) => (
    <ChevronLeft {...props} ref={ref} />
  )),
  ResetSearch: React.forwardRef((props, ref) => <Clear {...props} ref={ref} />),
  Search: React.forwardRef((props, ref) => <Search {...props} ref={ref} />),
  SortArrow: React.forwardRef((props, ref) => (
    <ArrowUpward {...props} ref={ref} />
  )),
  ThirdStateCheck: React.forwardRef((props, ref) => (
    <Remove {...props} ref={ref} />
  )),
  ViewColumn: React.forwardRef((props, ref) => (
    <ViewColumn {...props} ref={ref} />
  )),
};

const useStyles = makeStyles((theme) => ({
  pageContainer: {
    padding: theme.spacing(2, 3),
    maxWidth: 1400,
    margin: "0 auto",
  },
  filterCard: {
    marginBottom: theme.spacing(2),
    border: "1px solid #e0e0e0",
    borderRadius: 8,
    boxShadow: "0 2px 4px rgba(0,0,0,0.05)",
  },
  filterRow: {
    display: "flex",
    alignItems: "flex-end",
    gap: theme.spacing(2),
    flexWrap: "wrap",
    "& > div": {
      flex: "1 1 240px",
      minWidth: 200,
    },
  },
  label: {
    fontSize: 13,
    color: "#014d88",
    fontWeight: 600,
    marginBottom: theme.spacing(0.75),
    display: "block",
  },
  select: {
    width: "100%",
    height: 42,
    padding: "8px 12px",
    border: "1.5px solid #014d88",
    borderRadius: 6,
    fontSize: 14,
    color: "#333",
    backgroundColor: "#fff",
    outline: "none",
    transition: "border-color 0.2s, box-shadow 0.2s",
    "&:focus": {
      borderColor: "#014d88",
      boxShadow: "0 0 0 3px rgba(1, 77, 136, 0.1)",
    },
  },
  actionBar: {
    display: "flex",
    justifyContent: "flex-end",
    alignItems: "center",
    marginTop: theme.spacing(2),
    marginBottom: theme.spacing(1),
    minHeight: 48,
  },
  searchButton: {
    backgroundColor: "rgb(153, 46, 98)",
    color: "#fff",
    fontWeight: 600,
    textTransform: "capitalize",
    padding: "8px 24px",
    borderRadius: 6,
    "&:hover": {
      backgroundColor: "rgb(130, 39, 83)",
    },
  },
  assignSection: {
    display: "flex",
    alignItems: "flex-end",
    gap: theme.spacing(2),
    justifyContent: "flex-end",
    flexWrap: "wrap",
  },
  assignField: {
    minWidth: 280,
  },
  assignButton: {
    backgroundColor: "#014d88",
    color: "#fff",
    fontWeight: 600,
    textTransform: "capitalize",
    padding: "8px 24px",
    borderRadius: 6,
    "&:hover": {
      backgroundColor: "#013a6b",
    },
  },
  error: {
    color: "#f85032",
    fontSize: 12,
    marginTop: 4,
    display: "block",
  },
  tableCard: {
    border: "1px solid #e0e0e0",
    borderRadius: 8,
    boxShadow: "0 2px 8px rgba(0,0,0,0.06)",
    overflow: "hidden",
    "& .MuiPaper-root": {
      boxShadow: "none",
    },
  },
  tableHeader: {
    backgroundColor: "#014d88",
    color: "#fff",
    fontSize: 15,
    fontWeight: 600,
    padding: "12px 16px",
  },
  required: {
    color: "#f85032",
    marginLeft: 2,
  },
}));

const INITIAL_FILTER = {
  facilityId: "",
  sex: "",
  state: "",
  lga: "",
  targetgroup: "",
};

const INITIAL_ASSIGN = {
  caseManagerId: "",
};

const PatientList = () => {
  const classes = useStyles();
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [states, setStates] = useState([]);
  const [provinces, setProvinces] = useState([]);
  const [facilities, setFacilities] = useState([]);
  const [kpGroups, setKpGroups] = useState([]);
  const [patients, setPatients] = useState([]);
  const [caseManagers, setCaseManagers] = useState([]);
  const [errors, setErrors] = useState({});
  const [user, setUser] = useState("");
  const [selectedPatients, setSelectedPatients] = useState([]);
  const [filterData, setFilterData] = useState(INITIAL_FILTER);
  const [assignData, setAssignData] = useState(INITIAL_ASSIGN);

  const apiGet = useCallback(async (endpoint) => {
    const response = await axios.get(`${url}${endpoint}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return response.data;
  }, []);

  const apiPost = useCallback(async (endpoint, data) => {
    const response = await axios.post(`${url}${endpoint}`, data, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return response.data;
  }, []);

  const fetchKPGroups = useCallback(async () => {
    try {
      const data = await apiGet("application-codesets/v2/TARGET_GROUP");
      setKpGroups(data);
    } catch {
      toast.error("Failed to load target groups");
    }
  }, [apiGet]);

  const fetchCaseManagers = useCallback(async () => {
    try {
      const data = await apiGet("casemanager/list");
      setCaseManagers(data);
    } catch {
      toast.error("Failed to load case managers");
    }
  }, [apiGet]);

  const fetchFacilities = useCallback(async () => {
    try {
      const data = await apiGet("account");
      setUser(`${data.firstName} ${data.lastName}`);
      setFacilities(data.applicationUserOrganisationUnits || []);
    } catch {
      toast.error("Failed to load facilities");
    }
  }, [apiGet]);

  const fetchStates = useCallback(async () => {
    try {
      const data = await apiGet(
        "organisation-units/parent-organisation-units/1",
      );
      setStates(data);
    } catch {
      toast.error("Failed to load states");
    }
  }, [apiGet]);

  const fetchProvinces = useCallback(
    async (stateId) => {
      if (!stateId) return;
      try {
        const data = await apiGet(
          `organisation-units/parent-organisation-units/${stateId}`,
        );
        setProvinces(data.sort((a, b) => a.id - b.id));
      } catch {
        toast.error("Failed to load LGAs");
      }
    },
    [apiGet],
  );

  useEffect(() => {
    fetchStates();
    fetchFacilities();
    fetchKPGroups();
    fetchCaseManagers();
  }, [fetchStates, fetchFacilities, fetchKPGroups, fetchCaseManagers]);

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilterData((prev) => ({ ...prev, [name]: value }));
  };

  const handleStateChange = (e) => {
    const stateId = e.target.value;
    setFilterData((prev) => ({ ...prev, state: stateId, lga: "" }));
    setProvinces([]);
    if (stateId) fetchProvinces(stateId);
  };

  const handleAssignChange = (e) => {
    const { name, value } = e.target;
    setAssignData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: "" }));
    }
  };

  const validateSearch = () => {
    const temp = {
      facilityId: filterData.facilityId ? "" : "Facility is required.",
    };
    setErrors(temp);
    return Object.values(temp).every((x) => x === "");
  };

  const validateAssign = () => {
    const temp = {
      caseManagerId: assignData.caseManagerId
        ? ""
        : "Case manager is required.",
    };
    setErrors(temp);
    return Object.values(temp).every((x) => x === "");
  };

  const handleSearch = async () => {
    setLoading(true);
    if (!validateSearch()) {
      setLoading(false);
      return;
    }
    try {
      const stateName = filterData.state
        ? states.find((s) => String(s.id) === filterData.state)?.id || ""
        : "";
      const params = new URLSearchParams({
        stateOfResidence: stateName,
        lgaOfResidence: filterData.lga || "",
        gender: filterData.sex || "",
        targetGroup: filterData.targetgroup || "",
      });

      const data = await apiGet(
        `casemanager/patients/${filterData.facilityId}?${params}`,
      );
      setPatients(data);
      setSelectedPatients([]);
    } catch (err) {
      toast.error("Failed to fetch patients");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleTableReset = () => {
    setPatients([]);
    setSelectedPatients([]);
  };

  const handleSelectionChange = useCallback((rows) => {
    setSelectedPatients(rows);
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateAssign()) return;
    if (selectedPatients.length === 0) {
      toast.error("Please select at least one patient to assign.");
      return;
    }

    setSubmitting(true);
    try {
      const payload = {
        caseManagerId: assignData.caseManagerId,
        patients: selectedPatients.map((patient) => ({
          ...patient,
          createdBy: user,
          modifiedBy: "",
          action: "ASSIGNMENTS",
        })),
      };

      await apiPost("assign/create", payload);
      toast.success("Case manager assigned successfully");
      setPatients([]);
      setSelectedPatients([]);
      setAssignData(INITIAL_ASSIGN);
    } catch (err) {
      toast.error(`Assignment failed: ${err.message}`);
      console.error(err);
    } finally {
      setSubmitting(false);
    }
  };

  const tableData = useMemo(() => {
    if (!Array.isArray(patients)) return [];

    return patients.map((row) => ({
      hospitalNo: row?.hospitalNumber || "",
      fullName:
        `${row?.firstName || ""} ${row?.otherName || ""} ${row?.surname || ""}`.trim(),
      sex: row?.gender || "",
      dob: row?.dateOfBirth || "",
      age: row?.age || "",
      state: row?.state || "",
      lga: row?.lga || "",
      phone: row?.phone || "",
      facilityId: row?.facilityId || "",
      personUuid: row?.personUuid || "",
      datimId: row?.datimId || "",
    }));
  }, [patients]);

  const showAssignSection = patients.length > 0;

  return (
    <div className={classes.pageContainer}>
      {/* Filter Section */}
      <Card className={classes.filterCard}>
        <CardBody style={{ padding: 20 }}>
          <div className={classes.filterRow}>
            <div>
              <Label className={classes.label}>
                Facility
                <span className={classes.required}>*</span>
              </Label>
              <select
                className={classes.select}
                name="facilityId"
                value={filterData.facilityId}
                onChange={handleFilterChange}
              >
                <option value="">Select Facility</option>
                {facilities.map((value) => (
                  <option key={value.id} value={value.organisationUnitId}>
                    {value.organisationUnitName}
                  </option>
                ))}
              </select>
              {errors.facilityId && (
                <span className={classes.error}>{errors.facilityId}</span>
              )}
            </div>

            <div>
              <Label className={classes.label}>State of Residence</Label>
              <select
                className={classes.select}
                name="state"
                value={filterData.state}
                onChange={handleStateChange}
              >
                <option value="">Select State</option>
                {states.map((value) => (
                  <option key={value.id} value={String(value.id)}>
                    {value.name}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <Label className={classes.label}>LGA of Residence</Label>
              <select
                className={classes.select}
                name="lga"
                value={filterData.lga}
                onChange={handleFilterChange}
                disabled={!filterData.state}
              >
                <option value="">Select LGA</option>
                {provinces.map((value) => (
                  <option key={value.id} value={value.id}>
                    {value.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Action Bar */}
          <div className={classes.actionBar}>
            {!showAssignSection ? (
              <Button
                variant="contained"
                disableElevation
                startIcon={<PersonSearchIcon />}
                className={classes.searchButton}
                onClick={handleSearch}
                disabled={loading}
              >
                {loading ? "Searching..." : "Search Patients"}
              </Button>
            ) : (
              <div className={classes.assignSection}>
                <div className={classes.assignField}>
                  <Label className={classes.label}>
                    Case Manager
                    <span className={classes.required}>*</span>
                  </Label>
                  <select
                    className={classes.select}
                    name="caseManagerId"
                    value={assignData.caseManagerId}
                    onChange={handleAssignChange}
                  >
                    <option value="">Select Case Manager</option>
                    {caseManagers.map((value) => (
                      <option key={value.id} value={String(value.id)}>
                        {`${value.firstName} ${value.lastName}`}
                      </option>
                    ))}
                  </select>
                  {errors.caseManagerId && (
                    <span className={classes.error}>
                      {errors.caseManagerId}
                    </span>
                  )}
                </div>

                <Button
                  variant="contained"
                  disableElevation
                  startIcon={<PersonAddIcon />}
                  className={classes.assignButton}
                  onClick={handleSubmit}
                  disabled={submitting}
                >
                  {submitting ? "Assigning..." : "Assign Case Manager"}
                </Button>

                <Button
                  variant="contained"
                  disableElevation
                  startIcon={<RestartAltIcon />}
                  className={classes.required}
                  onClick={handleTableReset}
                >
                  Reset Table
                </Button>
              </div>
            )}
          </div>
        </CardBody>
      </Card>

      {/* Table Section */}
      <Card className={classes.tableCard}>
        <MaterialTable
          icons={tableIcons}
          title="List of Unassigned Patients"
          columns={[
            { title: "Hospital ID", field: "hospitalNo" },
            { title: "Full Name", field: "fullName" },
            { title: "Sex", field: "sex" },
            { title: "DOB", field: "dob" },
            { title: "Age", field: "age" },
            { title: "State", field: "state" },
            { title: "LGA", field: "lga" },
            { title: "Phone", field: "phone" },
            { title: "Facility", field: "facilityId", hidden: true },
            { title: "PersonUuid", field: "personUuid", hidden: true },
            { title: "DatimId", field: "datimId", hidden: true },
          ]}
          isLoading={loading}
          data={tableData}
          options={{
            headerStyle: {
              backgroundColor: "#014d88",
              color: "#fff",
              fontSize: 14,
              fontWeight: 600,
              padding: "12px 16px",
              whiteSpace: "nowrap",
            },
            rowStyle: {
              fontSize: 13,
              color: "#333",
            },
            searchFieldStyle: {
              padding: "8px 12px",
              fontSize: 14,
            },
            searchFieldVariant: "outlined",
            selection: true,
            filtering: false,
            sorting: true,
            exportButton: false,
            searchFieldAlignment: "right",
            searchAutoFocus: false,
            pageSizeOptions: [10, 20, 50, 100],
            pageSize: 10,
            showFirstLastPageButtons: true,
            debounceInterval: 400,
            padding: "dense",
            toolbarButtonAlignment: "left",
          }}
          onSelectionChange={handleSelectionChange}
          localization={{
            toolbar: {
              searchPlaceholder: "Search patients...",
              searchTooltip: "Search",
            },
            body: {
              emptyDataSourceMessage:
                "No patients found. Use filters above to search.",
            },
          }}
        />
      </Card>
    </div>
  );
};

export default PatientList;
