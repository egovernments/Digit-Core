var config = require("./config");
var axios = require("axios").default;
var url = require("url");
var producer = require("./producer").producer;
var logger = require("./logger").logger;
const { Pool } = require('pg');
const get = require('lodash/get');
var FormData = require("form-data");
const uuidv4 = require("uuid/v4");

const pool = new Pool({
  user: config.DB_USER,
  host: config.DB_HOST,
  database: config.DB_NAME,
  password: config.DB_PASSWORD,
  port: config.DB_PORT,
});

auth_token = config.auth_token;

async function search_projectDetails(tenantId, requestinfo, projectId) {
  var params = {
    tenantId: tenantId,
    limit: 1,
    offset: 0
  };

  var searchEndpoint = config.paths.projectDetails_search;
  var data = {
    "Projects": [{
      "tenantId": tenantId,
      "projectNumber": projectId
    }]
  }
  return await axios({
    method: "post",
    url: url.resolve(config.host.projectDetails, searchEndpoint),
    data: Object.assign(requestinfo, data),
    params,
  });
}
async function search_musterRoll(tenantId, requestinfo, musterRollNumber) {
  var params = {
    tenantId: tenantId,
    musterRollNumber: musterRollNumber
  };

  var searchEndpoint = config.paths.musterRoll_search;

  return await axios({
    method: "post",
    url: url.resolve(config.host.musterRoll, searchEndpoint),
    data: Object.assign(requestinfo),
    params,
  });
}
async function search_contract(tenantId, requestinfo, contractId) {
  var params = {
    tenantId: tenantId,
    contractNumber: contractId
  };

  var searchEndpoint = config.paths.contract_search;

  return await axios({
    method: "post",
    url: url.resolve(config.host.contract, searchEndpoint),
    data: Object.assign(requestinfo, params),

  });
}
async function search_organisation(tenantId, requestinfo, orgId) {

  var params = {
    limit: 10,
    offset: 0
  };
  var data = {
    "apiOperation": "SEARCH",
    "SearchCriteria": {
      "tenantId": tenantId,
      "id": [orgId]
    }
  }

  var searchEndpoint = config.paths.orgnisation_search;

  return await axios({
    method: "post",
    url: url.resolve(config.host.organisation, searchEndpoint),
    data: Object.assign(requestinfo, data),
    params,

  });
}

async function search_mdmsLabourCharges(tenantId, requestinfo) {
  var params = {
    tenantId: tenantId.split(".")[0],
    moduleName: "expense",
    masterName: "LabourCharges",
  };

  var searchEndpoint = config.paths.mdms_get;

  return await axios({
    method: "post",
    url: url.resolve(config.host.mdms, searchEndpoint),
    data: Object.assign(requestinfo),
    params

  });
}

async function searchEstimateFormusterRoll(requestinfo) {
  var params = {  
  limit: 1,
    _offset: 0,
    get offset() {
      return this._offset;
    },
    set offset(value) {
      this._offset = value;
    },
  };

  var searchEndpoint = config.paths.expense_calculator_estimate;
  return await axios({
    method: "post",
    url: url.resolve(config.host.expense_calculator, searchEndpoint),
    data: Object.assign(requestinfo),
    params,
  });
}

async function search_estimateDetails(tenantId, requestinfo, estimateNumber) {
  var params = {
    tenantId: tenantId,
    estimateNumber: estimateNumber,
    limit: 100,
    _offset: 0,
    get offset() {
      return this._offset;
    },
    set offset(value) {
      this._offset = value;
    },
  };

  var searchEndpoint = config.paths.estimate_search;
  return await axios({
    method: "post",
    url: url.resolve(config.host.estimates, searchEndpoint),
    data: Object.assign(requestinfo),
    params,
  });
}

async function search_user(uuid, tenantId, requestinfo) {
  return await axios({
    method: "post",
    url: url.resolve(config.host.user, config.paths.user_search),
    data: {
      RequestInfo: requestinfo.RequestInfo,
      uuid: [uuid],
      tenantId: tenantId,
    },
  });
}

async function search_case(cnrNumber1, tenantId1, requestinfo) {
  return await axios({
    method: "post",
    url: url.resolve(config.host.case, config.paths.case_search),
    data: {
      "RequestInfo": requestinfo.RequestInfo,
      "tenantId": tenantId1,
      "criteria": [
		{
      "cnrNumber": cnrNumber1,
		}
	]
    },
  });
}

