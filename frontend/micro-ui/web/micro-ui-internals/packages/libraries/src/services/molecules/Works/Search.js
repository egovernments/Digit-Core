// import cloneDeep from "lodash/cloneDeep";
import _ from "lodash";
import { WorksService } from "../../elements/Works";
import { format } from "date-fns";
// import HrmsService from "../../elements/HRMS";
// import { convertEpochToDate } from "../../../utils/pt";

const createProjectsArray = (t, project, searchParams, headerLocale) => {

    let totalProjects = {
        searchedProject : {},
        subProjects : []
    };
    let basicDetails = {};
    let totalProjectsLength = project.length;
    for(let projectIndex = 0; projectIndex < totalProjectsLength; projectIndex++) {
        let currentProject = project[projectIndex];
       
        const financialDetails = {
            title: " ",
            asSectionHeader: false,
            values: [
              { title: "WORKS_FUND", value: currentProject?.additionalDetails?.fund ? t(`ES_COMMON_FIN_${currentProject?.additionalDetails?.fund}`) : "NA" },
              { title: "WORKS_FUNCTION", value: currentProject?.additionalDetails?.function ? t(`ES_COMMON_${currentProject?.additionalDetails?.function}`) : "NA" },
              { title: "WORKS_BUDGET_HEAD", value: currentProject?.additionalDetails?.budgetHead ? t(`ES_COMMON_FIN_${currentProject?.additionalDetails?.budgetHead}`)  : "NA"},
              { title: "WORKS_SCHEME", value: currentProject?.additionalDetails?.scheme ? t(`ES_COMMON_${currentProject?.additionalDetails?.scheme}`) : "NA"},
              { title: "WORKS_SUB_SCHEME", value: currentProject?.additionalDetails?.subScheme ? t(`ES_COMMON_${currentProject?.additionalDetails?.subScheme}`) : "NA"}
            ],
          };
          const departmentDetails = {
            title: " ",
            asSectionHeader: false,
            values: [
                    { title: "PROJECT_OWNING_DEPT", value: currentProject?.department ? t(`COMMON_MASTERS_DEPARTMENT_${currentProject?.department}`) : "NA" },
                    { title: "PROJECT_TARGET_DEMOGRAPHY",value: currentProject?.additionalDetails?.targetDemography?.code ? t(`COMMON_MASTERS_${currentProject?.additionalDetails?.targetDemography?.code }`) : "NA" },
                    { title: "WORKS_LOR", value: currentProject?.referenceID || "NA" },
                    { title: "PROJECT_ESTIMATED_COST", value: currentProject?.additionalDetails?.estimatedCostInRs || "NA" },
                ]
            };
            const workTypeDetails = {
                title: "PROJECT_WORK_TYPE_DETAILS",
                asSectionHeader: true,
                values: [
                    { title: "WORKS_PROJECT_TYPE", value: currentProject?.projectType ? t(`WORKS_PROJECT_TYPE_${currentProject?.projectType}`) : "NA" }, //backend to update this
                    { title: "WORKS_SUB_PROJECT_TYPE", value: currentProject?.projectSubType ? t(`ES_COMMON_${currentProject?.projectSubType}`) : "NA" }, //backend to update this
                    { title: "WORKS_WORK_NATURE", value: currentProject?.natureOfWork?.code ? t(`ES_COMMON_${currentProject?.natureOfWork?.code}`) : "NA" },
                    { title: "WORKS_MODE_OF_INS", value: "NA" }, //backend to update this
                ],
            };
            const locationDetails = {
                title: "WORKS_LOCATION_DETAILS",
                asSectionHeader: true,
                values: [
                    { title: "WORKS_LOCALITY",value: currentProject?.address?.boundary ? t(`${headerLocale}_ADMIN_${currentProject?.address?.boundary}`) : "NA" },
                    { title: "WORKS_WARD", value: "NA" }, ///backend to update this
                    { title: "PDF_STATIC_LABEL_ESTIMATE_ULB", value: currentProject?.address?.city ? t(currentProject?.address?.city) : "NA" }, //will check with Backend
                    { title: "WORKS_GEO_LOCATION",value: currentProject?.address?.addressLine1 || "NA" }, //will check with Backend
                ],
            };

            const documentDetails = {
                title: "",
                asSectionHeader: true,
                additionalDetails: {
                    documents: [{
                        title: "CS_COMMON_DOCUMENTS",
                        BS : 'Works',
                        values: currentProject?.documents?.map((document) => {
                            return {
                                title: document?.additionalDetails?.fileName,
                                documentType: document?.documentType,
                                documentUid: document?.fileStore,
                                fileStoreId: document?.fileStore,
                            };
                        }),
                    },
                    ]
                }
            }
            if(currentProject?.projectNumber === searchParams?.Projects?.[0]?.projectNumber) {
                basicDetails = {
                    projectID : currentProject?.projectNumber,
                    projectProposalDate : convertEpochToDate(currentProject?.additionalDetails?.dateOfProposal) || "NA", //need to check this with Chetan
                    projectName : currentProject?.name || "NA",
                    projectDesc : currentProject?.description || "NA",
                    projectHasSubProject : (totalProjectsLength > 1 ? "COMMON_YES" : "COMMON_NO"),
                    projectParentProjectID : currentProject?.ancestors?.[0]?.projectNumber || "NA",
                    uuid:currentProject?.id,
                    address:currentProject?.address,
                    ward: currentProject?.additionalDetails?.ward
                }
                totalProjects.searchedProject = {
                    basicDetails : basicDetails,
                    financialDetails : financialDetails,
                    departmentDetails : departmentDetails,
                    workTypeDetails : workTypeDetails,
                    locationDetails : locationDetails,
                    documentDetails : documentDetails
                }
            }else {
                //sub projects dont have financial details
                //these keys are mapped to the view table
                totalProjects.subProjects?.push({
                    name :  currentProject?.name || "NA",
                    estimatedAmount : currentProject?.additionalDetails?.estimatedCostInRs || "NA",
                    type : currentProject?.projectType || "NA",
                    subType : currentProject?.projectSubType || "NA",
                    natureOfWork : currentProject?.natureOfWork || "NA", //not consumed by API
                    startDate : currentProject?.startDate || "NA",
                    endDate : currentProject?.endDate || "NA",
                    modeOfEntrustment : currentProject?.modeOfEntrustment || "NA", //not consumed by API
                    ward : currentProject?.ward || "NA", //not consumed by API
                    locality : currentProject?.address?.locality || "NA",
                    ulb : currentProject?.address?.city || "NA",
                    geoLocation : currentProject?.address?.addressLine1 || "NA", // this will change to Latitude and Longitude
                    uploadedDocuments : [{
                        BS : 'Works',
                        values: currentProject?.documents?.map((document) => {
                                return {
                                    title: document?.additionalDetails?.fileName,
                                    documentType: document?.documentType,
                                    documentUid: document?.fileStore,
                                    fileStoreId: document?.fileStore,
                                };
                            }),
                        },
                    ]
                });
            }
    }
    return totalProjects;
}

