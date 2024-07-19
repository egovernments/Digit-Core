const express = require("express");
const router = express.Router();
const url = require("url");
const config = require("../config");

const { search_estimateDetails,search_case,search_mdms_order,search_hrms,create_pdf } = require("../api");

const { asyncMiddleware } = require("../utils/asyncMiddleware");
const { pdf } = require("../config");
const { logger } = require("../logger");
const { transformDetailedData } = require("../utils/transformDetailedData");
const {transformIssueOfSummon} = require("../utils/transformIssueOfSummon")

function renderError(res, errorMessage, errorCode) {
    if (errorCode == undefined) errorCode = 500;
    res.status(errorCode).send({ errorMessage })

}
router.post(
    "/issue-summon",
    asyncMiddleware(async function (req, res, next) {
        const cnrNumber = req.query.cnrNumber;
        const id = req.query.id;
        const tenantId = req.query.tenantId;
        const requestinfo = req.body;

        if (!cnrNumber) {
            return renderError(res, "cnrNumber is mandatory to generate the receipt", 400)
        }
        if (!id) {
            return renderError(res, "id is mandatory to generate the receipt", 400)
        }
        if (!tenantId) {
            return renderError(res, "tenantId is mandatory to generate the receipt", 400)
        }
        if (requestinfo == undefined) {
            return renderError(res, "requestinfo can not be null", 400)
        }

        try {
            try {
                var resCase = await search_case(cnrNumber,tenantId,requestinfo);

            }
            catch (ex) {

                return renderError(res, "Failed to query details of the case", 500);

            }
            var caseBody1 = resCase.data;

            try {
                var reshrms = await search_hrms("EMP-0-000030",tenantId,requestinfo);

            }
            catch (ex) {

                return renderError(res, "Failed to query details of the hrms", 500);

            }

            var hrmsBody = reshrms.data;

            try {
                var resmdms = await search_mdms_order( "common-masters","Court_Rooms",tenantId,requestinfo);

            }
            catch (ex) {

                return renderError(res, "Failed to query details of the mdms", 500);

            }
            var mdmsBody = resmdms.data;

            var caseBody = transformIssueOfSummon(caseBody1);
            
            if (caseBody) {

                    var pdfResponse;

                    const pdfKey = config.pdf.issue_of_summon;

                    try {

                        pdfResponse = await create_pdf(
                            tenantId,
                            pdfKey,
                            caseBody,
                            requestinfo
                        )

                    }
                    

                    catch (ex) {
                        if (ex.response && ex.response.data)
                        return renderError(res, "Failed to generate PDF for detailed estimate", 500);

                    }

                    const filename = `${pdfKey}_${new Date().getTime()}`;

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