async function search_order(tenantId1, cnrNumber1, orderId1, requestinfo) {
  return await axios({
    method: "post",
    url: url.resolve(config.host.order, config.paths.order_search),
    data: {
      "RequestInfo": requestinfo.RequestInfo,
      "tenantId": tenantId1,
      "criteria": {
	      "tenantId": tenantId1,
        "cnrNumber": cnrNumber1,
        "id": orderId1
	    }
    },
  });
}

async function search_hearing(tenantId, cnrNumber, requestinfo) {
  return await axios({
    method: "post",
    url: url.resolve(config.host.hearing, config.paths.hearing_search),
    data: {
      "RequestInfo": requestinfo.RequestInfo,
      "criteria": {
	      "tenantId": tenantId
        // "cnrNumber": cnrNumber
	    },
      "pagination": {
        "limit": 10,
        "offset": 0,
        "sortBy": "createdTime",
        "order": "desc"
      }
    },
  });
}


async function search_mdms_order(uniqueIdentifier, schemaCode, tenantID,requestInfo) {
  return await axios({
    method: "post",
    url: url.resolve(config.host.mdms, config.paths.mdms_search),
    data: {
      RequestInfo: requestInfo.RequestInfo,
      MdmsCriteria: {
        tenantId: tenantID,
        schemaCode: schemaCode,
        uniqueIdentifiers: [uniqueIdentifier]
    }
    },
  });
}

async function search_hrms(tenantId, employeeTypes, courtRooms, requestinfo) {
  var params = {
    tenantId: tenantId,
    employeetypes: employeeTypes,
    courtrooms: courtRooms,
    limit:10,
    offset:0,
  };
  return await axios({
    method: "post",
    url: url.resolve(config.host.hrms, config.paths.hrms_search),
    data: {
      RequestInfo: requestinfo.RequestInfo
    },
    params,
  });
}

async function search_individual(tenantId, individualId, requestinfo) {
  var params = {
    tenantId: tenantId,
    limit:10,
    offset:0,
  };
  return await axios({
    method: "post",
    url: url.resolve(config.host.individual, config.paths.individual_search),
    data: {
      RequestInfo: requestinfo.RequestInfo,
      Individual: {
        individualId: individualId
      }
    },
    params,
  });
}

async function search_sunbirdrc_credential_service(tenantId, code, uuid, requestinfo) {
  return await axios({
    method: "post",
    url: url.resolve(config.host.sunbirdrc_credential_service, config.paths.sunbirdrc_credential_service_search),
    data: {
      RequestInfo: requestinfo.RequestInfo,
      tenantId: tenantId,
      code: code,
      uuid: uuid
    },
  });
}


async function search_workflow(applicationNumber, tenantId, requestinfo) {
  var params = {
    tenantId: tenantId,
    businessIds: applicationNumber,
  };
  return await axios({
    method: "post",
    url: url.resolve(config.host.workflow, config.paths.workflow_search),
    data: requestinfo,
    params,
  });
}

async function search_mdms(request) {
  return await axios({
    method: "post",
    url: url.resolve(config.host.mdms, config.paths.mdms_search),
    data: request
  });
}

async function search_localization(request, lang, module, tenantId) {
  return await axios({
    method: "post",
    url: url.resolve(config.host.localization, config.paths.localization_search),
    data: request,
    params: {
      "locale": lang,
      "module": module,
      "tenantId": tenantId.split(".")[0]
    }
  });
}

async function create_pdf(tenantId, key, data, requestinfo) {
  var oj = Object.assign(requestinfo, data);
  return await axios({
    responseType: "stream",
    method: "post",
    url: url.resolve(config.host.pdf, config.paths.pdf_create),
    data: Object.assign(requestinfo, data),
    params: {
      tenantId: tenantId,
      key: key,
    },
  });
}

async function create_pdf_and_upload(tenantId, key, data, requestinfo) {
  return await axios({
    //responseType: "stream",
    method: "post",
    url: url.resolve(config.host.pdf, config.paths.pdf_create_upload),
    data: Object.assign(requestinfo, data),
    params: {
      tenantId: tenantId,
      key: key,
    },
  });
}

function checkIfCitizen(requestinfo) {
  if (requestinfo.RequestInfo.userInfo.type == "CITIZEN") {
    return true;
  } else {
    return false;
  }
}

