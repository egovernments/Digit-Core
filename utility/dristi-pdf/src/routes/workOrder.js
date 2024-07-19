var express = require("express");
var router = express.Router();
var url = require("url");
var config = require("../config");

var { search_organisation, search_contract, create_pdf, search_workflow, search_localization } = require("../api");
const { asyncMiddleware } = require("../utils/asyncMiddleware");
const get = require("lodash.get");
const { getStateLocalizationModule, getLanguageFromRequest, getCityLocalizationModule, getLocalizationByKey } = require("../utils/localization");

function renderError(res, errorMessage, errorCode) {
    if (errorCode == undefined) errorCode = 500;
    res.status(errorCode).send({ errorMessage })

}

async function getLocalizaitons(request, tenantId) {
    let localizationMaps = {};
    let lang = getLanguageFromRequest(request);
    let modules = [getStateLocalizationModule(tenantId),getCityLocalizationModule(tenantId)].join(",");
    let localRequest = {}
    localRequest["RequestInfo"] = request["RequestInfo"];
    let localizations = await search_localization(localRequest, lang, modules, tenantId);
    if (localizations?.data?.messages?.length) {
        localizations.data.messages.forEach(localObj => {
            localizationMaps[localObj.code] = localObj.message;
        });
    }
    return localizationMaps;
}

router.post(
    "/work-order",
    asyncMiddleware(async function (req, res, next) {
        var tenantId = req.query.tenantId;
        var contractId = req.query.contractId;
        var requestinfo = req.body;
        if (requestinfo == undefined) {
            return renderError(res, "requestinfo can not be null", 400)
        }
        if (!tenantId) {
            return renderError(res, "tenantId is mandatory to generate the receipt", 400)
        }
        if (!contractId) {
            return renderError(res, "contractId is mandatory to generate the receipt", 400)
        }
        try {
            try {
                resContract = await search_contract(tenantId, requestinfo, contractId);
            }
            catch (ex) {
                if (ex.response && ex.response.data) console.log(ex.response.data);
                return renderError(res, "Failed to query details of the contract", 500);
            }
            try {
                resOrg = await search_organisation(tenantId, requestinfo, resContract.data.contracts[0].orgId);
            }
            catch (ex) {
                if (ex.response && ex.response.data) console.log(ex.response.data);
                return renderError(res, "Failed to query details of the organisation", 500);
            }
            try {
                let workflowRequest = {};
                workflowRequest["RequestInfo"] = requestinfo["RequestInfo"];
                resWorkFlow = await search_workflow(contractId, tenantId, workflowRequest);
            }
            catch (ex) {
                if (ex.response && ex.response.data) console.log(ex.response.data);
                return renderError(res, "Failed to query details of the workflow", 500);
            }

            // Get localizations as map
            let localizationMaps = await getLocalizaitons(requestinfo, tenantId);
            

            var contract = resContract.data;
            var organisation = resOrg.data
            let worflow = resWorkFlow?.data || {};
            if (contract && contract.contracts && contract.contracts.length > 0 && organisation && organisation.organisations && organisation.organisations.length > 0 && worflow?.ProcessInstances?.length) {
                var pdfResponse;
                const locale=requestinfo ?.RequestInfo ?.msgId ?.split("|")[1];
                var pdfkey;

                switch(locale){
                   case "hi_IN":
                         pdfkey = config.pdf.work_order_template_hindi;
                        break;
                   case "or_IN":
                         pdfkey = config.pdf.work_order_template_odiya;
                        break;
                   default:
                         pdfkey = config.pdf.work_order_template;
                }

                contract.contracts[0].contactName = organisation.organisations[0].contactDetails[0].contactName
                contract.contracts[0].nameOfCbo = organisation.organisations[0].name
                contract.contracts[0].city = organisation.organisations[0].orgAddress[0].city
                contract.contracts[0].doorNo = organisation.organisations[0].orgAddress[0].doorNo
                contract.contracts[0].street = organisation.organisations[0].orgAddress[0].street

                let address = [];
                if (contract.contracts[0].street) {
                    address.push(contract.contracts[0].street)
                }
                if (contract.contracts[0].doorNo) {
                    address.push(contract.contracts[0].doorNo)
                }
                if (contract.contracts[0].city) {
                    let city = contract.contracts[0].city.toUpperCase();
                    let cityKey = "TENANT_TENANTS_" + city.split(".").join("_");
                    city = getLocalizationByKey(cityKey, localizationMaps);
                    address.push(city)
                }
                if (address.length) {
                    address = address.join(", ");
                    contract.contracts[0].pdfCboAddress = address;
                }

                // Get issuer name 
                contract.contracts[0].pdfNameOfIssuedTo = contract.contracts[0].totalContractedAmount <= 1500000 ? organisation.organisations[0].name :  get(contract.contracts[0], 'additionalDetails.officerInChargeName.name', null);
                
                // Get days left to accept the work order
                let sla = get(worflow, "ProcessInstances[0].businesssServiceSla", 0);
                let todaysDateTime = new Date().getTime();
                let workflowAuditDetails = get(worflow, "ProcessInstances[0].auditDetails", {});
                // Get today's date time and subtract with accpted date and add the remaining sla to find the total number of days
                sla = sla + (todaysDateTime - workflowAuditDetails.createdTime);
                let slaDays = parseInt(sla) / (24*60*60*1000);
                contract.contracts[0].pdfWorkOrdAcceptanceDays = parseInt(slaDays.toFixed());
                var slaDate = workflowAuditDetails.createdTime + sla;
                contract.contracts[0].pdfSlaDate = slaDate;
                contract.contracts[0].pdfAcceptedDate  = workflowAuditDetails.createdTime;

                try {
                    pdfResponse = await create_pdf(
                        tenantId,
                        pdfkey,
                        contract,
                        requestinfo
                    )
                }
                catch (ex) {
                    if (ex.response && ex.response.data) console.log(ex.response.data);
                    return renderError(res, "Failed to generate PDF for work order", 500);
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
                    "There is no contract created using this contractId",
                    404
                );
            }
        } catch (ex) {
            return renderError(res, "Failed to query details of the contract", 500);
        }

    })

);

module.exports = router;