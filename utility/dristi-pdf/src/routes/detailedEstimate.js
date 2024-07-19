const express = require("express");
const router = express.Router();
const url = require("url");
const config = require("../config");

const { search_estimateDetails, create_pdf } = require("../api");

const { asyncMiddleware } = require("../utils/asyncMiddleware");
const { pdf } = require("../config");
const { logger } = require("../logger");
const { transformDetailedData } = require("../utils/transformDetailedData");

function renderError(res, errorMessage, errorCode) {
    if (errorCode == undefined) errorCode = 500;
    res.status(errorCode).send({ errorMessage })

}
router.post(
    "/detailed-estimate",
    asyncMiddleware(async function (req, res, next) {
        const estimateNumber = req.query.estimateNumber;
        const tenantId = req.query.tenantId;
        const requestinfo = req.body;

        if (!estimateNumber) {
            return renderError(res, "estimateNumber is mandatory to generate the receipt", 400)
        }
        if (!tenantId) {
            return renderError(res, "tenantId is mandatory to generate the receipt", 400)
        }
        if (requestinfo == undefined) {
            return renderError(res, "requestinfo can not be null", 400)
        }

        try {
            try {

                resEstimate = await search_estimateDetails(tenantId, requestinfo, estimateNumber);

            }
            catch (ex) {

                return renderError(res, "Failed to query details of the estimate", 500);

            }
            var estimate = resEstimate.data;


            const estimates = transformDetailedData(estimate);
            
            estimate.pdfData = estimates;

            if (estimate && estimate.pdfData) {

                    var pdfResponse;

                    const pdfkey = config.pdf.detailedEstimate_template;

                    try {

                        pdfResponse = await create_pdf(
                            tenantId,
                            pdfkey,
                            estimate,
                            requestinfo
                        )

                    }
                    

                    catch (ex) {

                        return renderError(res, "Failed to generate PDF for detailed estimate", 500);

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

            } 
            catch (ex) {

                return renderError(res, "Failed to query details of estimate", 500);
                
            }

        })

);

module.exports = router;