function search_expense_bill(request, limit, offset) {
  return new Promise((resolve, reject) => {
    let newRequest = JSON.parse(JSON.stringify(request))
    newRequest["pagination"] = { limit, offset }
    let promise = new axios({
      method: "POST",
      url: url.resolve(config.host.expense, config.paths.expense_bill_search),
      data: newRequest,
    });
    promise.then((data) => {
      resolve(data.data)
    }).catch((err) => reject(err))
  })
}

function search_expense_calculator_bill(request, limit, offset) {
  return new Promise((resolve, reject) => {
    let newRequest = JSON.parse(JSON.stringify(request))
    newRequest["pagination"] = { limit, offset }
    let promise = new axios({
      method: "POST",
      url: url.resolve(config.host.expense_calculator, config.paths.expense_calculator_search),
      data: newRequest,
    });
    promise.then((data) => {
      resolve(data.data)
    }).catch((err) => reject(err))
  })
}

function search_bank_account_details(request) {
  return new Promise((resolve, reject) => {
    let newRequest = JSON.parse(JSON.stringify(request))
    let promise = new axios({
      method: "POST",
      url: url.resolve(config.host.bankaccount, config.paths.bankaccount_search),
      data: newRequest,
    });
    promise.then((data) => {
      resolve(data.data)
    }).catch((err) => reject(err))
  })
}

function search_payment_details(request) {
  return new Promise((resolve, reject) => {
    let newRequest = JSON.parse(JSON.stringify(request))
    let promise = new axios({
      method: "POST",
      url: url.resolve(config.host.expense, config.paths.expense_payment_search),
      data: newRequest,
    });
    promise.then((data) => {
      resolve(data.data)
    }).catch((err) => reject(err))
  })
}

/**
 *
 * @param {*} filename -name of localy stored temporary file
 * @param {*} tenantId - tenantID
 */
async function upload_file_using_filestore(filename, tenantId, fileData) {
  try {
    var url = `${config.host.filestore}/filestore/v1/files?tenantId=${tenantId}&module=billgen&tag=works-billgen`;
    var form = new FormData();
    form.append("file", fileData, {
      filename: filename,
      contentType: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    });
    let response = await axios.post(url, form, {
      maxContentLength: Infinity,
      maxBodyLength: Infinity,
      headers: {
        ...form.getHeaders()
      }
    });
    return get(response.data, "files[0].fileStoreId");
  } catch (error) {
    console.log(error);
    throw(error)
  }
};

async function create_eg_payments_excel(paymentId, paymentNumber, tenantId, userId) {
  try {
    var id = uuidv4();
    const insertQuery = 'INSERT INTO eg_payments_excel(id, paymentid, paymentnumber, tenantId, status, numberofbills, numberofbeneficialy, totalamount, filestoreid, createdby, lastmodifiedby, createdtime, lastmodifiedtime) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13)';
    const status = 'INPROGRESS';
    const curentTimeStamp = new Date().getTime();
    await pool.query(insertQuery, [id, paymentId, paymentNumber, tenantId, status, 0, 0, 0, null, userId, userId, curentTimeStamp, curentTimeStamp]);
  } catch (error) {
    throw(error)
  }
}

async function reset_eg_payments_excel(paymentId, userId) {
  try {
    const status = 'INPROGRESS';
    const updateQuery = 'UPDATE eg_payments_excel SET status =  $1, numberofbills = $2, numberofbeneficialy = $3, totalamount = $4, filestoreid = $5, lastmodifiedby = $6, lastmodifiedtime = $7 WHERE paymentid = $8';
    const curentTimeStamp = new Date().getTime();
    await pool.query(updateQuery,[status, 0, 0, 0, null, userId, curentTimeStamp, paymentId]);
    return;
  } catch (error) {
    throw(error)
  }
}

async function exec_query_eg_payments_excel(query, queryParams) {
  try {
    return pool.query(query, queryParams);
  } catch (error) {
    throw(error)
  }
}


/**
 * It generates bill of property tax and merge into single PDF file
 * @param {*} kafkaData - Data pushed in kafka topic
 */
