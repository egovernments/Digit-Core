import axios from "axios";
import envVariables from "../EnvironmentVariables";
import get from "lodash/get";
import logger from "../config/logger";
const NodeCache = require("node-cache");
var moment = require("moment-timezone");

const cache = new NodeCache({ stdTTL: 300 });

let datetimezone = envVariables.DATE_TIMEZONE;
let egovLocHost = envVariables.EGOV_LOCALISATION_HOST;
let egovLocSearchCall = envVariables.EGOV_LOCALISATION_SEARCH;
let defaultLocale = envVariables.DEFAULT_LOCALISATION_LOCALE;
let defaultTenant = envVariables.DEFAULT_LOCALISATION_TENANT;
export const getTransformedLocale = (label) => {
  return label.toUpperCase().replace(/[.:-\s\/]/g, "_");
};

/**
 * This function returns localisation label from keys based on needs
 * This function does optimisation to fetch one module localisation values only once
 * @param {*} requestInfo - requestinfo from client
 * @param {*} localisationMap - localisation map containing localisation key,label fetched till now
 * @param {*} prefix - prefix to be added before key before fetching localisation ex:-"MODULE_NAME_MASTER_NAME"
 * @param {*} key - key to fetch localisation
 * @param {*} moduleName - "module name for fetching localisation"
 * @param {*} localisationModuleList - "list of modules for which localisation was already fetched"
 * @param {*} isCategoryRequired - ex:- "GOODS_RETAIL_TST-1" = get localisation for "GOODS"
 * @param {*} isMainTypeRequired  - ex:- "GOODS_RETAIL_TST-1" = get localisation for "RETAIL"
 * @param {*} isSubTypeRequired  - - ex:- "GOODS_RETAIL_TST-1" = get localisation for "GOODS_RETAIL_TST-1"
 */
 export const findLocalisation = async (
  requestInfo,
  moduleList,
  codeList,
  pdfKey
) => {
  let cacheData = null;
  let locale = requestInfo.msgId;
  if (null != locale) {
    locale = locale.split("|");
    locale = locale.length > 1 ? locale[1] : defaultLocale;
  } else {
    locale = defaultLocale;
  }

  if(pdfKey!=null){
    let cacheKey = pdfKey + '-' + locale;
    cacheData = await verifyCache(cacheKey);
  }
    
  if(cacheData!= null && Object.keys(cacheData).length>=1){
    return cacheData;
  }
  else{
    let correlationId = getCorrelationId(requestInfo);
    let requestInfoTenant = get(requestInfo, "userInfo.tenantId", defaultTenant);
    let statetenantid = getStateLevelTenant(requestInfoTenant);

    if (!statetenantid) {
      logger.error(`TENANTID=${requestInfoTenant}, CORRELATION_ID=${correlationId}, STAGE=localisation, PDF_KEY=${pdfKey}, ERROR=resolved TENANTID is empty`);
      throw { message: `unable to resolve TENANTID for localisation, PDF_KEY=${pdfKey}` };
    }

    logger.info(`TENANTID=${statetenantid}, CORRELATION_ID=${correlationId}, STAGE=localisation, PDF_KEY=${pdfKey}, LOCALE=${locale}, CODE_COUNT=${codeList ? codeList.length : 0}`);

    let url = egovLocHost + egovLocSearchCall;

    let request = {
      RequestInfo: requestInfo,
      messageSearchCriteria:{
        tenantId: statetenantid,
        locale: locale,
        codes: []
      }
    };

    request.messageSearchCriteria.module = moduleList.toString();
    request.messageSearchCriteria.codes = codeList.toString().split(",");

    let headers = {
      headers:{
        "content-type": "application/json;charset=UTF-8",
        accept: "application/json, text/plain, */*",
        "TENANTID": statetenantid
      }
    };

    let responseBody = await axios.post(url,request,headers)
    .then(function (response) {
      return response;
      
    })
    .catch((error) => {
      logger.error(`TENANTID=${statetenantid}, CORRELATION_ID=${correlationId}, STAGE=localisation, ERROR=call to ${url} failed: ${error.message}`);
      logger.error(error.stack || error);
      throw error
     });
    if(pdfKey!=null)
      cache.set(pdfKey, responseBody.data);
  
  
    return responseBody.data;
  }
}

export const verifyCache = async (pdfKey) => {
  let cacheData = null;
  if (cache.has(pdfKey)) {
    cacheData = cache.get(pdfKey);

    return Promise.resolve(cacheData);
  }
  else
    return cacheData;
}

