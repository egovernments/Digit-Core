var express = require("express");
var router = express.Router();
var url = require("url");
var config = require("../config");

var { search_estimateDetails, create_pdf } = require("../api");
const { asyncMiddleware } = require("../utils/asyncMiddleware");
const { pdf } = require("../config");

function renderError(res, errorMessage, errorCode) {
    if (errorCode == undefined) errorCode = 500;
    res.status(errorCode).send({ errorMessage })

}
router.post(
    "/estimates",
    asyncMiddleware(async function (req, res, next) {
        var tenantId = req.query.tenantId;
        var estimateNumber = req.query.estimateNumber;
        var requestinfo = req.body;
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
                resEstimate = await search_estimateDetails(tenantId, requestinfo, estimateNumber);
            }
            catch (ex) {
                if (ex.response && ex.response.data) console.log(ex.response.data);
                return renderError(res, "Failed to query details of the estimate", 500);
            }
            var estimate = resEstimate.data;
            if (estimate && estimate.estimates && estimate.estimates.length > 0) {
                    var pdfResponse;
                    var pdfkey = config.pdf.estimate_template;
                    try {
                        pdfResponse = await create_pdf(
                            tenantId,
                            pdfkey,
                            estimate,
                            requestinfo
                        )
                    }
                    
                    catch (ex) {
                        if (ex.response && ex.response.data) console.log(ex.response.data);
                        return renderError(res, "Failed to generate PDF for estimates", 500);
                    }

                    var filename = `${pdfkey}_${new Date().getTime()}`;

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