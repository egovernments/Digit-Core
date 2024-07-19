const express = require("express");
const router = express.Router();
const url = require("url");
const config = require("../config");

const { search_estimateDetails, create_pdf } = require("../api");

const { asyncMiddleware } = require("../utils/asyncMiddleware");
const { pdf } = require("../config");
const { logger } = require("../logger");
const { transformDeviationData } = require("../utils/transformDeviationData");

function renderError(res, errorMessage, errorCode) {
    if (errorCode == undefined) errorCode = 500;
    res.status(errorCode).send({ errorMessage })

}
router.post(
    "/deviation-statement",
    asyncMiddleware(async function (req, res, next) {
        const tenantId = req.query.tenantId;
        const estimateNumber = req.query.estimateNumber;
        const requestinfo = req.body;
        if (requestinfo == undefined) {
            return renderError(res, "requestinfo can not be null", 400)
        }
        if (!tenantId) {
            return renderError(res, "tenantId is mandatory to generate the receipt", 400)
        }
        if (!estimateNumber) {
            return renderError(res, "estimateNumber is mandatory to generate the receipt", 400)
        }
        try {
            try {
                var resEstimate = await search_estimateDetails(tenantId, requestinfo, estimateNumber);
            }
            catch (ex) {
                return renderError(res, "Failed to query details of the estimate", 500);
            }
            const estimate = resEstimate.data;
            if(estimate.estimates.length <2){
                return renderError(res, "Revision is not done for this estimate", 404);
            }

            var estimates;
            if (estimate && estimate.estimates.length > 1) {
                estimates = transformDeviationData(estimate);
            }
            estimate.pdfData = estimates;

            if (estimate && estimate.pdfData) {
                    var pdfResponse;
                    const pdfkey = config.pdf.deviationStatement_template;
                    try {
                        pdfResponse = await create_pdf(
                            tenantId,
                            pdfkey,
                            estimate,
                            requestinfo
                        )
                    }
                    
                    catch (ex) {
                        return renderError(res, "Failed to generate PDF for estimates", 500);
                    }

                    const filename = `${pdfkey}_${new Date().getTime()}`;

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
