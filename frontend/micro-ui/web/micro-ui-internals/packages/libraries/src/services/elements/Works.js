import { Request } from "../atoms/Utils/Request";
import Urls from "../atoms/urls";
import { filter } from "lodash";
export const WorksService = {
  createLOI: (details) =>
    Request({
      url: Urls.works.create,
      data: details,
      useCache: false,
      setTimeParam: false,
      userService: true,
      method: "POST",
      params: {},
      auth: true,
    }),
  estimateSearch: ({ tenantId, filters }) =>{
    return Request({
      url: Urls.works.searchEstimate,
      useCache: false,
      method: "POST",
      auth: true,
      userService: false,
      params: { tenantId, ...filters },
    })
  },
  loiSearch: ({ tenantId, filters }) =>
    Request({
      url: Urls.works.loiSearch,
      useCache: false,
      method: "POST",
      auth: true,
      userService: false,
      params: { tenantId, ...filters },
    }),
  createEstimate: (details) =>
    Request({
      url: Urls.works.createEstimate,
      data: details,
      useCache: false,
      setTimeParam: false,
      userService: true,
      method: "POST",
      // params:{},
      auth: true,
    }),
  approvedEstimateSearch: ({ tenantId, filters }) =>
    Request({
      //update URL for Approved Estimate Search
      url: Urls.works.approvedEstimateSearch,
      useCache: false,
      method: "POST",
      auth: true,
      userService: false,
      params: { tenantId, ...filters },
    }),
  SearchEstimate: (details) =>
    Request({
      url: Urls.works.searchEstimate,
      data: details,
      useCache: false,
      setTimeParam: false,
      userService: true,
      method: "POST",
      auth: true,
    }),
  updateLOI: (details) =>
    Request({
      url: Urls.works.updateLOI,
      data: details,
      useCache: false,
      setTimeParam: false,
      userService: true,
      method: "POST",
      // params:{},
      auth: true,
    }),
  updateEstimate: (details) =>
    Request({
      url: Urls.works.updateEstimate,
      data: details,
      useCache: false,
      setTimeParam: false,
      userService: true,
      method: "POST",
      // params:{},
      auth: true,
    }),
  downloadEstimate: (tenantId, estimateNumber) =>
    Request({
      url: Urls.works.download_pdf,
      data: {},
      useCache: true,
      method: "POST",
      params: { tenantId, estimateNumber },
      auth: true,
      locale: true,
      userService: true,
      userDownload: true,
    }),
  createProject: (details) =>
    Request({
      url: Urls?.works?.createProject,
      data: details,
      useCache: false,
      setTimeParam: false,
      userService: true,
      method: "POST",
      params: {},
      auth: true,
    }),
  updateProject: (details) =>
    Request({
      url: Urls?.works?.updateProject,
      data: details,
      useCache: false,
      setTimeParam: false,
      userService: true,
      method: "POST",
      params: {},
      auth: true,
    }),
  searchProject: (tenantId, details, filters) =>
    Request({
      url: Urls.works.searchProject,
      data: details,
      useCache: false,
      setTimeParam: false,
      userService: true,
      method: "POST",
      params: {
        tenantId,
        limit: filters?.limit,
        offset: filters?.offset,
        includeAncestors: filters?.includeAncestors,
        includeDescendants: filters?.includeDescendants,
      },
      auth: true,
    }),
  createWO: (details) =>
    Request({
      url: Urls?.contracts?.createWO,
      data: details,
      useCache: false,
      setTimeParam: false,
      userService: true,
      method: "POST",
      params: {},
      auth: true,
    }),
  updateWO: (details) =>
    Request({
      url: Urls?.contracts?.update,
      data: details,
      useCache: false,
      setTimeParam: false,
      userService: true,
      method: "POST",
      params: {},
      auth: true,
    }),
  searchOrg: (details) =>
    Request({
      url: Urls?.organisation?.search,
      data: details,
      useCache: false,
      setTimeParam: false,
      userService: true,
      method: "POST",
      params: {},
      auth: true,
    }),
  contractSearch: ({ tenantId, filters }) => {
    return Request({
      url: Urls.contracts.search,
      useCache: false,
      data: filters,
      method: "POST",
      auth: true,
      userService: false,
      params: { tenantId },
    });
  },
  createBill: (body) => {
    return Request({
      url: Urls.bills.createBill,
      useCache: false,
      data: body,
      method: "POST",
      auth: true,
      userService: false,
      params: {},
    });
  },
  fetchEstimateExpenseCalculator: (details) => {
    return Request({
      url: Urls.calculator.expenseBill,
      useCache: false,
      data: details,
      method: "POST",
      auth: true,
      userService: false,
      params: {},
    });
  },
  searchBill: async(details) => {
   const billObject=await  Request({
      url: Urls.bills.searchBill,
      useCache: false,
      data: details,
      method: "POST",
      auth: true,
      userService: false,
      params: {},
    });
    if(billObject?.bills?.[0]?.billDetails?.[0]?.lineItems?.[0]){
      billObject.bills[0].billDetails[0].lineItems=  [...billObject?.bills?.[0]?.billDetails?.[0]?.lineItems?.filter(item=>item.status=="ACTIVE")];
    }
    return billObject;
  },
  searchBillCalculator: (details) => {
    return Request({
      url: Urls.bill.searchCalculator,
      useCache: false,
      data: details,
      method: "POST",
      auth: true,
      userService: false,
      params: {},
    });
  },
  createPurchaseBill: (details) =>
    Request({
      url: Urls.bills.createPurchaseBill,
      data: details,
      useCache: false,
      setTimeParam: false,
      userService: true,
      method: "POST",
      auth: true,
    }),
  updatePurchaseBill: (details) =>
    Request({
      url: Urls.bills.updatePurchaseBill,
      data: details,
      useCache: false,
      setTimeParam: false,
      userService: true,
      method: "POST",
      auth: true,
    }),
};
