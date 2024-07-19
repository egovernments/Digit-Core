var express = require("express");
var router = express.Router();
var config = require("../config");
const { asyncMiddleware } = require("../utils/asyncMiddleware");
var logger = require("../logger").logger;
const { pool, search_payment_details, exec_query_eg_payments_excel, create_eg_payments_excel, reset_eg_payments_excel } = require("../api");
var producer = require("../producer").producer;

function renderError(res, errorMessage, errorCode) {
  if (errorCode == undefined) errorCode = 500;
  res.status(errorCode).send({ errorMessage });
}

router.post(
  "/_generate",
  asyncMiddleware(async function (req, res, next) {
    /**
     * TODO: integration with payment api is left, in query only payment id will be send, using payment id will fetch billids and then will trigger the request
     */
    var tenantId = req.query.tenantId;
    var requestinfo = req.body?.RequestInfo;
    var criteria = req.body?.Criteria;

    if (requestinfo == undefined) {
      return renderError(res, "Requestinfo can not be null");
    }
    if (criteria == undefined) {
      return renderError(res, "Criteria can not be null");
    }
    if (!tenantId) {
      return renderError(
        res,
        "TenantId are mandatory to generate the group bill"
      );
    }
    let paymentId = criteria?.paymentId;
    let paymentNumber = null;

    if (!paymentId) {
      return renderError(
        res,
        "paymentId is mandatory to generate the group bill"
      );
    }

    if (!requestinfo?.userInfo?.uuid) {
      return renderError(
        res,
        "uuid of useris missing."
      );
    }


    var kafkaData = {
      RequestInfo: requestinfo,
      tenantId: tenantId,
      paymentId: paymentId
    };

    try {
      let paymentRequest = {};
      paymentRequest['RequestInfo'] = requestinfo;
      paymentRequest.paymentCriteria = {
        "tenantId": tenantId,
        "ids": [paymentId]
      };
      let paymentResp = await search_payment_details(paymentRequest);
      if (!paymentResp?.payments?.length) {
        return renderError(res, `Failed to query for payment search. Check the Payment id.`);
      } else {
        paymentNumber = paymentResp?.payments[0]?.paymentNumber;
      }
    } catch (error) {
      logger.error(error.stack || err);
      return renderError(res, `Failed to query for payment search.`);
    }


    try {
      var payloads = [];
      payloads.push({
        topic: config.KAFKA_PAYMENT_EXCEL_GEN_TOPIC,
        messages: JSON.stringify(kafkaData)
      });
      producer.send(payloads, function (err, data) {
        if (err) {
          logger.error(err.stack || err);
          errorCallback({
            message: `error while publishing to kafka: ${err.message}`
          });
        } else {
          logger.info("paymentId: " + paymentId + ": published to kafka successfully to generate excel.");
        }
      });

      try {
        const result = await exec_query_eg_payments_excel('select * from eg_payments_excel where paymentid = $1', [paymentId])
        var userId = requestinfo?.userInfo?.uuid;
        if (result.rowCount < 1) {
          await create_eg_payments_excel(paymentId, paymentNumber, tenantId, userId);
        } else {
          await reset_eg_payments_excel(paymentId, userId);
        }
      } catch (err) {
        logger.error(err.stack || err);
      }

      res.status(201);
      res.json({
        ResponseInfo: requestinfo,
        paymentId: paymentId,
        message: `XLSX Bill creation is in process for payment ${paymentId}.`,
      });

    } catch (error) {
      logger.error(error.stack || err);
      return renderError(res, `Failed to query bill generate.`);
    }

  })
);

router.post(
  "/_search",
  async (req, res) => {
    let requestInfo;
    try {
      let tenantId = req.query.tenantId;
      let paymentId = req.query.paymentId;
      let status = req.query.status;
      requestInfo = req.body?.RequestInfo;
      let userId = requestInfo?.userInfo?.uuid;
      let pagination = req.body?.pagination;
      
      if ((userId == undefined || userId.trim() == "") && (paymentId == undefined || paymentId.trim() == "")) {
        res.status(400);
        res.json({
          ResponseInfo: requestInfo,
          message: "paymentId and userId both can not be empty",
        });
      } else {
        delete requestInfo['authToken']
        delete requestInfo['userInfo']
        let {countQuery, searchQuery, countParams, searchParams, defPageObj} = getQuery(paymentId, tenantId, userId, status, pagination);
        let total = 0;
        try {
          let countResp = await exec_query_eg_payments_excel(countQuery, countParams);
          if (countResp && countResp.rows.length > 0) {
            total = parseInt(countResp.rows[0]?.count) || 0;
          }
        } catch (error) {
          logger.error(error.stack || error);
          throw(error)
        }
        // Set total as totalCount
        defPageObj["totalCount"] = total;
        try {
          let searchResp = await exec_query_eg_payments_excel(searchQuery, searchParams);
          if (searchResp && searchResp.rows.length > 0) {
            let parsedResult = parseResult(searchResp)
            res.status(200).send({ ResponseInfo: requestInfo, searchresult:parsedResult , pagination: defPageObj });
          } else {
            logger.error("no result found in DB search");
            res.status(200).send({ ResponseInfo: requestInfo, searchresult: [], pagination: defPageObj });
          }
        } catch (error) {
          logger.error(error.stack || error);
          throw(error)
        }
      }
    } catch (error) {
      logger.error(error.stack || error);
      res.status(400);
      res.json({
        ResponseInfo: requestInfo,
        message: "some unknown error while searching: " + error.message,
      });
    }
  }
);