export const WorksSearch = {
    searchEstimate: async (tenantId="pb.jalandhar", filters = {} ) => {
        
        //dymmy response
        //const response = sampleEstimateSearchResponse
        //actual response
        const response = await WorksService?.estimateSearch({tenantId,filters})
        return response?.estimates
    },
    searchLOI: async (tenantId,filters={}) => {
        //dymmy response
        
        //const response = sampleLOISearchResponse
        //actual response
        const response = await WorksService?.loiSearch({tenantId,filters})
        return response?.letterOfIndents
    },
    viewProjectDetailsScreen: async(t,tenantId, searchParams, filters = {limit : 10, offset : 0, includeAncestors : true, includeDescendants : true}, headerLocale)=> {
        const response = await WorksService?.searchProject(tenantId, searchParams, filters);

        //Categoring the response into an object of searched project and its sub-projects ( if any )
        //searched projects will have basic details, project details and financial details
        //subprojects will be shown in a table similar to what create project has
        let projectDetails = {
            searchedProject : {
                basicDetails : {},
                details : {
                    projectDetails : [],
                    financialDetails : []
                }
            },
            subProjects : []
        }

        //Upon Search, we will get a response of one Project which will be our Searched Projects
        //That project will have descendants, which will be the part of Sub-Projects.

        let projects = createProjectsArray(t, response?.Projects, searchParams, headerLocale);
        //searched Project details
        projectDetails.searchedProject['basicDetails'] = projects?.searchedProject?.basicDetails;
        projectDetails.searchedProject['details']['projectDetails'] = {applicationDetails : [projects?.searchedProject?.departmentDetails, projects?.searchedProject?.workTypeDetails, projects?.searchedProject?.locationDetails, projects?.searchedProject?.documentDetails]}; //rest categories will come here
        projectDetails.searchedProject['details']['financialDetails'] = {applicationDetails :  [projects?.searchedProject?.financialDetails]}; //rest categories will come here

        if(response?.Projects?.[0]?.descendants) {
            projects = createProjectsArray(t, response?.Projects?.[0]?.descendants, searchParams, headerLocale);
            //all details of searched project will come here
            projectDetails.subProjects?.push(projects?.subProjects);
        }
        return {
            projectDetails : projectDetails,
            processInstancesDetails: [],
            applicationData: {},
            workflowDetails: [],
            applicationData:{}
        }
    }, 
    viewEstimateScreen: async (t, tenantId, estimateNumber) => {
        const estimateArr = await WorksSearch?.searchEstimate(tenantId, { estimateNumber })
        const estimate = estimateArr?.[0]
        
        const sample = {
          title:" ",
          asSectionHeader: true,
          values: [
            { title: "JAGAN", value: estimate?.tenantId },// added a different view screen
            { title: "EVENTS_NAME_LABEL", value: estimate?.name },
            { title: "EVENTS_CATEGORY_LABEL", value: estimate?.wfStatus },
            { title: "EVENTS_DESCRIPTION_LABEL", value: estimate?.id },
            { title: "EVENTS_FROM_DATE_LABEL", value: format(new Date(estimate?.auditDetails?.createdTime), 'dd/MM/yyyy') },
            { title: "EVENTS_TO_DATE_LABEL", value: format(new Date(estimate?.auditDetails?.createdTime), 'dd/MM/yyyy') },
            { title: "EVENTS_FROM_TIME_LABEL", value: format(new Date(estimate?.auditDetails?.lastModifiedTime), 'hh:mm'), skip: true },
            { title: "EVENTS_TO_TIME_LABEL", value: format(new Date(estimate?.auditDetails?.lastModifiedTime), 'hh:mm'), skip: true },
            { title: "EVENTS_ADDRESS_LABEL", value: estimate?.additionalDetails?.ward },
            { title: "EVENTS_MAP_LABEL",
              map: true,
              value: 'N/A' 
            },
            { title: "EVENTS_ORGANIZER_NAME_LABEL", value: estimate?.estimateNumber },
            { title: "EVENTS_ENTRY_FEE_INR_LABEL", value: estimate?.id },
          ]
        }

        const nonSOR = estimate?.estimateDetails?.filter(row=>row?.category?.includes("NON-SOR") && row?.isActive)
        const overheads = estimate?.estimateDetails?.filter(row => row?.category?.includes("OVERHEAD") && row?.isActive)
        

        const tableHeaderNonSor = [t("WORKS_SNO"), t("EVENTS_DESCRIPTION"), t("PROJECT_UOM"), t("CS_COMMON_RATE"), t("WORKS_ESTIMATED_QUANTITY"), t("WORKS_ESTIMATED_AMOUNT")] 
        const tableHeaderOverheads = [t("WORKS_SNO"), t("WORKS_OVERHEAD"), t("WORKS_PERCENTAGE"), t("WORKS_AMOUNT")]
        
        const tableRowsNonSor = nonSOR?.map((row,index)=>{
            return [
                index+1,
                row?.description,
                t(`ES_COMMON_UOM_${row?.uom}`),
                row?.unitRate,
                row?.noOfunit,
                row?.amountDetail[0]?.amount?.toFixed(2)
            ]
        })
        const totalAmountNonSor = nonSOR?.reduce((acc, row) => row?.amountDetail?.[0]?.amount + acc,0)
        tableRowsNonSor?.push(["","","","" ,t("RT_TOTAL"), totalAmountNonSor])
        
        const tableRowsOverheads = overheads?.map((row, index) => {
            return [
                index + 1,
                t(`ES_COMMON_OVERHEADS_${row?.name}`),
                row?.additionalDetails?.row?.name?.type?.includes("percent") ? `${row?.additionalDetails?.row?.name?.value}%`:t("WORKS_LUMPSUM"),
                row?.amountDetail?.[0]?.amount?.toFixed(2)
            ]
        })
        const totalAmountOverheads = overheads?.reduce((acc, row) => row?.amountDetail?.[0]?.amount + acc, 0)
        tableRowsOverheads?.push(["","", t("RT_TOTAL"), totalAmountOverheads])
        const nonSorItems = {
            title: "WORKS_NON_SOR",
            asSectionHeader: true,
            isTable: true,
            headers: tableHeaderNonSor,
            tableRows: tableRowsNonSor,
            state: estimate,
            tableStyles:{
                rowStyle:{},
                cellStyle: [{}, { "width": "40vw",whiteSpace: 'break-spaces',
                wordBreak: 'break-all' }, {}, {"textAlign":"right"}, {"textAlign":"left"},{"textAlign":"right"}]
            }
        }
        const overheadItems = {
            title: "WORKS_OVERHEADS",
            asSectionHeader: true,
            isTable: true,
            headers: tableHeaderOverheads,
            tableRows: tableRowsOverheads,
            state: estimate,
            tableStyles: {
                rowStyle: {},
                cellStyle: [{}, { "width": "50vw", whiteSpace: 'break-spaces',
                wordBreak: 'break-all' }, {"textAlign":"left"}, { "textAlign": "right" }]
            }
        }
        
        const files = estimate?.additionalDetails?.documents
        const documentDetails = {
            title: "",
            asSectionHeader: true,
            additionalDetails: {
                documents: [{
                    title: "WORKS_RELEVANT_DOCS",
                    BS: 'Works',
                    values: files?.filter(doc=>doc?.fileStoreId)?.map((document) => {
                        return {
                            title: document?.fileType==="Others"?document?.fileName:document?.fileType,
                            documentType: document?.documentType,
                            documentUid: document?.fileStoreId,
                            fileStoreId: document?.fileStoreId,
                        };
                    }),
                },
                ]
            }
        }

        const totalEstAmt = {
            "title": " ",
            "asSectionHeader": true,
            "Component": Digit.ComponentRegistryService.getComponent("ViewTotalEstAmount"),
            "value": Math.round(estimate?.additionalDetails?.totalEstimatedAmount)|| t("NA")
        }

        const labourDetails = {
            "title": "ESTIMATE_LABOUR_ANALYSIS",
            "asSectionHeader": true,
            "Component": Digit.ComponentRegistryService.getComponent("ViewLabourAnalysis"),
            "value": [
                {
                    "title": "ESTIMATE_LABOUR_COST",
                    "value":estimate?.additionalDetails?.labourMaterialAnalysis?.labour || t("NA")
                },
                {
                    "title": "ESTIMATE_MATERIAL_COST",
                    "value": estimate?.additionalDetails?.labourMaterialAnalysis?.material || t("NA")
                },
            ]
        }
        const details = [sample,nonSorItems, overheadItems,totalEstAmt,labourDetails,documentDetails]

        return {
            applicationDetails: details,
            applicationData:estimate,
            isNoDataFound : estimateArr.length === 0 ? true : false
        }
    },
    workflowDataDetails: async (tenantId, businessIds) => {
        const response = await Digit.WorkflowService.getByBusinessId(tenantId, businessIds);
        return response;
    },
    viewLOIScreen: async (t, tenantId, loiNumber,subEstimateNumber) => {
        
        
        const workflowDetails = await WorksSearch.workflowDataDetails(tenantId, loiNumber);

        const loiArr = await WorksSearch.searchLOI(tenantId, {letterOfIndentNumber:loiNumber})
         const loi = loiArr?.[0]
        //const loi = sampleLOISearchResponse?.letterOfIndents?.[0]
        //const estimate = sampleEstimateSearchResponse?.estimates?.[0]
        const estimateArr = await WorksSearch?.searchEstimate(tenantId, { estimateDetailNumber:subEstimateNumber })
        const estimate = estimateArr?.[0]   
       
        const additionalDetails = loi?.additionalDetails
        // const userInfo = Digit.UserService.getUser()?.info || {};
        // const uuidUser = userInfo?.uuid;
        // const {user:users} = await Digit.UserService.userSearch(tenantId, { uuid: [loi?.oicId] }, {});
        // const usersResponse = await HrmsService.search(tenantId,{codes: loi?.oicId }, {});
        // const user = users?.[0]
        
        const loiDetails = {
            title: "WORKS_LOI_DETAILS",
            asSectionHeader: true,
            values: [
                { title: "WORKS_LOI_NUMBER", value: loi?.letterOfIndentNumber || t("NA") },
                { title: "WORKS_DATE_CREATED", value: convertEpochToDate(loi?.auditDetails?.createdTime) || t("NA") },
                { title: "WORKS_ESTIMATE_NO", value: estimate?.estimateNumber || t("NA") },
                { title: "WORKS_SUB_ESTIMATE_NO", value: subEstimateNumber },
                { title: "WORKS_NAME_OF_WORK", value: estimate?.estimateDetails?.filter(subEs => subEs?.estimateDetailNumber === subEstimateNumber)?.[0]?.name || t("NA") },
                { title: "WORKS_DEPARTMENT", value: t(`ES_COMMON_${estimate?.department}`) || t("NA") },
                { title: "WORKS_FILE_NO", value: loi?.fileNumber || t("NA") },
                { title: "WORKS_FILE_DATE", value: convertEpochToDate(loi?.fileDate) || t("NA") },
            ]
        };

        const subEs = estimate?.estimateDetails?.filter(subEs => subEs?.estimateDetailNumber === subEstimateNumber)?.[0]

        const agreementAmount = subEs?.amount + ((parseInt(loi?.negotiatedPercentage) * subEs?.amount) / 100)


        const financialDetails = {
            title: "WORKS_FINANCIAL_DETAILS",
            asSectionHeader: true,
            values: [
                { title: "WORKS_ESTIMATED_AMT", value: estimate?.estimateDetails?.filter(subEs => subEs?.estimateDetailNumber === subEstimateNumber)?.[0]?.amount || t("NA") },
                { title: "WORKS_FINALIZED_PER", value: loi?.negotiatedPercentage || t("NA") },
                { title: "WORKS_AGREEMENT_AMT", value: agreementAmount || t("NA") },
            ]
        }
        const agreementDetails = {
            title: "WORKS_AGGREEMENT_DETAILS",
            asSectionHeader: true,
            values: [
                { title: "WORKS_AGENCY_NAME", value: t("NA") },
                { title: "WORKS_CONT_ID", value: loi?.contractorId || t("NA") },
                { title: "WORKS_PREPARED_BY", value:  t("NA") },
                { title: "WORKS_ADD_SECURITY_DP", value:loi?.securityDeposit || t("NA") },
                { title: "WORKS_BANK_G", value: loi?.bankGuarantee || t("NA") },
                { title: "WORKS_EMD", value: loi?.emdAmount || t("NA") },
                { title: "WORKS_INCHARGE_ENGG", value: additionalDetails?.oic?.nameOfEmp || t("NA") },
            ]
        }
        const files = additionalDetails?.filesAttached
        

        const documentDetails = {
            title: "",
            asSectionHeader: true,
            additionalDetails: {
                documents: [{
                    title: "WORKS_RELEVANT_DOCS",
                    BS: 'Works',
                    values: files?.map((document) => {
                        return {
                            title: document?.fileName,
                            documentType: document?.documentType,
                            documentUid: document?.fileStoreId,
                            fileStoreId: document?.fileStoreId,
                        };
                    }),
                },
                ]
            }
        }

        let details = [loiDetails,financialDetails,agreementDetails,documentDetails]
        return {
            applicationDetails: details,
            processInstancesDetails: workflowDetails?.ProcessInstances,
            applicationData:loi,
        }
    },
    viewProjectClosureScreen: (tenantId) => {
        //dummy estimate data
        const result = {
            "applicationDetails": [
                {
                    "title": " ",
                    "asSectionHeader": true,
                    "values": [
                        {
                            "title": "WORKS_ESTIMATE_ID",
                            "value": "EP/2022-23/12/000174"
                        },
                        {
                            "title": "WORKS_STATUS",
                            "value": "Checked"
                        }
                    ]
                },
                {
                    "title": "WORKS_ESTIMATE_DETAILS",
                    "asSectionHeader": true,
                    "values": [
                        {
                            "title": "WORKS_DATE_PROPOSAL",
                            "value": "13/12/2022"
                        },
                        {
                            "title": "WORKS_LOR",
                            "value": "123123"
                        },
                    ]
                },
                {
                    "title": "WORKS_LOCATION_DETAILS",
                    "asSectionHeader": true,
                    "values": [
                        {
                            "title": "WORKS_LOCATION",
                            "value": "Venkata Ramana Colony, gate no 1"
                        },
                        {
                            "title": "WORKS_LOCALITY",
                            "value": "Venkata Ramana Colony"
                        },
                        {
                            "title": "WORKS_WARD",
                            "value": "Ward No 23"
                        },
                        {
                            "title": "CS_SELECTED_TEXT",
                            "value": "Patna"
                        },
                        {
                            "title": "WORKS_GEO_LOCATION",
                            "value": "Patna"
                        },
                    ]
                },
                {
                    "title": "WORKS_WORK_DETAILS",
                    "asSectionHeader": true,
                    "values": [
                        {
                            "title": "WORKS_WORK_NATURE",
                            "value": "General Fund"
                        },
                        {
                            "title": "WORKS_WORK_TYPE",
                            "value": "General Fund"
                        },
                    ]
                },
                {
                    "title": "WORKS_FINANCIAL_DETAILS",
                    "asSectionHeader": true,
                    "values": [
                        {
                            "title": "WORKS_CHART_OF_ACCOUNT",
                            "value": "General Fund"
                        },
                    ]
                },
                {
                    "title": "WORKS_WORK_DETAILS",
                    "asSectionHeader": true,
                    "isTable": true,
                    "headers": [
                        "WORKS_SNO",
                        "WORKS_NAME_OF_WORK",
                        "WORKS_ESTIMATED_AMOUNT"
                    ],
                    "tableRows": [
                        [
                            1,
                            "work",
                            "12,312"
                        ],
                        [
                            "",
                            "Total Amount",
                            "12,312"
                        ]
                    ],
                    "state": {
                        "id": "7a376942-12b6-48ad-8054-9883c53b18d7",
                        "tenantId": "pb.amritsar",
                        "estimateNumber": "EP/2022-23/12/000174",
                        "adminSanctionNumber": null,
                        "proposalDate": 1670920740935,
                        "status": "ACTIVE",
                        "estimateStatus": "CHECKED",
                        "subject": "Construct new schools v2",
                        "requirementNumber": "123123",
                        "description": "Construct new schools",
                        "department": "DEPT_1",
                        "location": "",
                        "workCategory": "Engineering",
                        "beneficiaryType": "General",
                        "natureOfWork": "Operation & Maintenance",
                        "typeOfWork": "Road",
                        "subTypeOfWork": "RD01",
                        "entrustmentMode": "Nomination",
                        "fund": "01",
                        "function": "0001",
                        "budgetHead": "01",
                        "scheme": "15th CFC",
                        "subScheme": "15th CFC-01",
                        "totalAmount": null,
                        "estimateDetails": [
                            {
                                "id": "9dfa2d69-497a-487c-ae42-ab0edbf7724e",
                                "estimateDetailNumber": "EP/2022-23/12/000174/000155",
                                "name": "work",
                                "amount": 12312,
                                "additionalDetails": null
                            }
                        ],
                        "auditDetails": {
                            "createdBy": "be99b2c2-5780-4b1c-8e41-e3f8a972ebda",
                            "lastModifiedBy": "be99b2c2-5780-4b1c-8e41-e3f8a972ebda",
                            "createdTime": 1670920740935,
                            "lastModifiedTime": 1672053008013
                        },
                        "additionalDetails": {
                            "formData": [
                                {
                                    "name": "work",
                                    "amount": 12312
                                }
                            ]
                        }
                    }
                },
                {
                    "title": "",
                    "asSectionHeader": true,
                    "additionalDetails": {
                        "documents": [
                            {
                                "title": "WORKS_RELEVANT_DOCS",
                                "BS": "Works"
                            }
                        ]
                    }
                }
            ],
            "processInstancesDetails": [
                {
                    "id": "1b9a428d-5c1c-4219-a878-015217aa9d94",
                    "tenantId": "pb.amritsar",
                    "businessService": "estimate-approval-2",
                    "businessId": "EP/2022-23/12/000174",
                    "action": "CHECK",
                    "moduleName": "estimate-service",
                    "state": {
                        "auditDetails": null,
                        "uuid": "e970bdf2-a968-4be5-b0fe-bc6584e62829",
                        "tenantId": "pb",
                        "businessServiceId": "52e2c4e0-f12c-4c75-aef3-1535bc8edac0",
                        "sla": null,
                        "state": "CHECKED",
                        "applicationStatus": "CHECKED",
                        "docUploadRequired": false,
                        "isStartState": false,
                        "isTerminateState": false,
                        "isStateUpdatable": null,
                        "actions": [
                            {
                                "auditDetails": null,
                                "uuid": "a952bc13-07ef-4384-9214-9c7c3e974ec8",
                                "tenantId": "pb",
                                "currentState": "e970bdf2-a968-4be5-b0fe-bc6584e62829",
                                "action": "TECHNICALSANCATION",
                                "nextState": "e41d89f8-0977-4b43-9193-3e17c1257ff6",
                                "roles": [
                                    "EST_TECH_SANC"
                                ],
                                "active": null
                            },
                            {
                                "auditDetails": null,
                                "uuid": "c788321f-dc5b-4dc8-a6e6-bd78c6a769fb",
                                "tenantId": "pb",
                                "currentState": "e970bdf2-a968-4be5-b0fe-bc6584e62829",
                                "action": "REJECT",
                                "nextState": "af66155b-f5ac-447f-947b-f56539c4d671",
                                "roles": [
                                    "EST_TECH_SANC"
                                ],
                                "active": null
                            }
                        ]
                    },
                    "comment": null,
                    "documents": null,
                    "assigner": {
                        "id": 109,
                        "userName": "Nipsyyyy",
                        "name": "Nipun ",
                        "type": "EMPLOYEE",
                        "mobileNumber": "9667076655",
                        "emailId": "",
                        "roles": [
                            {
                                "id": null,
                                "name": "Employee",
                                "code": "EMPLOYEE",
                                "tenantId": "pb.amritsar"
                            },
                            {
                                "id": null,
                                "name": "EST CREATOR",
                                "code": "EST_CREATOR",
                                "tenantId": "pb.amritsar"
                            },
                            {
                                "id": null,
                                "name": "EST_CHECKER",
                                "code": "EST_CHECKER",
                                "tenantId": "pb.amritsar"
                            },
                            {
                                "id": null,
                                "name": "LOI APPROVER",
                                "code": "LOI_APPROVER",
                                "tenantId": "pb.amritsar"
                            },
                            {
                                "id": null,
                                "name": "EST TECH SANC",
                                "code": "EST_TECH_SANC",
                                "tenantId": "pb.amritsar"
                            },
                            {
                                "id": null,
                                "name": "EST FIN SANC",
                                "code": "EST_FIN_SANC",
                                "tenantId": "pb.amritsar"
                            },
                            {
                                "id": null,
                                "name": "LOI CREATOR",
                                "code": "LOI_CREATOR",
                                "tenantId": "pb.amritsar"
                            },
                            {
                                "id": null,
                                "name": "SUPER USER",
                                "code": "SUPERUSER",
                                "tenantId": "pb.amritsar"
                            },
                            {
                                "id": null,
                                "name": "EST TECH SANC",
                                "code": "EST_ADMIN_SANC",
                                "tenantId": "pb.amritsar"
                            },
                            {
                                "id": null,
                                "name": "LOI CHECKER",
                                "code": "LOI_CHECKER",
                                "tenantId": "pb.amritsar"
                            },
                            {
                                "id": null,
                                "name": "HRMS Admin",
                                "code": "HRMS_ADMIN",
                                "tenantId": "pb.amritsar"
                            }
                        ],
                        "tenantId": "pb.amritsar",
                        "uuid": "be99b2c2-5780-4b1c-8e41-e3f8a972ebda"
                    },
                    "assignes": [
                        {
                            "id": 109,
                            "userName": "Nipsyyyy",
                            "name": "Nipun ",
                            "type": "EMPLOYEE",
                            "mobileNumber": "9667076655",
                            "emailId": "",
                            "roles": [
                                {
                                    "id": null,
                                    "name": "Employee",
                                    "code": "EMPLOYEE",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "EST CREATOR",
                                    "code": "EST_CREATOR",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "EST_CHECKER",
                                    "code": "EST_CHECKER",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "LOI APPROVER",
                                    "code": "LOI_APPROVER",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "EST TECH SANC",
                                    "code": "EST_TECH_SANC",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "EST FIN SANC",
                                    "code": "EST_FIN_SANC",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "LOI CREATOR",
                                    "code": "LOI_CREATOR",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "SUPER USER",
                                    "code": "SUPERUSER",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "EST TECH SANC",
                                    "code": "EST_ADMIN_SANC",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "LOI CHECKER",
                                    "code": "LOI_CHECKER",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "HRMS Admin",
                                    "code": "HRMS_ADMIN",
                                    "tenantId": "pb.amritsar"
                                }
                            ],
                            "tenantId": "pb.amritsar",
                            "uuid": "be99b2c2-5780-4b1c-8e41-e3f8a972ebda"
                        }
                    ],
                    "nextActions": [
                        {
                            "auditDetails": null,
                            "uuid": "c788321f-dc5b-4dc8-a6e6-bd78c6a769fb",
                            "tenantId": "pb",
                            "currentState": "e970bdf2-a968-4be5-b0fe-bc6584e62829",
                            "action": "REJECT",
                            "nextState": "af66155b-f5ac-447f-947b-f56539c4d671",
                            "roles": [
                                "EST_TECH_SANC"
                            ],
                            "active": null
                        },
                        {
                            "auditDetails": null,
                            "uuid": "a952bc13-07ef-4384-9214-9c7c3e974ec8",
                            "tenantId": "pb",
                            "currentState": "e970bdf2-a968-4be5-b0fe-bc6584e62829",
                            "action": "TECHNICALSANCATION",
                            "nextState": "e41d89f8-0977-4b43-9193-3e17c1257ff6",
                            "roles": [
                                "EST_TECH_SANC"
                            ],
                            "active": null
                        }
                    ],
                    "stateSla": null,
                    "businesssServiceSla": -793324953,
                    "previousStatus": null,
                    "entity": null,
                    "auditDetails": {
                        "createdBy": "be99b2c2-5780-4b1c-8e41-e3f8a972ebda",
                        "lastModifiedBy": "be99b2c2-5780-4b1c-8e41-e3f8a972ebda",
                        "createdTime": 1672053008038,
                        "lastModifiedTime": 1672053008038
                    },
                    "rating": 0,
                    "escalated": false
                },
                {
                    "id": "0a618d8b-8604-4aae-8e70-c45e3fccdf51",
                    "tenantId": "pb.amritsar",
                    "businessService": "estimate-approval-2",
                    "businessId": "EP/2022-23/12/000174",
                    "action": "CREATE",
                    "moduleName": "estimate-service",
                    "state": {
                        "auditDetails": null,
                        "uuid": "67d17040-0c49-40a1-b932-a7b5a5266557",
                        "tenantId": "pb",
                        "businessServiceId": "52e2c4e0-f12c-4c75-aef3-1535bc8edac0",
                        "sla": null,
                        "state": "CREATED",
                        "applicationStatus": "CREATED",
                        "docUploadRequired": false,
                        "isStartState": true,
                        "isTerminateState": false,
                        "isStateUpdatable": null,
                        "actions": [
                            {
                                "auditDetails": null,
                                "uuid": "568b7e7d-d88f-4079-bb02-3dc9a37c56ea",
                                "tenantId": "pb",
                                "currentState": "67d17040-0c49-40a1-b932-a7b5a5266557",
                                "action": "CHECK",
                                "nextState": "e970bdf2-a968-4be5-b0fe-bc6584e62829",
                                "roles": [
                                    "EST_CHECKER"
                                ],
                                "active": null
                            },
                            {
                                "auditDetails": null,
                                "uuid": "1a6d9f29-893d-49d9-870f-6e007a6820e8",
                                "tenantId": "pb",
                                "currentState": "67d17040-0c49-40a1-b932-a7b5a5266557",
                                "action": "REJECT",
                                "nextState": "af66155b-f5ac-447f-947b-f56539c4d671",
                                "roles": [
                                    "EST_CHECKER"
                                ],
                                "active": null
                            }
                        ]
                    },
                    "comment": "string",
                    "documents": null,
                    "assigner": {
                        "id": 109,
                        "userName": "Nipsyyyy",
                        "name": "Nipun ",
                        "type": "EMPLOYEE",
                        "mobileNumber": "9667076655",
                        "emailId": "",
                        "roles": [
                            {
                                "id": null,
                                "name": "Employee",
                                "code": "EMPLOYEE",
                                "tenantId": "pb.amritsar"
                            },
                            {
                                "id": null,
                                "name": "EST CREATOR",
                                "code": "EST_CREATOR",
                                "tenantId": "pb.amritsar"
                            },
                            {
                                "id": null,
                                "name": "EST_CHECKER",
                                "code": "EST_CHECKER",
                                "tenantId": "pb.amritsar"
                            },
                            {
                                "id": null,
                                "name": "LOI APPROVER",
                                "code": "LOI_APPROVER",
                                "tenantId": "pb.amritsar"
                            },
                            {
                                "id": null,
                                "name": "EST TECH SANC",
                                "code": "EST_TECH_SANC",
                                "tenantId": "pb.amritsar"
                            },
                            {
                                "id": null,
                                "name": "EST FIN SANC",
                                "code": "EST_FIN_SANC",
                                "tenantId": "pb.amritsar"
                            },
                            {
                                "id": null,
                                "name": "LOI CREATOR",
                                "code": "LOI_CREATOR",
                                "tenantId": "pb.amritsar"
                            },
                            {
                                "id": null,
                                "name": "SUPER USER",
                                "code": "SUPERUSER",
                                "tenantId": "pb.amritsar"
                            },
                            {
                                "id": null,
                                "name": "EST TECH SANC",
                                "code": "EST_ADMIN_SANC",
                                "tenantId": "pb.amritsar"
                            },
                            {
                                "id": null,
                                "name": "LOI CHECKER",
                                "code": "LOI_CHECKER",
                                "tenantId": "pb.amritsar"
                            },
                            {
                                "id": null,
                                "name": "HRMS Admin",
                                "code": "HRMS_ADMIN",
                                "tenantId": "pb.amritsar"
                            }
                        ],
                        "tenantId": "pb.amritsar",
                        "uuid": "be99b2c2-5780-4b1c-8e41-e3f8a972ebda"
                    },
                    "assignes": [
                        {
                            "id": 109,
                            "userName": "Nipsyyyy",
                            "name": "Nipun ",
                            "type": "EMPLOYEE",
                            "mobileNumber": "9667076655",
                            "emailId": "",
                            "roles": [
                                {
                                    "id": null,
                                    "name": "Employee",
                                    "code": "EMPLOYEE",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "EST CREATOR",
                                    "code": "EST_CREATOR",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "EST_CHECKER",
                                    "code": "EST_CHECKER",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "LOI APPROVER",
                                    "code": "LOI_APPROVER",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "EST TECH SANC",
                                    "code": "EST_TECH_SANC",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "EST FIN SANC",
                                    "code": "EST_FIN_SANC",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "LOI CREATOR",
                                    "code": "LOI_CREATOR",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "SUPER USER",
                                    "code": "SUPERUSER",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "EST TECH SANC",
                                    "code": "EST_ADMIN_SANC",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "LOI CHECKER",
                                    "code": "LOI_CHECKER",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "HRMS Admin",
                                    "code": "HRMS_ADMIN",
                                    "tenantId": "pb.amritsar"
                                }
                            ],
                            "tenantId": "pb.amritsar",
                            "uuid": "be99b2c2-5780-4b1c-8e41-e3f8a972ebda"
                        }
                    ],
                    "nextActions": [
                        {
                            "auditDetails": null,
                            "uuid": "568b7e7d-d88f-4079-bb02-3dc9a37c56ea",
                            "tenantId": "pb",
                            "currentState": "67d17040-0c49-40a1-b932-a7b5a5266557",
                            "action": "CHECK",
                            "nextState": "e970bdf2-a968-4be5-b0fe-bc6584e62829",
                            "roles": [
                                "EST_CHECKER"
                            ],
                            "active": null
                        },
                        {
                            "auditDetails": null,
                            "uuid": "1a6d9f29-893d-49d9-870f-6e007a6820e8",
                            "tenantId": "pb",
                            "currentState": "67d17040-0c49-40a1-b932-a7b5a5266557",
                            "action": "REJECT",
                            "nextState": "af66155b-f5ac-447f-947b-f56539c4d671",
                            "roles": [
                                "EST_CHECKER"
                            ],
                            "active": null
                        }
                    ],
                    "stateSla": null,
                    "businesssServiceSla": -793324953,
                    "previousStatus": null,
                    "entity": null,
                    "auditDetails": {
                        "createdBy": "be99b2c2-5780-4b1c-8e41-e3f8a972ebda",
                        "lastModifiedBy": "be99b2c2-5780-4b1c-8e41-e3f8a972ebda",
                        "createdTime": 1670920740971,
                        "lastModifiedTime": 1670920740971
                    },
                    "rating": 0,
                    "escalated": false
                }
            ],
            "applicationData": {
                "id": "7a376942-12b6-48ad-8054-9883c53b18d7",
                "tenantId": "pb.amritsar",
                "estimateNumber": "EP/2022-23/12/000174",
                "adminSanctionNumber": null,
                "proposalDate": 1670920740935,
                "status": "ACTIVE",
                "estimateStatus": "CHECKED",
                "subject": "Construct new schools v2",
                "requirementNumber": "123123",
                "description": "Construct new schools",
                "department": "DEPT_1",
                "location": "",
                "workCategory": "Engineering",
                "beneficiaryType": "General",
                "natureOfWork": "Operation & Maintenance",
                "typeOfWork": "Road",
                "subTypeOfWork": "RD01",
                "entrustmentMode": "Nomination",
                "fund": "01",
                "function": "0001",
                "budgetHead": "01",
                "scheme": "15th CFC",
                "subScheme": "15th CFC-01",
                "totalAmount": null,
                "estimateDetails": [
                    {
                        "id": "9dfa2d69-497a-487c-ae42-ab0edbf7724e",
                        "estimateDetailNumber": "EP/2022-23/12/000174/000155",
                        "name": "work",
                        "amount": 12312,
                        "additionalDetails": null
                    }
                ],
                "auditDetails": {
                    "createdBy": "be99b2c2-5780-4b1c-8e41-e3f8a972ebda",
                    "lastModifiedBy": "be99b2c2-5780-4b1c-8e41-e3f8a972ebda",
                    "createdTime": 1670920740935,
                    "lastModifiedTime": 1672053008013
                },
                "additionalDetails": {
                    "formData": [
                        {
                            "name": "work",
                            "amount": 12312
                        }
                    ]
                }
            },
            workflowDetails: {
                "ResponseInfo": null,
                "ProcessInstances": [
                    {
                        "id": "1b9a428d-5c1c-4219-a878-015217aa9d94",
                        "tenantId": "pb.amritsar",
                        "businessService": "estimate-approval-2",
                        "businessId": "EP/2022-23/12/000174",
                        "action": "CHECK",
                        "moduleName": "estimate-service",
                        "state": {
                            "auditDetails": null,
                            "uuid": "e970bdf2-a968-4be5-b0fe-bc6584e62829",
                            "tenantId": "pb",
                            "businessServiceId": "52e2c4e0-f12c-4c75-aef3-1535bc8edac0",
                            "sla": null,
                            "state": "CHECKED",
                            "applicationStatus": "CHECKED",
                            "docUploadRequired": false,
                            "isStartState": false,
                            "isTerminateState": false,
                            "isStateUpdatable": null,
                            "actions": [
                                {
                                    "auditDetails": null,
                                    "uuid": "a952bc13-07ef-4384-9214-9c7c3e974ec8",
                                    "tenantId": "pb",
                                    "currentState": "e970bdf2-a968-4be5-b0fe-bc6584e62829",
                                    "action": "TECHNICALSANCATION",
                                    "nextState": "e41d89f8-0977-4b43-9193-3e17c1257ff6",
                                    "roles": [
                                        "EST_TECH_SANC"
                                    ],
                                    "active": null
                                },
                                {
                                    "auditDetails": null,
                                    "uuid": "c788321f-dc5b-4dc8-a6e6-bd78c6a769fb",
                                    "tenantId": "pb",
                                    "currentState": "e970bdf2-a968-4be5-b0fe-bc6584e62829",
                                    "action": "REJECT",
                                    "nextState": "af66155b-f5ac-447f-947b-f56539c4d671",
                                    "roles": [
                                        "EST_TECH_SANC"
                                    ],
                                    "active": null
                                }
                            ]
                        },
                        "comment": null,
                        "documents": null,
                        "assigner": {
                            "id": 109,
                            "userName": "Nipsyyyy",
                            "name": "Nipun ",
                            "type": "EMPLOYEE",
                            "mobileNumber": "9667076655",
                            "emailId": "",
                            "roles": [
                                {
                                    "id": null,
                                    "name": "Employee",
                                    "code": "EMPLOYEE",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "EST CREATOR",
                                    "code": "EST_CREATOR",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "EST_CHECKER",
                                    "code": "EST_CHECKER",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "LOI APPROVER",
                                    "code": "LOI_APPROVER",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "EST TECH SANC",
                                    "code": "EST_TECH_SANC",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "EST FIN SANC",
                                    "code": "EST_FIN_SANC",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "LOI CREATOR",
                                    "code": "LOI_CREATOR",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "SUPER USER",
                                    "code": "SUPERUSER",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "EST TECH SANC",
                                    "code": "EST_ADMIN_SANC",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "LOI CHECKER",
                                    "code": "LOI_CHECKER",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "HRMS Admin",
                                    "code": "HRMS_ADMIN",
                                    "tenantId": "pb.amritsar"
                                }
                            ],
                            "tenantId": "pb.amritsar",
                            "uuid": "be99b2c2-5780-4b1c-8e41-e3f8a972ebda"
                        },
                        "assignes": [
                            {
                                "id": 109,
                                "userName": "Nipsyyyy",
                                "name": "Nipun ",
                                "type": "EMPLOYEE",
                                "mobileNumber": "9667076655",
                                "emailId": "",
                                "roles": [
                                    {
                                        "id": null,
                                        "name": "Employee",
                                        "code": "EMPLOYEE",
                                        "tenantId": "pb.amritsar"
                                    },
                                    {
                                        "id": null,
                                        "name": "EST CREATOR",
                                        "code": "EST_CREATOR",
                                        "tenantId": "pb.amritsar"
                                    },
                                    {
                                        "id": null,
                                        "name": "EST_CHECKER",
                                        "code": "EST_CHECKER",
                                        "tenantId": "pb.amritsar"
                                    },
                                    {
                                        "id": null,
                                        "name": "LOI APPROVER",
                                        "code": "LOI_APPROVER",
                                        "tenantId": "pb.amritsar"
                                    },
                                    {
                                        "id": null,
                                        "name": "EST TECH SANC",
                                        "code": "EST_TECH_SANC",
                                        "tenantId": "pb.amritsar"
                                    },
                                    {
                                        "id": null,
                                        "name": "EST FIN SANC",
                                        "code": "EST_FIN_SANC",
                                        "tenantId": "pb.amritsar"
                                    },
                                    {
                                        "id": null,
                                        "name": "LOI CREATOR",
                                        "code": "LOI_CREATOR",
                                        "tenantId": "pb.amritsar"
                                    },
                                    {
                                        "id": null,
                                        "name": "SUPER USER",
                                        "code": "SUPERUSER",
                                        "tenantId": "pb.amritsar"
                                    },
                                    {
                                        "id": null,
                                        "name": "EST TECH SANC",
                                        "code": "EST_ADMIN_SANC",
                                        "tenantId": "pb.amritsar"
                                    },
                                    {
                                        "id": null,
                                        "name": "LOI CHECKER",
                                        "code": "LOI_CHECKER",
                                        "tenantId": "pb.amritsar"
                                    },
                                    {
                                        "id": null,
                                        "name": "HRMS Admin",
                                        "code": "HRMS_ADMIN",
                                        "tenantId": "pb.amritsar"
                                    }
                                ],
                                "tenantId": "pb.amritsar",
                                "uuid": "be99b2c2-5780-4b1c-8e41-e3f8a972ebda"
                            }
                        ],
                        "nextActions": [
                            {
                                "auditDetails": null,
                                "uuid": "c788321f-dc5b-4dc8-a6e6-bd78c6a769fb",
                                "tenantId": "pb",
                                "currentState": "e970bdf2-a968-4be5-b0fe-bc6584e62829",
                                "action": "REJECT",
                                "nextState": "af66155b-f5ac-447f-947b-f56539c4d671",
                                "roles": [
                                    "EST_TECH_SANC"
                                ],
                                "active": null
                            },
                            {
                                "auditDetails": null,
                                "uuid": "a952bc13-07ef-4384-9214-9c7c3e974ec8",
                                "tenantId": "pb",
                                "currentState": "e970bdf2-a968-4be5-b0fe-bc6584e62829",
                                "action": "TECHNICALSANCATION",
                                "nextState": "e41d89f8-0977-4b43-9193-3e17c1257ff6",
                                "roles": [
                                    "EST_TECH_SANC"
                                ],
                                "active": null
                            }
                        ],
                        "stateSla": null,
                        "businesssServiceSla": -793324953,
                        "previousStatus": null,
                        "entity": null,
                        "auditDetails": {
                            "createdBy": "be99b2c2-5780-4b1c-8e41-e3f8a972ebda",
                            "lastModifiedBy": "be99b2c2-5780-4b1c-8e41-e3f8a972ebda",
                            "createdTime": 1672053008038,
                            "lastModifiedTime": 1672053008038
                        },
                        "rating": 0,
                        "escalated": false
                    },
                    {
                        "id": "0a618d8b-8604-4aae-8e70-c45e3fccdf51",
                        "tenantId": "pb.amritsar",
                        "businessService": "estimate-approval-2",
                        "businessId": "EP/2022-23/12/000174",
                        "action": "CREATE",
                        "moduleName": "estimate-service",
                        "state": {
                            "auditDetails": null,
                            "uuid": "67d17040-0c49-40a1-b932-a7b5a5266557",
                            "tenantId": "pb",
                            "businessServiceId": "52e2c4e0-f12c-4c75-aef3-1535bc8edac0",
                            "sla": null,
                            "state": "CREATED",
                            "applicationStatus": "CREATED",
                            "docUploadRequired": false,
                            "isStartState": true,
                            "isTerminateState": false,
                            "isStateUpdatable": null,
                            "actions": [
                                {
                                    "auditDetails": null,
                                    "uuid": "568b7e7d-d88f-4079-bb02-3dc9a37c56ea",
                                    "tenantId": "pb",
                                    "currentState": "67d17040-0c49-40a1-b932-a7b5a5266557",
                                    "action": "CHECK",
                                    "nextState": "e970bdf2-a968-4be5-b0fe-bc6584e62829",
                                    "roles": [
                                        "EST_CHECKER"
                                    ],
                                    "active": null
                                },
                                {
                                    "auditDetails": null,
                                    "uuid": "1a6d9f29-893d-49d9-870f-6e007a6820e8",
                                    "tenantId": "pb",
                                    "currentState": "67d17040-0c49-40a1-b932-a7b5a5266557",
                                    "action": "REJECT",
                                    "nextState": "af66155b-f5ac-447f-947b-f56539c4d671",
                                    "roles": [
                                        "EST_CHECKER"
                                    ],
                                    "active": null
                                }
                            ]
                        },
                        "comment": "string",
                        "documents": null,
                        "assigner": {
                            "id": 109,
                            "userName": "Nipsyyyy",
                            "name": "Nipun ",
                            "type": "EMPLOYEE",
                            "mobileNumber": "9667076655",
                            "emailId": "",
                            "roles": [
                                {
                                    "id": null,
                                    "name": "Employee",
                                    "code": "EMPLOYEE",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "EST CREATOR",
                                    "code": "EST_CREATOR",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "EST_CHECKER",
                                    "code": "EST_CHECKER",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "LOI APPROVER",
                                    "code": "LOI_APPROVER",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "EST TECH SANC",
                                    "code": "EST_TECH_SANC",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "EST FIN SANC",
                                    "code": "EST_FIN_SANC",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "LOI CREATOR",
                                    "code": "LOI_CREATOR",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "SUPER USER",
                                    "code": "SUPERUSER",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "EST TECH SANC",
                                    "code": "EST_ADMIN_SANC",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "LOI CHECKER",
                                    "code": "LOI_CHECKER",
                                    "tenantId": "pb.amritsar"
                                },
                                {
                                    "id": null,
                                    "name": "HRMS Admin",
                                    "code": "HRMS_ADMIN",
                                    "tenantId": "pb.amritsar"
                                }
                            ],
                            "tenantId": "pb.amritsar",
                            "uuid": "be99b2c2-5780-4b1c-8e41-e3f8a972ebda"
                        },
                        "assignes": [
                            {
                                "id": 109,
                                "userName": "Nipsyyyy",
                                "name": "Nipun ",
                                "type": "EMPLOYEE",
                                "mobileNumber": "9667076655",
                                "emailId": "",
                                "roles": [
                                    {
                                        "id": null,
                                        "name": "Employee",
                                        "code": "EMPLOYEE",
                                        "tenantId": "pb.amritsar"
                                    },
                                    {
                                        "id": null,
                                        "name": "EST CREATOR",
                                        "code": "EST_CREATOR",
                                        "tenantId": "pb.amritsar"
                                    },
                                    {
                                        "id": null,
                                        "name": "EST_CHECKER",
                                        "code": "EST_CHECKER",
                                        "tenantId": "pb.amritsar"
                                    },
                                    {
                                        "id": null,
                                        "name": "LOI APPROVER",
                                        "code": "LOI_APPROVER",
                                        "tenantId": "pb.amritsar"
                                    },
                                    {
                                        "id": null,
                                        "name": "EST TECH SANC",
                                        "code": "EST_TECH_SANC",
                                        "tenantId": "pb.amritsar"
                                    },
                                    {
                                        "id": null,
                                        "name": "EST FIN SANC",
                                        "code": "EST_FIN_SANC",
                                        "tenantId": "pb.amritsar"
                                    },
                                    {
                                        "id": null,
                                        "name": "LOI CREATOR",
                                        "code": "LOI_CREATOR",
                                        "tenantId": "pb.amritsar"
                                    },
                                    {
                                        "id": null,
                                        "name": "SUPER USER",
                                        "code": "SUPERUSER",
                                        "tenantId": "pb.amritsar"
                                    },
                                    {
                                        "id": null,
                                        "name": "EST TECH SANC",
                                        "code": "EST_ADMIN_SANC",
                                        "tenantId": "pb.amritsar"
                                    },
                                    {
                                        "id": null,
                                        "name": "LOI CHECKER",
                                        "code": "LOI_CHECKER",
                                        "tenantId": "pb.amritsar"
                                    },
                                    {
                                        "id": null,
                                        "name": "HRMS Admin",
                                        "code": "HRMS_ADMIN",
                                        "tenantId": "pb.amritsar"
                                    }
                                ],
                                "tenantId": "pb.amritsar",
                                "uuid": "be99b2c2-5780-4b1c-8e41-e3f8a972ebda"
                            }
                        ],
                        "nextActions": [
                            {
                                "auditDetails": null,
                                "uuid": "568b7e7d-d88f-4079-bb02-3dc9a37c56ea",
                                "tenantId": "pb",
                                "currentState": "67d17040-0c49-40a1-b932-a7b5a5266557",
                                "action": "CHECK",
                                "nextState": "e970bdf2-a968-4be5-b0fe-bc6584e62829",
                                "roles": [
                                    "EST_CHECKER"
                                ],
                                "active": null
                            },
                            {
                                "auditDetails": null,
                                "uuid": "1a6d9f29-893d-49d9-870f-6e007a6820e8",
                                "tenantId": "pb",
                                "currentState": "67d17040-0c49-40a1-b932-a7b5a5266557",
                                "action": "REJECT",
                                "nextState": "af66155b-f5ac-447f-947b-f56539c4d671",
                                "roles": [
                                    "EST_CHECKER"
                                ],
                                "active": null
                            }
                        ],
                        "stateSla": null,
                        "businesssServiceSla": -793324953,
                        "previousStatus": null,
                        "entity": null,
                        "auditDetails": {
                            "createdBy": "be99b2c2-5780-4b1c-8e41-e3f8a972ebda",
                            "lastModifiedBy": "be99b2c2-5780-4b1c-8e41-e3f8a972ebda",
                            "createdTime": 1670920740971,
                            "lastModifiedTime": 1670920740971
                        },
                        "rating": 0,
                        "escalated": false
                    }
                ],
                "totalCount": 0
            }
        }
        return result
    },
    viewProjectClosureScreenBills:(tenantId)=> {
        const result = {
            "applicationDetails": [
                {
                    "title": " ",
                    "asSectionHeader": true,
                    "values": [
                        {
                            "title": "WORKS_CREATED_BY",
                            "value": "EP/2022-23/12/000174"
                        },
                        {
                            "title": "WORKS_LOI_STATUS",
                            "value": "Checked"
                        },
                        {
                            "title": "WORKS_LABOUR_COST",
                            "value": "Checked"
                        },
                        {
                            "title": "WORKS_MATERIAL_COST",
                            "value": "Checked"
                        },
                        {
                            "title": "WORKS_COMMISSION",
                            "value": "Checked"
                        },
                        {
                            "title": "MODULE_CSS",
                            "value": "Checked"
                        },
                        {
                            "title": "WORKS_ROYALTY",
                            "value": "Checked"
                        },
                        {
                            "title": "OTHERS",
                            "value": "Checked"
                        },
                        {
                            "title": "RT_TOTAL",
                            "value": "Checked"
                        }
                    ]
                },
            ],
            workflowDetails : {
                isLoading: false,
                error: null,
                isError: false,
                breakLineRequired: false,
                data: {
                    nextActions: [
                        {
                            action: "REJECT",
                            roles: "EST_CHECKER,EST_CHECKER",
                        },
                        {
                            action: "APPROVE",
                            roles: "EST_CHECKER,EST_CHECKER",
                        },
                        {
                            action: "EDIT",
                            roles: "EST_CHECKER,EST_CHECKER",
                        },
                    ],
                    actionState: {
                        auditDetails: {
                            createdBy: "7e46e32c-187c-4fb4-9d6b-1ac70fa8f011",
                            lastModifiedBy: "7e46e32c-187c-4fb4-9d6b-1ac70fa8f011",
                            createdTime: 1663568004997,
                            lastModifiedTime: 1663568004997,
                        },
                        uuid: "67d17040-0c49-40a1-b932-a7b5a5266557",
                        tenantId: "pb.amritsar",
                        businessServiceId: "52e2c4e0-f12c-4c75-aef3-1535bc8edac0",
                        sla: null,
                        state: "CREATED",
                        applicationStatus: "CREATED",
                        docUploadRequired: false,
                        isStartState: true,
                        isTerminateState: false,
                        isStateUpdatable: true,
                        actions: [
                            {
                                auditDetails: {
                                    createdBy: "7e46e32c-187c-4fb4-9d6b-1ac70fa8f011",
                                    lastModifiedBy: "7e46e32c-187c-4fb4-9d6b-1ac70fa8f011",
                                    createdTime: 1663568004997,
                                    lastModifiedTime: 1663568004997,
                                },
                                uuid: "1a6d9f29-893d-49d9-870f-6e007a6820e8",
                                tenantId: "pb.amritsar",
                                currentState: "67d17040-0c49-40a1-b932-a7b5a5266557",
                                action: "REJECT",
                                nextState: "af66155b-f5ac-447f-947b-f56539c4d671",
                                roles: ["EST_CHECKER"],
                                active: true,
                            },
                            {
                                auditDetails: {
                                    createdBy: "7e46e32c-187c-4fb4-9d6b-1ac70fa8f011",
                                    lastModifiedBy: "7e46e32c-187c-4fb4-9d6b-1ac70fa8f011",
                                    createdTime: 1663568004997,
                                    lastModifiedTime: 1663568004997,
                                },
                                uuid: "1a6d9f29-893d-49d9-870f-6e007a6820e8",
                                tenantId: "pb.amritsar",
                                currentState: "67d17040-0c49-40a1-b932-a7b5a5266557",
                                action: "APPROVE",
                                nextState: "af66155b-f5ac-447f-947b-f56539c4d671",
                                roles: ["EST_CHECKER"],
                                active: true,
                            },
                        ],
                        nextActions: [
                            {
                                auditDetails: {
                                    createdBy: "7e46e32c-187c-4fb4-9d6b-1ac70fa8f011",
                                    lastModifiedBy: "7e46e32c-187c-4fb4-9d6b-1ac70fa8f011",
                                    createdTime: 1663568004997,
                                    lastModifiedTime: 1663568004997,
                                },
                                uuid: "af66155b-f5ac-447f-947b-f56539c4d671",
                                tenantId: "pb.amritsar",
                                businessServiceId: "52e2c4e0-f12c-4c75-aef3-1535bc8edac0",
                                sla: null,
                                state: "REJECTED",
                                applicationStatus: "ATTENDANCE_REJECTED",
                                docUploadRequired: false,
                                isStartState: false,
                                isTerminateState: false,
                                isStateUpdatable: true,
                                actions: [
                                    {
                                        auditDetails: {
                                            createdBy: "7e46e32c-187c-4fb4-9d6b-1ac70fa8f011",
                                            lastModifiedBy: "7e46e32c-187c-4fb4-9d6b-1ac70fa8f011",
                                            createdTime: 1663568004997,
                                            lastModifiedTime: 1663568004997,
                                        },
                                        uuid: "0250f409-7e07-464d-926e-97ac72457d72",
                                        tenantId: "pb.amritsar",
                                        currentState: "af66155b-f5ac-447f-947b-f56539c4d671",
                                        action: "EDIT",
                                        nextState: "67d17040-0c49-40a1-b932-a7b5a5266557",
                                        roles: ["EST_CREATOR"],
                                        active: true,
                                    },
                                ],
                                assigneeRoles: ["EST_CREATOR"],
                                action: "REJECT",
                                roles: ["EST_CHECKER"],
                            },
                            {
                                auditDetails: {
                                    createdBy: "7e46e32c-187c-4fb4-9d6b-1ac70fa8f011",
                                    lastModifiedBy: "7e46e32c-187c-4fb4-9d6b-1ac70fa8f011",
                                    createdTime: 1663568004997,
                                    lastModifiedTime: 1663568004997,
                                },
                                uuid: "af66155b-f5ac-447f-947b-f56539c4d671",
                                tenantId: "pb.amritsar",
                                businessServiceId: "52e2c4e0-f12c-4c75-aef3-1535bc8edac0",
                                sla: null,
                                state: "APPROVE",
                                applicationStatus: "ATTENDANCE_APPROVE",
                                docUploadRequired: false,
                                isStartState: false,
                                isTerminateState: false,
                                isStateUpdatable: true,
                                actions: [
                                    {
                                        auditDetails: {
                                            createdBy: "7e46e32c-187c-4fb4-9d6b-1ac70fa8f011",
                                            lastModifiedBy: "7e46e32c-187c-4fb4-9d6b-1ac70fa8f011",
                                            createdTime: 1663568004997,
                                            lastModifiedTime: 1663568004997,
                                        },
                                        uuid: "0250f409-7e07-464d-926e-97ac72457d72",
                                        tenantId: "pb.amritsar",
                                        currentState: "af66155b-f5ac-447f-947b-f56539c4d671",
                                        action: "EDIT",
                                        nextState: "67d17040-0c49-40a1-b932-a7b5a5266557",
                                        roles: ["EST_CREATOR"],
                                        active: true,
                                    },
                                ],
                                assigneeRoles: ["EST_CREATOR"],
                                action: "APPROVE",
                                roles: ["EST_CHECKER"],
                            },
                        ],
                        roles: ["EST_CHECKER", "EST_CHECKER"],
                    },
                    applicationBusinessService: "estimate-approval-2",
                    processInstances: [
                        {
                            id: "e3f890d0-bcb0-4526-afde-1d36d2be91f4",
                            tenantId: "pb.amritsar",
                            businessService: "estimate-approval-2",
                            businessId: "EP/2022-23/11/000160",
                            action: "CREATE",
                            moduleName: "estimate-service",
                            state: {
                                auditDetails: null,
                                uuid: "67d17040-0c49-40a1-b932-a7b5a5266557",
                                tenantId: "pb",
                                businessServiceId: "52e2c4e0-f12c-4c75-aef3-1535bc8edac0",
                                sla: null,
                                state: "CREATED",
                                applicationStatus: "CREATED",
                                docUploadRequired: false,
                                isStartState: true,
                                isTerminateState: false,
                                isStateUpdatable: null,
                                actions: [
                                    {
                                        auditDetails: null,
                                        uuid: "568b7e7d-d88f-4079-bb02-3dc9a37c56ea",
                                        tenantId: "pb",
                                        currentState: "67d17040-0c49-40a1-b932-a7b5a5266557",
                                        action: "APPROVE",
                                        nextState: "e970bdf2-a968-4be5-b0fe-bc6584e62829",
                                        roles: ["EST_CHECKER"],
                                        active: null,
                                    },
                                    {
                                        auditDetails: null,
                                        uuid: "1a6d9f29-893d-49d9-870f-6e007a6820e8",
                                        tenantId: "pb",
                                        currentState: "67d17040-0c49-40a1-b932-a7b5a5266557",
                                        action: "REJECT",
                                        nextState: "af66155b-f5ac-447f-947b-f56539c4d671",
                                        roles: ["EST_CHECKER"],
                                        active: null,
                                    },
                                ],
                            },
                            comment: "",
                            documents: null,
                            assigner: {
                                id: 109,
                                userName: "Nipsyyyy",
                                name: "Nipun ",
                                type: "EMPLOYEE",
                                mobileNumber: "9667076655",
                                emailId: "",
                                roles: [
                                    {
                                        id: null,
                                        name: "Employee",
                                        code: "EMPLOYEE",
                                        tenantId: "pb.amritsar",
                                    },
                                    {
                                        id: null,
                                        name: "EST CREATOR",
                                        code: "EST_CREATOR",
                                        tenantId: "pb.amritsar",
                                    },
                                    {
                                        id: null,
                                        name: "EST_CHECKER",
                                        code: "EST_CHECKER",
                                        tenantId: "pb.amritsar",
                                    },
                                    {
                                        id: null,
                                        name: "LOI APPROVER",
                                        code: "LOI_APPROVER",
                                        tenantId: "pb.amritsar",
                                    },
                                    {
                                        id: null,
                                        name: "EST TECH SANC",
                                        code: "EST_TECH_SANC",
                                        tenantId: "pb.amritsar",
                                    },
                                    {
                                        id: null,
                                        name: "EST FIN SANC",
                                        code: "EST_FIN_SANC",
                                        tenantId: "pb.amritsar",
                                    },
                                    {
                                        id: null,
                                        name: "LOI CREATOR",
                                        code: "LOI_CREATOR",
                                        tenantId: "pb.amritsar",
                                    },
                                    {
                                        id: null,
                                        name: "SUPER USER",
                                        code: "SUPERUSER",
                                        tenantId: "pb.amritsar",
                                    },
                                    {
                                        id: null,
                                        name: "EST TECH SANC",
                                        code: "EST_ADMIN_SANC",
                                        tenantId: "pb.amritsar",
                                    },
                                    {
                                        id: null,
                                        name: "LOI CHECKER",
                                        code: "LOI_CHECKER",
                                        tenantId: "pb.amritsar",
                                    },
                                    {
                                        id: null,
                                        name: "HRMS Admin",
                                        code: "HRMS_ADMIN",
                                        tenantId: "pb.amritsar",
                                    },
                                ],
                                tenantId: "pb.amritsar",
                                uuid: "be99b2c2-5780-4b1c-8e41-e3f8a972ebda",
                            },
                            assignes: [
                                {
                                    id: 111,
                                    userName: "EMP-107-000011",
                                    name: "Estimate Checker",
                                    type: "EMPLOYEE",
                                    mobileNumber: "8877665544",
                                    emailId: null,
                                    roles: [
                                        {
                                            id: null,
                                            name: "EST_CHECKER",
                                            code: "EST_CHECKER",
                                            tenantId: "pb.amritsar",
                                        },
                                        {
                                            id: null,
                                            name: "Employee",
                                            code: "EMPLOYEE",
                                            tenantId: "pb.amritsar",
                                        },
                                    ],
                                    tenantId: "pb.amritsar",
                                    uuid: "88bd1b70-dd6d-45f7-bcf7-5aa7a6fae7d9",
                                },
                            ],
                            nextActions: [
                                {
                                    auditDetails: null,
                                    uuid: "568b7e7d-d88f-4079-bb02-3dc9a37c56ea",
                                    tenantId: "pb",
                                    currentState: "67d17040-0c49-40a1-b932-a7b5a5266557",
                                    action: "APPROVE",
                                    nextState: "e970bdf2-a968-4be5-b0fe-bc6584e62829",
                                    roles: ["EST_CHECKER"],
                                    active: null,
                                },
                                {
                                    auditDetails: null,
                                    uuid: "1a6d9f29-893d-49d9-870f-6e007a6820e8",
                                    tenantId: "pb",
                                    currentState: "67d17040-0c49-40a1-b932-a7b5a5266557",
                                    action: "REJECT",
                                    nextState: "af66155b-f5ac-447f-947b-f56539c4d671",
                                    roles: ["EST_CHECKER"],
                                    active: null,
                                },
                            ],
                            stateSla: null,
                            businesssServiceSla: 431977852,
                            previousStatus: null,
                            entity: null,
                            auditDetails: {
                                createdBy: "be99b2c2-5780-4b1c-8e41-e3f8a972ebda",
                                lastModifiedBy: "be99b2c2-5780-4b1c-8e41-e3f8a972ebda",
                                createdTime: 1669175470551,
                                lastModifiedTime: 1669175470551,
                            },
                            rating: 0,
                            escalated: false,
                        },
                    ],
                },
            },
            CollapseConfig: {
                collapseAll: true,
                groupHeader: "",
                headerLabel: "Bill 1- ID(CTR/2022-23/08/0004) - Type(Work Order)- Date(20-09-2022)",
                headerValue: "₹ 19,08,500"
            },
        }
        return result;
    },
    getValueHelper:(questionType)=>{
        switch (questionType) {
            case "MULTIPLE_ANSWER_TYPE":
                return "A.  Yes"
            case "DATE_ANSWER_TYPE":
                return "A.  20/11/1992"
            case "UPLOAD_ANSWER_TYPE":
                return "A.  Yes"
            default:
                return ""
        }
    },
    viewProjectClosureScreenFieldSurvey:(tenantId,questions,t) => {
        const tenant = Digit.ULBService.getStateId();
        const applicationDetails = [
            {
                "title": " ",
                "asSectionHeader": true,
                "tab":"fieldSurvey",
                "values":questions.map((question,index)=> {
                    return {
                        "title": `${index+1}.  ${t(question.code)}`,
                        "value": WorksSearch.getValueHelper(question.type),
                        "isImages": question.type ==="UPLOAD_ANSWER_TYPE" ? true : false,
                        "fileStoreIds": ["0db89173-6621-455e-b953-2286936040be"],
                        tenant
                    }
                }) 
            }
        ]

        const result = {
            applicationDetails
        }
        return result
        
    },
    viewProjectClosureScreenClosureChecklist: (tenantId,questions,t) => {
        
        const applicationDetails = [
            {
                "title": " ",
                "asSectionHeader": true,
                "tab": "fieldSurvey",
                "values": questions.map((question, index) => {
                    return {
                        "title": `${index + 1}.  ${t(question.code)}`,
                        "value": WorksSearch.getValueHelper(question.type)
                    }
                })
            }
        ]

        const result = {
            applicationDetails
        }
        return result
    },
}