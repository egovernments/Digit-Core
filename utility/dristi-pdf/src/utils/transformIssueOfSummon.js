const { logger } = require("../logger");

const transformIssueOfSummon = (data) => {
    // Example data transformation
    const caseData = {
        // "Data": [
        //     {
        //         "id": data.id || 1826,
        //         "courtName": data.courtName || "Supreme Court",
        //         "place": data.place || "New York",
        //         "state": data.state || "NY",
        //         "caseNumber": data.caseNumber || "12345",
        //         "year": data.year || "2024",
        //         "caseName": data.caseName || "Smith vs. Johnson",
        //         "respondentName": data.respondentName || "John Doe",
        //         "date": data.date || "July 18, 2024",
        //         "hearingDate": data.hearingDate || "August 1, 2024",
        //         "additionalComments": data.additionalComments || "Please ensure all relevant documents are submitted prior to the hearing date.",
        //         "judgeSignature": data.judgeSignature || "Judge's Signature",
        //         "judgeName": data.judgeName || "Hon. Jane Smith",
        //         "courtSeal": data.courtSeal || "Court Seal"
        //     }
        // ]

        "Data": [
        {
            "id": 1826,
            "courtName": "Supreme Court",
            "place": "New York",
            "state": "NY",
            "caseNumber": "12345",
            "year": "2024",
            "caseName": data.criteria[0].responseList[0].caseTitle,
            "respondentName": "John Doe",
            "date": "July 18, 2024",
            "hearingDate": "August 1, 2024",
            "additionalComments": "Please ensure all relevant documents are submitted prior to the hearing date.",
            "judgeSignature": "Judge's Signature",
            "judgeName": "Hon. Jane Smith",
            "courtSeal": "Court Seal"
        }
    ]
    };

    return caseData;
};

module.exports = {
    transformIssueOfSummon
};
