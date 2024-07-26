    const { logger } = require("../logger");

const transformIssueOfSummon = (id, caseResponse, hrmsResponse, mdmsResponse) => {
    // Example data transformation
    const data = {
        "Data": [
        {
            "id": id,
            "courtName": mdmsResponse.mdms[0].data.name,
            "place": mdmsResponse.mdms[0].data.address,
            "state": mdmsResponse.mdms[0].data.address,
            "caseNumber": caseResponse.criteria[0].responseList[0].cnrNumber,
            "year": "2024",
            "caseName": caseResponse.criteria[0].responseList[0].caseTitle,
            "respondentName": "John Doe",
            "date": caseResponse.criteria[0].responseList[0].filingDate,
            "hearingDate": "August 1, 2024",
            "additionalComments": "Please ensure all relevant documents are submitted prior to the hearing date.",
            "judgeSignature": "Judge's Signature",
            "judgeName": "Hon. Jane Smith",
            "courtSeal": "Court Seal"
        }
    ]
    };

    return data;
};

module.exports = {
    transformIssueOfSummon
};
