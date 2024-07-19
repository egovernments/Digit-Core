var { search_expense_bill, search_bank_account_details, upload_file_using_filestore, search_payment_details, create_eg_payments_excel, exec_query_eg_payments_excel, search_mdms, search_expense_calculator_bill } = require("./api");
const XLSX = require('xlsx');
var logger = require("./logger").logger;
let get = require('lodash.get');
const config = require("./config");

async function processGroupBillFromPaymentCreateTopic(requestData) {
    logger.info("Started generating bill from payment topic.")
    let userid = get(requestData, "RequestInfo.userInfo.uuid", null);
    let paymentId = get(requestData, "payment.id", null);
    try {
        let request = {}
        let filestoreId = null;
        if (requestData.RequestInfo && requestData?.payment?.bills && requestData.payment.tenantId) {
            request.paymentId = requestData.payment.id;
            let paymentNumber = requestData.payment.paymentNumber;
            request.billIds = [];
            request.billIds = requestData.payment.bills.map(bill => {return bill.billId})
            request.RequestInfo = requestData.RequestInfo;
            request.tenantId = requestData.payment.tenantId;
            request.paymentNumber = paymentNumber;
            // Get user uuid and payment id and create an entry on db
            userid = get(request, "RequestInfo.userInfo.uuid", null)
            paymentId = request.paymentId;
            await create_eg_payments_excel(paymentId, paymentNumber, request.tenantId, userid);
            filestoreId = await processGroupBill(request);
        }
        return filestoreId;
    } catch (error) {
        logger.info("Exception Catched on processGroupBill from create topic.");
        logger.error(error.stack || error);
        await updatePaymentExcelIfJobFailed(paymentId, userid);
        return;
    }
}


async function processGroupBillFromPaymentId(requestData) {
    let userid =  get(requestData, "RequestInfo.userInfo.uuid", null);;
    let paymentId = requestData?.paymentId;
    try {
        let filestoreId = null;
        if (requestData.RequestInfo && requestData?.paymentId && requestData.tenantId) {
            let request = {}
            userid = get(requestData, "RequestInfo.userInfo.uuid", null);
            // paymentId = requestData.paymentId;
            request.RequestInfo = requestData.RequestInfo;
            request.paymentCriteria = {
                "tenantId": requestData.tenantId,
                "ids": [requestData.paymentId]
            };
            let paymentDetails = await search_payment_details(request);
            if (paymentDetails && paymentDetails?.payments?.length) {
                let groupBillRequest = {} ;
                groupBillRequest.RequestInfo = request.RequestInfo;
                let payment = paymentDetails.payments[0];
                groupBillRequest.paymentId = payment.id;
                groupBillRequest.paymentNumber = payment.paymentNumber;
                groupBillRequest.billIds = [];
                groupBillRequest.billIds = payment.bills.map(bill => {return bill.billId})
                groupBillRequest.RequestInfo = requestData.RequestInfo;
                groupBillRequest.tenantId = payment.tenantId;
                filestoreId = await processGroupBill(groupBillRequest);
            }
        }
        return filestoreId;
    } catch (error) {
        logger.error(error.stack || error);
        await updatePaymentExcelIfJobFailed(paymentId, userid);
        return;
    }
}
async function processGroupBill(requestData) {
    try {
        logger.info("Started processGroupBill.")
        let paymentId = requestData.paymentId;
        let paymentNumber = requestData.paymentNumber;
        let tenantId = requestData.tenantId;
        let userId = requestData?.RequestInfo?.userInfo?.uuid;

        let exBills = await getBillDetails(requestData);
        let bills = await getBillDetailsUsingExCalc(requestData, exBills);
        let headCodes = await getHeadCodeFromMDMS(requestData);
        let headCodeMap = {};
        headCodes.forEach((headCode) => headCodeMap[headCode.code] = headCode);
        let billsForExcel = [];
        if (bills) {
            bills.forEach((bill, idx) => {
                let nBills = getBillsForExcel(bill, headCodeMap);
                if (nBills.length)
                billsForExcel = billsForExcel.concat(nBills);
            })
        }
        
        
        let accountIdMap = {};
        let beneficiaryIds = []
        billsForExcel.forEach((bill) => {
            if (bill?.beneficiaryId && !accountIdMap[bill.beneficiaryId]) {
                accountIdMap[bill.beneficiaryId] = {}
                beneficiaryIds.push(bill.beneficiaryId)
            }
        })
        logger.info("Fetching bank details")
        let bankAccounts = await getBankAccountDetails(requestData, beneficiaryIds);
        bankAccounts.forEach((bankAccount) => { accountIdMap[bankAccount.referenceId] = bankAccount });
        billsForExcel = billsForExcel.map((billDetails) => {
            // Set projectNumber as projectId

            if (accountIdMap[billDetails.beneficiaryId]) {
                let accountDetails = accountIdMap[billDetails.beneficiaryId] || {};
                let bankAccountDetails = get(accountDetails, 'bankAccountDetails[0]', {});
                billDetails['bankAccountNumber'] = bankAccountDetails?.accountNumber || "";
                billDetails['accountType'] = bankAccountDetails?.accountType || "";
                billDetails['beneficiaryName'] = bankAccountDetails?.accountHolderName || "";
                billDetails['ifsc'] = bankAccountDetails?.bankBranchIdentifier?.code || "";
            }
            return billDetails;
        })
        logger.info("Creating excel.")
        let filestoreId = await createXlsxFromBills(billsForExcel, paymentId, paymentNumber, tenantId);
        let billsLength = bills.length;
        let numberofbeneficialy = billsForExcel.length;
        let totalAmount = 0;
        bills.forEach((bill) => {
            if (bill?.bill?.totalAmount) {
                totalAmount = totalAmount + bill?.bill?.totalAmount;
            }
        })
        if (totalAmount) {
            totalAmount = parseFloat(totalAmount.toFixed(2));
        }
        logger.info("Update file id and other details.")
        await updateForJobCompletion(paymentId, filestoreId, userId, billsLength, numberofbeneficialy, totalAmount);
        logger.info("File generated and saved.")
        return filestoreId;
    } catch (error) {
        logger.error(error.stack || error);
        throw(error)
    }
}

