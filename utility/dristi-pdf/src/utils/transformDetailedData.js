const e = require("express");
const { logger } = require("../logger");

const transformDetailedData = (data) => {

    //iterate over estimate make an array of object which contains estimateNumber, projectNumber, projectName and description and an array of estimateDetails
    const estimates = {};
    estimates["estimateNumber"] = data.estimates[0].estimateNumber;
    estimates["projectNumber"] = data.estimates[0].additionalDetails.projectNumber;
    estimates["projectName"] = data.estimates[0].additionalDetails.projectName;
    estimates["description"] = data.estimates[0].description;
    estimates["tenantId"] = data.estimates[0].tenantId;
    estimates["projectName"] = data.estimates[0].additionalDetails.projectName;
    estimates["projectLocation"] = data.estimates[0].additionalDetails.locality;
    estimates["totalEstimatedAmount"] = data.estimates[0].additionalDetails.totalEstimatedAmount;
    const sorIdMap = {};

    for (const estimateDetail of data.estimates[0].estimateDetails) {

        if(estimateDetail.category === 'NON-SOR' && estimateDetail.sorId === null){
            estimateDetail.sorId = '0';
        }
        
        // if in the end of name there is a square bracket then convert that square bracket into round bracket
        if(estimateDetail.name.includes('[')){
            estimateDetail.name = estimateDetail.name.replace('[', '(');
            estimateDetail.name = estimateDetail.name.replace(']', ')');
        }
    
        const { sorId, category, isDeduction, name, description, uom, unitRate, quantity, amountDetail, additionalDetails } = estimateDetail;
    
        if (sorIdMap[sorId] === undefined) {
            const amount = isDeduction ? -amountDetail[0].amount : amountDetail[0].amount;
    
            sorIdMap[sorId] = {
                sorId,
                category,
                name,
                description,
                uom,
                unitRate,
                quantity,
                amount,
                additionalDetails
            };
        } else {
            const sorIdEntry = sorIdMap[sorId];
            const amountChange = isDeduction ? -amountDetail[0].amount : amountDetail[0].amount;
    
            sorIdEntry.amount += amountChange;
            sorIdEntry.quantity += isDeduction ? -quantity : quantity;
        }
    }

    const estimateDetails = [];
    for(const key in sorIdMap){
        estimateDetails.push(sorIdMap[key]);
    }
    estimates["estimateDetails"] = estimateDetails;



    return estimates;

    
};

module.exports = {
    transformDetailedData
    };