const getQuery = (paymentId,  tenantId, userId, status, pagination) => {
  let defPageObj = {"limit": 10, "offSet": 0, "sortBy": "createdtime", "order": "DESC"}
  let tableName = "eg_payments_excel"
  let countQuery = `select count(*) from ${tableName} WHERE `;
  let searchQuery = `select * from ${tableName} WHERE `;
  let countParams = []
  let searchParams = []
  var sNext = 1, cNext = 1;
  let prevTrue = false;
  if (paymentId != undefined && paymentId.trim() !== "") {
    searchQuery += ` paymentId = ($${sNext++})`;
    searchParams.push(paymentId);
    countQuery += ` paymentId = ($${cNext++})`;
    countParams.push(paymentId)
    prevTrue = true;
  }

  if (tenantId != undefined && tenantId.trim() !== "") {
    if (prevTrue) {
      searchQuery += ` and `;
      countQuery += ` and `;
    }
    searchQuery += ` tenantId = ($${sNext++})`;
    searchParams.push(tenantId);
    countQuery += ` tenantId = ($${cNext++})`;
    countParams.push(tenantId);
    prevTrue = true;
  }

  if (userId != undefined && userId.trim() !== "") {
    if (prevTrue) {
      searchQuery += " and ";
      countQuery += " and ";
    } 
    searchQuery += ` createdby = ($${sNext++})`;
    countQuery += ` createdby = ($${cNext++})`;
    searchParams.push(userId);
    countParams.push(userId);
    prevTrue = true;
  }

  if (status != undefined && status.trim() !== "") {
    if (prevTrue) {
      searchQuery += " and ";
      countQuery += " and ";
    }
    searchQuery += ` status = ($${sNext++})`;
    searchParams.push(status);
    countQuery += ` status = ($${cNext++})`;
    countParams.push(status)
  }

  if (pagination) {
    if (pagination?.sortBy && pagination?.sortBy.trim() != null) {
      defPageObj.sortBy = pagination.sortBy.trim();
    }
    if (pagination?.order && pagination?.order.trim() != null) {
      if (["ASC", "DESC"].indexOf(pagination.order.trim().toUpperCase()))
      defPageObj.sortBy = pagination.sortBy.trim().toUpperCase();
    }
    if (pagination?.limit && parseInt(pagination.limit) != NaN) {
      pagination.limit = parseInt(pagination.limit)
      if (pagination.limit && pagination.limit <= 100)
        defPageObj.limit = pagination.limit;
    }
    if (pagination?.offSet && parseInt(pagination.offSet) != NaN) {
      defPageObj.offSet = parseInt(pagination.offSet)
    }
  }
  searchQuery += ` ORDER BY ${defPageObj.sortBy} ${defPageObj.order} LIMIT ${defPageObj.limit} offset ${defPageObj.offSet}`;
  return {countQuery, searchQuery, countParams, searchParams, defPageObj};
}

const parseResult = (results) => {
  var searchresult = [];
  if (results && results.rows.length > 0) {
    results.rows.map(crow => {
      searchresult.push({
        "id": crow.id,
        "paymentId": crow.paymentid,
        "paymentNumber": crow.paymentnumber,
        "tenantId": crow.tenantid,
        "status": crow.status,
        "numberofbills": crow.numberofbills,
        "numberofbeneficialy": crow.numberofbeneficialy,
        "totalamount": crow.totalamount,
        "filestoreid": crow.filestoreid,
        "createdby": crow.createdby,
        "lastmodifiedby": crow.lastmodifiedby,
        "createdtime": crow.createdtime,
        "lastmodifiedtime": crow.lastmodifiedtime,
      });
    });
    
  }
  return searchresult;
};
module.exports = router;