const getHeadCodeFromMDMS = async (requestData) => {
    try {
        let request = {}
        request['RequestInfo'] = requestData['RequestInfo']
        request['MdmsCriteria'] = {
            tenantId: requestData.tenantId.split(".")[0],
            "moduleDetails": [
                {
                    "moduleName": "expense",
                    "masterDetails": [
                        {
                            "name": "HeadCodes"
                        }
                    ]
                }
            ]
        }
        let mdmsHeadCode = await search_mdms(request);
        let headCodes = get(mdmsHeadCode, "data.MdmsRes.expense.HeadCodes", [])
        return headCodes;
        
    } catch (error) {
        logger.error(error.stack || error);
        return null;
    }
}


const getBillDetails = async (requestData) => {
    let bills = [];
    try {
        let request = {}
        request['RequestInfo'] = requestData['RequestInfo']
        request['billCriteria'] = {
            tenantId: requestData.tenantId,
            ids: requestData.billIds
        }
        // Get how many expenses are there
        // let expenseResponse = await search_expense_bill(request, 1, 0)
        // let pagination = expenseResponse?.pagination;
        // pagination.totalCount = 11;
        // let total = pagination.totalCount;
        let total = requestData.billIds.length;
        if (total) {
            let limit = 100;
            let rounds = total / limit;
            let promises = [];
            for (let idx = 0; idx < rounds; idx++) {
                let offset = limit * idx;
                promises.push(fetchBillDetailsByRequest(deepClone(request), limit, offset));
            }
            let billResponse = await Promise.all(promises)
            for (let idx = 0; idx < billResponse.length; idx++) {
                bills = bills.concat(billResponse[idx]);
            }
        }
    } catch (error) {
        logger.error(error.stack || error);
    }
    return bills;
}

const getBillDetailsUsingExCalc = async (requestData, bills) => {
    let calcBills = [];
    try {
        let request = {}
        let billNumbers = bills.map(bill => {return bill.billNumber})
        request['RequestInfo'] = requestData['RequestInfo']
        request['searchCriteria'] = {
            tenantId: requestData.tenantId,
            billNumbers: billNumbers
        }
        // Get how many expenses are there
        let total = billNumbers.length;
        if (total) {
            let limit = 100;
            let rounds = total / limit;
            let promises = [];
            for (let idx = 0; idx < rounds; idx++) {
                let offset = limit * idx;
                promises.push(fetchBillDetailsFromExCalcByRequest(deepClone(request), limit, offset));
            }
            let billResponse = await Promise.all(promises)
            for (let idx = 0; idx < billResponse.length; idx++) {
                calcBills = calcBills.concat(billResponse[idx]);
            }
        }
    } catch (error) {
        logger.error(error.stack || error);
    }
    return calcBills;
}

