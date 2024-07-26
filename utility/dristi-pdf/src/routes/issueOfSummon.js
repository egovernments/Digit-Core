const express = require("express");
const router = express.Router();
const url = require("url");
const config = require("../config");

const { search_estimateDetails, search_case, search_order, search_hearing, search_mdms_order, search_hrms, search_individual, create_pdf } = require("../api");

const { asyncMiddleware } = require("../utils/asyncMiddleware");
const { pdf } = require("../config");
const { logger } = require("../logger");
const { transformDetailedData } = require("../utils/transformDetailedData");
const { transformIssueOfSummon } = require("../utils/transformIssueOfSummon");

function renderError(res, errorMessage, errorCode, errorObject) {
    if (errorCode == undefined) errorCode = 500;
    logger.error(`${errorMessage}: ${errorObject ? errorObject.stack || errorObject : ''}`);
    res.status(errorCode).send({ errorMessage });
}

function formatDate(date) {
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0'); // Months are zero-based
    const year = date.getFullYear();
    return `${day}-${month}-${year}`;
}

router.post(
    "/issue-summon",
    asyncMiddleware(async function (req, res, next) {
        const cnrNumber = req.query.cnrNumber;
        const id = req.query.id;
        const orderId = req.query.orderId;
        const tenantId = req.query.tenantId;
        const requestInfo = req.body;

        if (!cnrNumber) {
            return renderError(res, "cnrNumber is mandatory to generate the receipt", 400);
        }
        if (!id) {
            return renderError(res, "id is mandatory to generate the receipt", 400);
        }
        if (!orderId) {
            return renderError(res, "orderId is mandatory to generate the receipt", 400);
        }
        if (!tenantId) {
            return renderError(res, "tenantId is mandatory to generate the receipt", 400);
        }
        if (requestInfo == undefined) {
            return renderError(res, "requestInfo cannot be null", 400);
        }

        try {
            var resCase;
            try {
                resCase = await search_case(cnrNumber, tenantId, requestInfo);
            } catch (ex) {
                return renderError(res, "Failed to query details of the case", 500, ex);
            }
            var courtCase = resCase.data.criteria[0].responseList[0];

            var resHrms;
            try {
                resHrms = await search_hrms(tenantId, "JUDGE", courtCase.courtId, requestInfo);
            } catch (ex) {
                return renderError(res, "Failed to query details of HRMS", 500, ex);
            }
            var employee = resHrms.data.Employees[0];

            var resMdms;
            try {
                resMdms = await search_mdms_order(courtCase.courtId, "common-masters.Court_Rooms", tenantId, requestInfo);
            } catch (ex) {
                return renderError(res, "Failed to query details of the court room mdms", 500, ex);
            }
            var mdmsCourtRoom = resMdms.data.mdms[0].data;

            var resMdms1;
            try {
                resMdms1 = await search_mdms_order(mdmsCourtRoom.courtEstablishmentId, "case.CourtEstablishment", tenantId, requestInfo);
            } catch (ex) {
                return renderError(res, "Failed to query details of the court establishment mdms", 500, ex);
            }
            var mdmsCourtEstablishment = resMdms1.data.mdms[0].data;
            

            var resOrder;
            try {
                resOrder = await search_order(tenantId, cnrNumber, orderId, requestInfo);
            } catch (ex) {
                return renderError(res, "Failed to query details of the order", 500, ex);
            }
            var order = resOrder.data.list[0];

            var resHearing;
            try {
                resHearing = await search_hearing(tenantId, cnrNumber, requestInfo);
            } catch (ex) {
                return renderError(res, "Failed to query details of the hearing", 500, ex);
            }
            var hearing = resHearing.data.HearingList[0];

            // const hearingDate = formatDate(new Date(hearing.startTime));


            // Filter litigants to find the respondent.primary
            const respondentParty = courtCase.litigants.find(party => party.partyType === 'respondent.primary');
            if (!respondentParty) {
                return renderError(res, "No respondent with partyType 'respondent.primary' found", 400);
            }

            var resIndividual;
            try {
                resIndividual = await search_individual(tenantId, respondentParty.individualId, requestInfo);
            } catch (ex) {
                return renderError(res, "Failed to query details of the individual", 500, ex);
            }
            var individual = resIndividual.data.Individual[0];



            let year;
            if (typeof courtCase.filingDate === 'string') {
                year = courtCase.filingDate.slice(-4);
            } else if (courtCase.filingDate instanceof Date) {
                year = courtCase.filingDate.getFullYear();
            }else if (typeof courtCase.filingDate === 'number') {
             // Assuming the number is in milliseconds (epoch time)
                year = new Date(courtCase.filingDate).getFullYear();
            }  else {
                return renderError(res, "Invalid filingDate format", 500);
            }

            const data = {
                "Data": [
                    {
                        "id": id,
                        "courtName": mdmsCourtRoom.name,
                        "place": mdmsCourtEstablishment.boundaryName,
                        "state": mdmsCourtEstablishment.rootBoundaryName,
                        "caseNumber": courtCase.cnrNumber,
                        "year": year,
                        "caseName": courtCase.caseTitle,
                        "respondentName": `${individual.name.givenName} ${individual.name.familyName}`,
                        "date": order.createdDate,
                        "hearingDate": hearing.startTime,
                        "additionalComments": "Please ensure all relevant documents are submitted prior to the hearing date.",
                        "judgeSignature": "Judge's Signature",
                        "judgeName": employee.user.name,
                        "courtSeal": "Court Seal"
                    }
                ]
            };

            var pdfResponse;
            const pdfKey = config.pdf.issue_of_summon;
            try {
                pdfResponse = await create_pdf(
                    tenantId,
                    pdfKey,
                    data,
                    requestInfo
                );
            } catch (ex) {
                return renderError(res, "Failed to generate PDF for detailed estimate", 500, ex);
            }
            const filename = `${pdfKey}_${new Date().getTime()}`;
            res.writeHead(200, {
                "Content-Type": "application/pdf",
                "Content-Disposition": `attachment; filename=${filename}.pdf`,
            });
            pdfResponse.data.pipe(res);

        } catch (ex) {
            return renderError(res, "Failed to query details of issue of summons", 500, ex);
        }
    })
);

module.exports = router;