async function create_bulk_pdf_pt(kafkaData) {
  var propertyBills;
  var consolidatedResult = { Bill: [] };

  let {
    tenantId,
    locality,
    bussinessService,
    isConsolidated,
    consumerCode,
    requestinfo,
    jobid
  } = kafkaData;

  try {

    try {
      var searchCriteria = { locality, tenantId, bussinessService };
      propertyBills = await search_bill(
        null,
        null,
        {
          RequestInfo: requestinfo.RequestInfo,
          searchCriteria
        }
      );

      propertyBills = propertyBills.data.Bills;

      if (propertyBills.length > 0) {
        for (let propertyBill of propertyBills) {
          if (propertyBill.status === 'EXPIRED') {
            var billresponse = await fetch_bill(
              tenantId, propertyBill.consumerCode, propertyBill.businessService, { RequestInfo: requestinfo.RequestInfo }
            );
            if (billresponse?.data?.Bill?.[0]) consolidatedResult.Bill.push(billresponse.data.Bill[0]);
          }
          else {
            if (propertyBill.status === 'ACTIVE')
              consolidatedResult.Bill.push(propertyBill);
          }
        }
      }
    }
    catch (ex) {
      if (ex.response && ex.response.data) logger.error(ex.response.data);
      throw new Error("Failed to query details of property ");
    }

    if (consolidatedResult?.Bill?.length > 0) {
      var pdfResponse;
      var pdfkey = config.pdf.ptbill_pdf_template;
      try {
        var batchSize = config.PDF_BATCH_SIZE;
        var size = consolidatedResult.Bill.length;
        var numberOfFiles = (size % batchSize) == 0 ? (size / batchSize) : (~~(size / batchSize) + 1);
        for (var i = 0; i < size; i += batchSize) {
          var payloads = [];
          var billData = consolidatedResult.Bill.slice(i, i + batchSize);
          var billArray = {
            Bill: billData,
            isBulkPdf: true,
            pdfJobId: jobid,
            pdfKey: pdfkey,
            totalPdfRecords: size,
            currentPdfRecords: billData.length,
            tenantId: tenantId,
            numberOfFiles: numberOfFiles,
            locality: locality,
            service: bussinessService,
            isConsolidated: isConsolidated,
            consumerCode: consumerCode
          };
          var pdfData = Object.assign({ RequestInfo: requestinfo.RequestInfo }, billArray)
          payloads.push({
            topic: config.KAFKA_RECEIVE_CREATE_JOB_TOPIC,
            messages: JSON.stringify(pdfData)
          });
          producer.send(payloads, function (err, data) {
            if (err) {
              logger.error(err.stack || err);
              errorCallback({
                message: `error while publishing to kafka: ${err.message}`
              });
            } else {
              logger.info("jobid: " + jobid + ": published to kafka successfully");
            }
          });

        }

        try {
          const result = await pool.query('select * from egov_bulk_pdf_info where jobid = $1', [jobid]);
          if (result.rowCount >= 1) {
            const updateQuery = 'UPDATE egov_bulk_pdf_info SET totalrecords = $1 WHERE jobid = $2';
            await pool.query(updateQuery, [size, jobid]);
          }
        } catch (err) {
          logger.error(err.stack || err);
        }
      } catch (ex) {
        let errorMessage = "Failed to generate PDF";
        if (ex.response && ex.response.data) logger.error(ex.response.data);
        throw new Error(errorMessage);
      }
    } else {
      throw new Error("There is no billfound for the criteria");
    }

  } catch (ex) {
    throw new Error("Failed to query bill for property application");
  }

}

async function search_measurementBookDetails(tenantId, requestinfo,contractNumber, measurementBookNumber) {

  const searchEndpoint = config.paths.measurement_book_search;
  const data = {
    "contractNumber": contractNumber,
    "measurementNumber": measurementBookNumber,
    "tenantId": tenantId
  }
  return await axios({
    method: "post",
    url: url.resolve(config.host.measurements, searchEndpoint),
    data: Object.assign(requestinfo, data)
  });
}

module.exports = {
  pool,
  create_pdf,
  create_pdf_and_upload,
  search_mdms,
  search_hrms,
  search_user,
  search_workflow,
  search_projectDetails,
  search_estimateDetails,
  search_musterRoll,
  search_contract,
  search_mdms,
  search_localization,
  search_mdmsLabourCharges,
  search_organisation,
  search_expense_bill,
  search_expense_calculator_bill,
  search_payment_details,
  search_bank_account_details,
  searchEstimateFormusterRoll,
  upload_file_using_filestore,
  create_eg_payments_excel,
  reset_eg_payments_excel,
  exec_query_eg_payments_excel,
  search_measurementBookDetails,
  search_case,
  search_order,
  search_mdms_order,
  search_individual,
  search_hearing,
  search_sunbirdrc_credential_service
};