const getBankAccountDetails = async (requestData, beneficiaryIds) => {
    let bankAccounts = []
    let defaultRequest = {}
    defaultRequest['RequestInfo'] = requestData['RequestInfo'];
    defaultRequest["bankAccountDetails"] = {};
    defaultRequest["bankAccountDetails"]["tenantId"] = requestData?.tenantId;
    defaultRequest["bankAccountDetails"]["referenceId"] = beneficiaryIds;
    let total = beneficiaryIds.length;
    if (total) {
        let limit = total;
        let rounds = total / limit;
        let requests = [];
        for (let idx = 0; idx < rounds; idx++) {
            let nRequest = {}
            nRequest["bankAccountDetails"] = defaultRequest["bankAccountDetails"]
            nRequest['pagination'] = {
                limit: limit,
                offSet: idx * limit,
                sortBy: "createdTime",
                order: "DESC"
            }
            nRequest['RequestInfo'] = defaultRequest["RequestInfo"]
            requests.push(nRequest);
        }
        let promises = [];
        requests.forEach((request) => {
            promises.push(fetchBankAccountDetailsByRequest(deepClone(request)));
        })
        let bankAccountResponse = await Promise.all(promises)
        for (let idx = 0; idx < bankAccountResponse.length; idx++) {
            bankAccounts = bankAccounts.concat(bankAccountResponse[idx]);
        }
    }
    return bankAccounts;
}

const fetchBillDetailsByRequest = (request, limit, offset) => {
    return new Promise((resolve, reject) => {
        try {
            search_expense_bill(deepClone(request), limit, offset).then((data) => {
                if (data?.bills?.length) {
                    resolve(data.bills)
                } else {
                    resolve([])
                }
            }).catch(err => reject(err))
        } catch (error) {
            reject(error)
            logger.error(error.stack || error);
        }
    })
}

const fetchBillDetailsFromExCalcByRequest = (request, limit, offset) => {
    return new Promise((resolve, reject) => {
        try {
            search_expense_calculator_bill(deepClone(request), limit, offset).then((data) => {
                if (data?.bills?.length) {
                    resolve(data.bills)
                } else {
                    resolve([])
                }
            }).catch(err => reject(err))
        } catch (error) {
            reject(error)
            logger.error(error.stack || error);
        }
    })
}

const fetchBankAccountDetailsByRequest = (request) => {
    return new Promise((resolve, reject) => {
        try {
            search_bank_account_details(deepClone(request)).then((data) => {
                if (data?.bankAccounts?.length) {
                    resolve(data.bankAccounts)
                } else {
                    resolve([])
                }
            }).catch(err => reject(err))
        } catch (error) {
            reject(error)
            logger.error(error.stack || error);
        }
    })
}


const getBillsForExcel = (billDetails, headCodeMap) => {
    let bill = billDetails?.bill;
    let bills = []
    let billObj = {
        "projectId" : billDetails?.projectNumber,
        "workOrderId" : billDetails?.contractNumber,
        "billNumber" : bill?.billNumber,
        "billType" :  bill?.businessService,
        "grossAmount" : 0,
        "beneficiaryType" : "",
        "beneficiaryName" : "",
        "beneficiaryId" : "",
        "accountType" : "", // call bank account service with paye.identifire id
        "bankAccountNumber" : "",
        "ifsc" : "",
        "payableAmount" : 0,
        "headCode": ""
    }
    if (!(bill.billDetails && bill.billDetails.length)) { 
        return [billObj];
    }
    let businessService = bill['businessService'];
    if (businessService == 'EXPENSE.WAGES') {
        let wagesBills = getWagesBill(bill, billObj);
        bills = bills.concat(wagesBills);
    } else if (businessService == 'EXPENSE.PURCHASE') {
        let purchaseBills = getPurchaseBill(bill, billObj, headCodeMap);
        bills = bills.concat(purchaseBills);
    } else if (businessService == 'EXPENSE.SUPERVISION') {
        let supervisionBills = getSupervisionBill(bill, billObj);
        bills = bills.concat(supervisionBills);
    } else {
        bills = [billObj];
    }
    return bills;
}

let getWagesBill = (bill, billObj) => {
    let bills = [];
    bill?.billDetails.forEach(billDetail => {
        billDetail?.payableLineItems.forEach((payableLineItem) => {
            let newBill = deepClone(billObj);
            newBill['beneficiaryId'] = billDetail?.payee?.identifier || "";
            newBill['beneficiaryType'] = billDetail?.payee?.type || "";
            newBill['grossAmount'] = payableLineItem?.amount || 0;
            newBill['payableAmount'] = payableLineItem?.amount || 0;
            newBill['headCode'] = payableLineItem?.headCode || "";
            bills.push(newBill)
        })
    });
    return bills;
}

let getPurchaseBill = (bill, billObj, headCodeMap) => {
    let bills = [];
    bill?.billDetails.forEach(billDetail => {
        if (billDetail?.payableLineItems?.length) {
            billDetail?.payableLineItems.forEach((payableLineItem) => {
                if (payableLineItem.status == "ACTIVE") {
                    let newBill = deepClone(billObj);
                    let headCodeBeneficiary = getBeneficiaryByHeadCode(payableLineItem, headCodeMap)
                    newBill['beneficiaryId'] = headCodeBeneficiary || billDetail?.payee?.identifier || "";
                    newBill['beneficiaryType'] = billDetail?.payee?.type || "";
                    newBill['grossAmount'] = payableLineItem?.amount || 0;
                    newBill['payableAmount'] = payableLineItem?.amount || 0;
                    newBill['headCode'] = payableLineItem?.headCode || "";
                    bills.push(newBill)
                }
            })
        }
    });
    return bills;
}