export const getLocalisationkey = async (
  prefix,
  key,
  isCategoryRequired,
  isMainTypeRequired,
  isSubTypeRequired,
  delimiter = " / "
) => {

  let keyArray = [];
  let localisedLabels = [];
  let isArray = false;

  if (key == null) {
    return key;
  } else if (typeof key == "string" || typeof key == "number") {
    keyArray.push(key);
  } else {
    keyArray = key;
    isArray = true;
  }

  keyArray.map((item) => {
    let codeFromKey = "";

    // append main category in the beginning
    if (isCategoryRequired) {
        codeFromKey = getLocalisationLabel(
        item.split(".")[0],
        prefix
      );
    }

    if (isMainTypeRequired) {
     if (isCategoryRequired) codeFromKey = `${codeFromKey}${delimiter}`;
        codeFromKey = getLocalisationLabel(
        item.split(".")[1],
        prefix
      );
    }

    if (isSubTypeRequired) {
      if (isMainTypeRequired || isCategoryRequired)
        codeFromKey = `${codeFromKey}${delimiter}`;
        codeFromKey = `${codeFromKey}${getLocalisationLabel(
        item,
        prefix
      )}`;
    }

    if (!isCategoryRequired && !isMainTypeRequired && !isSubTypeRequired) {
      codeFromKey = getLocalisationLabel(item, prefix);
    }

    localisedLabels.push(codeFromKey === "" ? item : codeFromKey);
  });
  if (isArray) {
    return localisedLabels;
  }
  return localisedLabels[0];
};

const getLocalisationLabel = (key, prefix) => {
  if (prefix != undefined && prefix != "") {
    key = `${prefix}_${key}`;
  }
  key = getTransformedLocale(key);
  return key;
};

export const getDateInRequiredFormat = (et, dateformat = "DD/MM/YYYY") => {
  if (!et) return "NA";
  // var date = new Date(Math.round(Number(et)));
  return moment(et).tz(datetimezone).format(dateformat);
};

/**
 *
 * @param {*} value - values to be checked
 * @param {*} defaultValue - default value
 * @param {*} path  - jsonpath from where the value was fetched
 */
export const getValue = (value, defaultValue, path) => {
  if (
    value == undefined ||
    value == null ||
    value.length === 0 ||
    (value.length === 1 && (value[0] === null || value[0] === ""))
  ) {
    // logger.error(`no value found for path: ${path}`);
    return defaultValue;
  } else return value;
};

export const convertFooterStringtoFunctionIfExist = (footer) => {
  if (footer != undefined) {
    footer = Function(`'use strict'; return (${footer})`)();
  }
  return footer;
};

export const isEnvironmentCentralInstance = () => {
  return envVariables.IS_ENVIRONMENT_CENTRAL_INSTANCE == true;
}

export const getStateSchemaIndexPositionInTenantId = () => {
  const value = envVariables.STATE_SCHEMA_INDEX_POSITION_TENANTID;

  // Explicitly check for null/undefined before numeric conversion.
  if (value === null || value === undefined) {
    return 1;
  }

  const num = Number(value);

  // Use Number.isNaN for a robust check; it handles non-numeric strings
  // correctly while allowing 0.
  if (Number.isNaN(num)) {
    return 1;
  }

  // Use the radix parameter with parseInt.
  return parseInt(value, 10);
}

/**
 * Derives the state-level tenant from a tenantId, mirroring
 * MultiStateInstanceUtil.getStateLevelTenant() from services-common.
 *
 * For central instances:
 *   - If tenantId has more segments than STATE_LEVEL_TENANT_ID_LENGTH,
 *     returns the first N segments joined by "."
 *   - Otherwise returns the full tenantId
 *
 * For non-central instances:
 *   - Returns the first segment of the tenantId
 *
 * @param {string} tenantId
 * @returns {string} state-level tenant
 */
export const getStateLevelTenant = (tenantId) => {
  if (!tenantId) {
    logger.info(`TENANTID=${defaultTenant}, CORRELATION_ID=null, STAGE=resolveTenant, INPUT_TENANTID=null, IS_CENTRAL_INSTANCE=${isEnvironmentCentralInstance()}`);
    return defaultTenant;
  }

  let tenantArray = tenantId.split(".");
  let stateTenant = tenantArray[0];

  if (isEnvironmentCentralInstance()) {
    let stateLevelTenantIdLength = parseInt(envVariables.STATE_LEVEL_TENANT_ID_LENGTH) || 1;
    if (stateLevelTenantIdLength < tenantArray.length) {
      for (let i = 1; i < stateLevelTenantIdLength; i++) {
        stateTenant = stateTenant + "." + tenantArray[i];
      }
    } else {
      stateTenant = tenantId;
    }
  }

  logger.info(`TENANTID=${stateTenant}, CORRELATION_ID=null, STAGE=resolveTenant, INPUT_TENANTID=${tenantId}, IS_CENTRAL_INSTANCE=${isEnvironmentCentralInstance()}`);
  return stateTenant;
}

export const getCorrelationId = (requestInfo, headers) => {
  let cid = get(requestInfo, "correlationId");
  if (!cid && headers) {
    cid = headers["x-correlation-id"] || headers["correlation-id"];
  }
  if (!cid) {
    cid = get(requestInfo, "msgId");
  }
  return cid || null;
}


