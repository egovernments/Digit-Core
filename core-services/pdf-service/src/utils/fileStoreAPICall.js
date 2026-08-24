import request from "request";
import fs from "fs";
import get from "lodash/get";
import axios, { post } from "axios";
var FormData = require("form-data");
import envVariables from "../EnvironmentVariables";
import logger from "../config/logger";
import { getCorrelationId } from "./commons";

let egovFileHost = envVariables.EGOV_FILESTORE_SERVICE_HOST;
let externalHost = envVariables.EGOV_EXTERNAL_HOST;

/**
 *
 * @param {*} filename -name of localy stored temporary file
 * @param {*} tenantId - tenantID
 */
export const fileStoreAPICall = async function(filename, tenantId, fileData, header) {
  var url = `${egovFileHost}/filestore/v1/files?tenantId=${tenantId}&module=pdfgen&tag=00040-2017-QR`;
  var form = new FormData();
  form.append("file", fileData, {
    filename: filename,
    contentType: "application/pdf"
  });
  const correlationId = getCorrelationId(null, header);
  logger.info(`TENANTID=${tenantId}, CORRELATION_ID=${correlationId}, STAGE=filestore, FILENAME=${filename}, STATUS=posting`);
  let response;
  try {
    response = await axios.post(url, form, {
      maxContentLength: Infinity,
      maxBodyLength: Infinity,
      headers: {
        ...form.getHeaders()
      }
    });
  } catch (error) {
    logger.error(`TENANTID=${tenantId}, CORRELATION_ID=${correlationId}, STAGE=filestore, FILENAME=${filename}, ERROR=filestore call failed: ${error.message}`);
    logger.error(error.stack || error);
    throw { message: `filestore upload failed for TENANTID=${tenantId}: ${error.message}` };
  }
  let fileStoreId = get(response.data, "files[0].fileStoreId");
  if (!fileStoreId) {
    logger.error(`TENANTID=${tenantId}, CORRELATION_ID=${correlationId}, STAGE=filestore, FILENAME=${filename}, ERROR=no fileStoreId returned by filestore`);
    throw { message: `filestore did not return a fileStoreId for TENANTID=${tenantId}` };
  }
  return fileStoreId;
};

export async function getFilestoreUrl(filestoreid, tenantId){
  var url = `${egovFileHost}/filestore/v1/files/url?tenantId=${tenantId}&fileStoreIds=${filestoreid}`;
  try {
    let response = await axios.get(url);
    let data = response.data;
    let fileEntry = get(data, "fileStoreIds[0].url");
    if (!fileEntry) {
      throw new Error(`filestore returned no url for FILESTOREID=${filestoreid}`);
    }
    var fileURL = fileEntry.split(",");
    return await getShortneningUrl(fileURL[0]);
  } catch (error) {
    logger.error(`TENANTID=${tenantId}, STAGE=filestoreUrl, FILESTOREID=${filestoreid}, ERROR=${error.message}`);
    logger.error(error.stack || error);
    throw new Error(`filestore url fetch failed for FILESTOREID=${filestoreid}, TENANTID=${tenantId}: ${error.message}`);
  }
}

export async function getShortneningUrl(actualUrl){
  var url = `${externalHost}egov-url-shortening/shortener`;
  var request = {
    "url": actualUrl
  };

  let headers = {
    headers:{
      'Content-Type': 'application/json'
    }
  };

  try {
    let response = await axios.post(url,request, headers);
    return response.data;
  } catch (error) {
    logger.error(`STAGE=urlShortening, URL=${actualUrl}, ERROR=${error.message}`);
    logger.error(error.stack || error);
    throw new Error(`url shortening failed: ${error.message}`);
  }
}