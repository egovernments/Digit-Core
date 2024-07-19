const get = require("lodash.get");

/**
 * Return language from request
 * @param {*} requestInfo 
 */
let getLanguageFromRequest = (request) => {
    let lang = "en_IN";
    let msgId = get(request, "RequestInfo.msgId", null)
    if (msgId) {
        msgId = msgId.split("|")
        lang = msgId.length == 2 ? msgId[1] : lang;
    }
    return lang;
}

let getStateLocalizationModule = (tenantId) => {
    let rootTenantId = tenantId.split(".")[0];
    return 'rainmaker-' + rootTenantId;
}

let getCityLocalizationModule = (tenantId) => {
    return 'rainmaker-' + tenantId;
}

let getStateLocalizationPrefix = (tenantId) => {
    let rootTenantId = tenantId.split(".")[0];
    return rootTenantId.toUpperCase();
}

let getCityLocalizationPrefix = (tenantId) => {
    let nTenantId = tenantId.toUpperCase();
    let tenants = nTenantId.split(".").join("_");
    return tenants;
}

let getLocalizationByKey = (key, localizationMap) => {
    if (localizationMap) {
        return localizationMap[key] || key;
    }
    return key;
}


module.exports = {
    getLanguageFromRequest,
    getStateLocalizationModule,
    getCityLocalizationModule,
    getStateLocalizationPrefix,
    getCityLocalizationPrefix,
    getLocalizationByKey
}