// Get beneficiary by headcode 
let getBeneficiaryByHeadCode = (payableLineItem, headCodeMap) => {
    let beneficiaryIdFormate = config.constraints.beneficiaryIdByHeadCode;
    if (payableLineItem && beneficiaryIdFormate) {
        let plHeadCode = payableLineItem.headCode || "";
        let headCode = headCodeMap[plHeadCode] 
        // If headcode category is deduction then change beneficiaryId
        if (headCode && headCode?.category == "deduction") {
            beneficiaryIdFormate = beneficiaryIdFormate.replace("{tanentId}", payableLineItem.tenantId)
            beneficiaryIdFormate = beneficiaryIdFormate.replace("{headcode}", plHeadCode)
            return beneficiaryIdFormate;
        } else {
            return null
        }
    } else {
        return null;
    }
}

let getSupervisionBill = (bill, billObj) => {
    let bills = [];
    bill?.billDetails.forEach(billDetail => {
        let newBill = deepClone(billObj);
        newBill['beneficiaryId'] = get(billDetail,'payee.identifier', "");
        newBill['beneficiaryType'] = get(billDetail, "payee.type", "");
        newBill['grossAmount'] = get(billDetail, 'payableLineItems[0].amount', 0);
        newBill['payableAmount'] = get(billDetail, 'payableLineItems[0].amount', 0);
        newBill['headCode'] = get(billDetail, 'payableLineItems[0].headCode', "");
        bills.push(newBill)
    })
    return bills;
}

let createXlsxFromBills = async (billsData, paymentId, paymentNumber, tenantId) => {
    const data = billsData.map((obj, idx) => [
        idx+1,
        obj.projectId,
        obj.workOrderId,
        obj.billNumber,
        obj.billType,
        obj.grossAmount,
        obj.beneficiaryType,
        obj.beneficiaryName,
        obj.beneficiaryId,
        obj.accountType,
        obj.bankAccountNumber,
        obj.ifsc,
        obj.payableAmount,
        obj.headCode
    ]);
    const headers = [
        "Sr. No.",
        "Project ID",
        "Work Order ID",
        "Bill Number",
        "Bill Type",
        "Gross Amount",
        "Beneficiary Type",
        "Beneficiary Name",
        "Beneficiary ID",
        "Account Type",
        "Bank Account Number",
        "IFSC",
        "Payable Amount",
        "Head Code"
    ];
    const worksheet = XLSX.utils.aoa_to_sheet([headers, ...data]);

    // Create a new workbook and add the worksheet to it
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Payment');
    const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'buffer' });
    try {
        // if payment number will available then file name will be based on payment number else paymentId
        let filename = "";
        if (paymentNumber) {
            filename = paymentNumber;
        } else {
            filename = paymentId;
        }
        filename = filename + " - " + new Date().getTime() + ".xlsx";
        let filestoreId = await upload_file_using_filestore(filename, tenantId, buffer);
        return filestoreId;
    } catch (error) {
        logger.error(error.stack || error);
        throw(error)
    }
}


async function updateForJobCompletion (paymentId, filestoreId, userId, billsLength, numberofbeneficialy, totalAmount) {
    try {
        const updateQuery = 'UPDATE eg_payments_excel SET filestoreid =  $1, lastmodifiedby = $2, lastmodifiedtime = $3, status = $4, numberofbills = $5, numberofbeneficialy = $6, totalamount = $7 WHERE paymentid = $8';
        const curentTimeStamp = new Date().getTime();
        let status = "COMPLETED";
        await exec_query_eg_payments_excel(updateQuery, [filestoreId, userId, curentTimeStamp, status, billsLength, numberofbeneficialy, totalAmount, paymentId]);
    } catch (error) {
        logger.error("Error occured while updating the eg_payments_excel table.");
        logger.error(error.stack || error);
    }
}

async function updatePaymentExcelIfJobFailed(paymentId, userId) {
    try {
        const updateQuery = 'UPDATE eg_payments_excel SET lastmodifiedby = $1, lastmodifiedtime = $2, status = $3 WHERE paymentid = $4';
        const curentTimeStamp = new Date().getTime();
        let status = "FAILED";
        await exec_query_eg_payments_excel(updateQuery, [userId, curentTimeStamp, status, paymentId]);
    } catch (error) {
        logger.error("Error occured while executing updatePaymentExcelIfJobFailed.");
        logger.error(error.stack || error);
    }
}

let deepClone = (data) => { return JSON.parse(JSON.stringify(data)) };

module.exports = { processGroupBillFromPaymentId, processGroupBillFromPaymentCreateTopic };