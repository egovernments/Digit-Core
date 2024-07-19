const express = require("express");
const router = express.Router();
const url = require("url");
const config = require("../config");

const { search_measurementBookDetails, create_pdf } = require("../api");
const { asyncMiddleware } = require("../utils/asyncMiddleware");
const { pdf } = require("../config");
const { logger } = require("../logger");

const { transformEstimateData } = require("../utils/transformEstimateData");

function renderError(res, errorMessage, errorCode) {
    if (errorCode == undefined) errorCode = 500;
    res.status(errorCode).send({ errorMessage })

}
router.post(
    "/measurement-book",
    asyncMiddleware(async function (req, res, next) {
        const tenantId = req.query.tenantId;
        const contractNumber = req.query.contractNumber;
        const measurementNumber = req.query.measurementNumber;
        const requestinfo = req.body;
        if (requestinfo == undefined) {
            return renderError(res, "requestinfo can not be null", 400)
        }
        if (!tenantId) {
            return renderError(res, "tenantId is mandatory to generate the receipt", 400)
        }
        if (!measurementNumber) {
            return renderError(res, "measurementNumber is mandatory to generate the receipt", 400)
        }
        if (!contractNumber) {
            return renderError(res, "contractNumber is mandatory to generate the receipt", 400)
        }
        try {
            try {
                var resMeasurement = await search_measurementBookDetails(tenantId, requestinfo, contractNumber, measurementNumber);
            }
            catch (ex) {
                if (ex.response && ex.response.data)
                return renderError(res, "Failed to query details of the measurement", 500);
            }
            const measurementBookDetails = resMeasurement.data;

            const contract = resMeasurement.data?.contract;
            const lineItems = resMeasurement.data?.contract?.lineItems;
            const measurement = resMeasurement.data?.measurement;
            const allMeasurements = resMeasurement.data?.allMeasurements;
            const estimateDetails = resMeasurement.data?.estimate?.estimateDetails;

            // convert startDateTime and endDateTime into dd/mm/yyyy format and show only date in a variable named measurement period
            const startDate = new Date(measurement.additionalDetails.startDate);
            const endDate = new Date(measurement.additionalDetails.endDate);
            const measurementPeriod = startDate.getDate() + "/" + (startDate.getMonth() + 1) + "/" + startDate.getFullYear() + " - " + endDate.getDate() + "/" + (endDate.getMonth() + 1) + "/" + endDate.getFullYear();

            // make a new variable in measurement named measurementPeriod and assign measurementPeriod to it
            measurementBookDetails.measurement.measurementPeriod = measurementPeriod;

            var transformedData;
            if(measurementBookDetails){
                transformedData = transformEstimateData(lineItems, contract, measurement, allMeasurements, estimateDetails);
            }

            // make an array of all the values from the transformedData without keys
            const transformedDataValues = Object.values(transformedData);
            measurementBookDetails.tableData = transformedDataValues;

            if(measurementBookDetails){
                    var pdfResponse;
                    var pdfkey = config.pdf.measurement_template;
                    try {
                        pdfResponse = await create_pdf(
                            tenantId,
                            pdfkey,
                            measurementBookDetails,
                            requestinfo
                        )
                    }
                    
                    catch (ex) {
                        if (ex.response && ex.response.data)
                        return renderError(res, "Failed to generate PDF for measurement", 500);
                    }

                    const filename = `${pdfkey}_${new Date().getTime()}`;

                    //pdfData = pdfResponse.data.read();
                    res.writeHead(200, {
                        "Content-Type": "application/pdf",
                        "Content-Disposition": `attachment; filename=${filename}.pdf`,
                    });

                    pdfResponse.data.pipe(res);
                }
                else {
                    return renderError(
                        res,
                        "There is no estimate created using this estimate number",
                        404
                    );
                }
            } catch (ex) {
                return renderError(res, "Failed to query details of the estimate", 500);
            }

        })

);

module.exports = router;